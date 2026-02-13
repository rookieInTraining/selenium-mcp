package com.autom8tr.selenium_mcp.core.session;

import com.autom8tr.selenium_mcp.core.SessionMetadata;
import com.autom8tr.selenium_mcp.exception.SessionException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Hybrid session manager that combines local Caffeine cache with Redis registry.
 * 
 * Architecture:
 * - Local Caffeine cache: Stores WebDriver instances (not serializable)
 * - Redis: Stores session metadata and pod affinity information
 * 
 * This allows horizontal scaling where:
 * - Each pod manages its own WebDriver instances locally
 * - Redis tracks which pod owns which session
 * - Requests can be routed to the correct pod based on session affinity
 */
public class HybridSessionManager implements SessionStorage {

    private static final Logger log = LoggerFactory.getLogger(HybridSessionManager.class);

    // Local cache for WebDriver instances
    private final Cache<UUID, WebDriver> driverCache;

    // Local metadata cache (synced with Redis)
    private final ConcurrentHashMap<UUID, SessionMetadata> metadataCache;

    // Redis registry for distributed coordination
    private final RedisSessionRegistry redisRegistry;

    // Configuration
    private final int maxLocalSessions;
    private final Duration sessionTtl;
    private final String podId;

    public HybridSessionManager(
            RedisSessionRegistry redisRegistry,
            int maxLocalSessions,
            Duration sessionTtl,
            String podId) {
        this.redisRegistry = redisRegistry;
        this.maxLocalSessions = maxLocalSessions;
        this.sessionTtl = sessionTtl;
        this.podId = podId;

        this.metadataCache = new ConcurrentHashMap<>();

        // Configure Caffeine cache with eviction listener
        this.driverCache = Caffeine.newBuilder()
                .maximumSize(maxLocalSessions)
                .expireAfterAccess(sessionTtl)
                .removalListener((UUID key, WebDriver driver, RemovalCause cause) -> {
                    if (driver != null && cause != RemovalCause.EXPLICIT) {
                        log.info("Session {} evicted from cache due to: {}", key, cause);
                        cleanupEvictedSession(key, driver);
                    }
                })
                .build();

        log.info("Initialized HybridSessionManager: maxSessions={}, ttl={}, podId={}",
                maxLocalSessions, sessionTtl, podId);
    }

    @Override
    public void save(UUID sessionId, WebDriver driver, SessionMetadata metadata) {
        // Check local capacity
        if (driverCache.estimatedSize() >= maxLocalSessions) {
            throw SessionException.limitExceeded(maxLocalSessions);
        }

        // Store in local cache
        driverCache.put(sessionId, driver);
        metadataCache.put(sessionId, metadata);

        // Register in Redis
        SessionRegistryEntry entry = SessionRegistryEntry.fromMetadata(
                metadata, podId, redisRegistry.getPodAddress());
        redisRegistry.registerSession(sessionId, entry);

        log.debug("Saved session {} to hybrid storage", sessionId);
    }

    @Override
    public Optional<WebDriver> getDriver(UUID sessionId) {
        // First check local cache
        WebDriver driver = driverCache.getIfPresent(sessionId);
        if (driver != null) {
            touch(sessionId);
            return Optional.of(driver);
        }

        // Check if session exists in Redis but owned by another pod
        Optional<SessionRegistryEntry> entry = redisRegistry.getSession(sessionId);
        if (entry.isPresent()) {
            if (!entry.get().isOwnedByPod(podId)) {
                log.warn("Session {} exists but is owned by pod {}. Route request to that pod.",
                        sessionId, entry.get().getPodId());
                throw new SessionRoutingException(sessionId, entry.get().getPodId(), entry.get().getPodAddress());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<SessionMetadata> getMetadata(UUID sessionId) {
        // Check local cache first
        SessionMetadata metadata = metadataCache.get(sessionId);
        if (metadata != null) {
            return Optional.of(metadata);
        }

        // Check Redis
        return redisRegistry.getSession(sessionId)
                .map(this::convertToMetadata);
    }

    @Override
    public WebDriver remove(UUID sessionId) {
        // Remove from local caches
        metadataCache.remove(sessionId);
        WebDriver driver = driverCache.getIfPresent(sessionId);
        driverCache.invalidate(sessionId);

        // Remove from Redis
        redisRegistry.removeSession(sessionId);

        log.debug("Removed session {} from hybrid storage", sessionId);
        return driver;
    }

    @Override
    public boolean exists(UUID sessionId) {
        // Check local first (faster)
        if (driverCache.getIfPresent(sessionId) != null) {
            return true;
        }

        // Check Redis
        return redisRegistry.sessionExists(sessionId);
    }

    @Override
    public Set<UUID> getAllSessionIds() {
        // Return all sessions from Redis (complete view)
        return redisRegistry.getAllSessionIds();
    }

    /**
     * Gets session IDs that are locally available on this pod.
     */
    public Set<UUID> getLocalSessionIds() {
        return driverCache.asMap().keySet();
    }

    @Override
    public List<SessionMetadata> getAllMetadata() {
        return new ArrayList<>(metadataCache.values());
    }

    @Override
    public int getSessionCount() {
        return (int) driverCache.estimatedSize();
    }

    /**
     * Gets total session count across all pods (from Redis).
     */
    public long getTotalSessionCount() {
        return redisRegistry.getTotalSessionCount();
    }

    @Override
    public void updateMetadata(UUID sessionId, SessionMetadata metadata) {
        metadataCache.put(sessionId, metadata);

        // Update Redis
        SessionRegistryEntry entry = SessionRegistryEntry.fromMetadata(
                metadata, podId, redisRegistry.getPodAddress());
        redisRegistry.updateSession(sessionId, entry);
    }

    @Override
    public void touch(UUID sessionId) {
        SessionMetadata metadata = metadataCache.get(sessionId);
        if (metadata != null) {
            metadata.updateLastAccessed();

            // Update Redis
            redisRegistry.getSession(sessionId).ifPresent(entry -> {
                entry.updateLastAccessed();
                redisRegistry.updateSession(sessionId, entry);
            });
        }
    }

    @Override
    public int cleanupExpired(long ttlSeconds) {
        int localCleaned = 0;

        // Clean up local expired sessions
        List<UUID> expiredLocal = metadataCache.entrySet().stream()
                .filter(e -> e.getValue().isExpired(ttlSeconds))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (UUID sessionId : expiredLocal) {
            WebDriver driver = remove(sessionId);
            if (driver != null) {
                try {
                    driver.quit();
                    localCleaned++;
                    log.info("Cleaned up expired local session: {}", sessionId);
                } catch (Exception e) {
                    log.warn("Error closing expired session {}: {}", sessionId, e.getMessage());
                }
            }
        }

        // Also clean up orphaned Redis entries for this pod
        redisRegistry.cleanupExpiredSessions(ttlSeconds);

        return localCleaned;
    }

    @Override
    public int closeAll() {
        int closed = 0;

        // Close all local sessions
        for (UUID sessionId : new ArrayList<>(driverCache.asMap().keySet())) {
            WebDriver driver = remove(sessionId);
            if (driver != null) {
                try {
                    driver.quit();
                    closed++;
                } catch (Exception e) {
                    log.warn("Error closing session {}: {}", sessionId, e.getMessage());
                }
            }
        }

        // Remove all Redis entries for this pod
        redisRegistry.removeAllCurrentPodSessions();

        log.info("Closed {} local sessions during shutdown", closed);
        return closed;
    }

    @Override
    public boolean isHealthy() {
        return redisRegistry.isHealthy();
    }

    /**
     * Handles cleanup when a session is evicted from local cache.
     */
    private void cleanupEvictedSession(UUID sessionId, WebDriver driver) {
        try {
            driver.quit();
            log.info("Closed evicted session: {}", sessionId);
        } catch (Exception e) {
            log.warn("Error closing evicted session {}: {}", sessionId, e.getMessage());
        }

        // Remove from Redis
        redisRegistry.removeSession(sessionId);
        metadataCache.remove(sessionId);
    }

    /**
     * Converts Redis entry to SessionMetadata.
     */
    private SessionMetadata convertToMetadata(SessionRegistryEntry entry) {
        SessionMetadata metadata = new SessionMetadata(
                entry.getSessionIdAsUUID(),
                entry.getBrowserType(),
                entry.getDeviceType()
        );
        metadata.setCurrentUrl(entry.getCurrentUrl());
        metadata.setState(SessionMetadata.SessionState.valueOf(entry.getState()));
        return metadata;
    }

    public String getPodId() {
        return podId;
    }

    public int getMaxLocalSessions() {
        return maxLocalSessions;
    }

    /**
     * Exception indicating a session exists but on a different pod.
     */
    public static class SessionRoutingException extends RuntimeException {
        private final UUID sessionId;
        private final String targetPodId;
        private final String targetPodAddress;

        public SessionRoutingException(UUID sessionId, String targetPodId, String targetPodAddress) {
            super("Session " + sessionId + " is owned by pod " + targetPodId);
            this.sessionId = sessionId;
            this.targetPodId = targetPodId;
            this.targetPodAddress = targetPodAddress;
        }

        public UUID getSessionId() {
            return sessionId;
        }

        public String getTargetPodId() {
            return targetPodId;
        }

        public String getTargetPodAddress() {
            return targetPodAddress;
        }
    }
}

