package com.autom8tr.selenium_mcp.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-based session registry for distributed session tracking.
 * Stores session metadata and pod affinity information.
 * Note: Actual WebDriver instances are stored in local cache per pod.
 */
public class RedisSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(RedisSessionRegistry.class);

    private static final String SESSION_KEY_PREFIX = "selenium:session:";
    private static final String SESSION_INDEX_KEY = "selenium:sessions";
    private static final String POD_SESSIONS_PREFIX = "selenium:pod:";

    private final RedisTemplate<String, SessionRegistryEntry> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final String podId;
    private final String podAddress;
    private final Duration defaultTtl;

    public RedisSessionRegistry(
            RedisTemplate<String, SessionRegistryEntry> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            String podId,
            String podAddress,
            Duration defaultTtl) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.podId = podId;
        this.podAddress = podAddress;
        this.defaultTtl = defaultTtl;

        log.info("Initialized Redis session registry for pod: {} at {}", podId, podAddress);
    }

    /**
     * Registers a new session in Redis.
     */
    public void registerSession(UUID sessionId, SessionRegistryEntry entry) {
        String key = SESSION_KEY_PREFIX + sessionId;

        // Store session entry
        redisTemplate.opsForValue().set(key, entry, defaultTtl);

        // Add to session index
        stringRedisTemplate.opsForSet().add(SESSION_INDEX_KEY, sessionId.toString());

        // Add to pod's session list
        stringRedisTemplate.opsForSet().add(POD_SESSIONS_PREFIX + podId, sessionId.toString());

        log.debug("Registered session {} in Redis for pod {}", sessionId, podId);
    }

    /**
     * Gets session entry by ID.
     */
    public Optional<SessionRegistryEntry> getSession(UUID sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        SessionRegistryEntry entry = redisTemplate.opsForValue().get(key);

        if (entry != null) {
            // Update last accessed time
            entry.updateLastAccessed();
            redisTemplate.opsForValue().set(key, entry, defaultTtl);
        }

        return Optional.ofNullable(entry);
    }

    /**
     * Updates session entry.
     */
    public void updateSession(UUID sessionId, SessionRegistryEntry entry) {
        String key = SESSION_KEY_PREFIX + sessionId;
        entry.updateLastAccessed();
        redisTemplate.opsForValue().set(key, entry, defaultTtl);
    }

    /**
     * Removes session from Redis.
     */
    public boolean removeSession(UUID sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;

        // Get entry to find pod
        SessionRegistryEntry entry = redisTemplate.opsForValue().get(key);

        // Remove from session index
        stringRedisTemplate.opsForSet().remove(SESSION_INDEX_KEY, sessionId.toString());

        // Remove from pod's session list
        if (entry != null) {
            stringRedisTemplate.opsForSet().remove(POD_SESSIONS_PREFIX + entry.getPodId(), sessionId.toString());
        }

        // Remove session entry
        Boolean deleted = redisTemplate.delete(key);

        log.debug("Removed session {} from Redis: {}", sessionId, deleted);
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Checks if session exists in Redis.
     */
    public boolean sessionExists(UUID sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Gets all session IDs.
     */
    public Set<UUID> getAllSessionIds() {
        Set<String> ids = stringRedisTemplate.opsForSet().members(SESSION_INDEX_KEY);
        if (ids == null) {
            return Collections.emptySet();
        }

        return ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Gets session IDs owned by this pod.
     */
    public Set<UUID> getSessionsForCurrentPod() {
        Set<String> ids = stringRedisTemplate.opsForSet().members(POD_SESSIONS_PREFIX + podId);
        if (ids == null) {
            return Collections.emptySet();
        }

        return ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Gets session IDs owned by a specific pod.
     */
    public Set<UUID> getSessionsForPod(String targetPodId) {
        Set<String> ids = stringRedisTemplate.opsForSet().members(POD_SESSIONS_PREFIX + targetPodId);
        if (ids == null) {
            return Collections.emptySet();
        }

        return ids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    /**
     * Gets total session count across all pods.
     */
    public long getTotalSessionCount() {
        Long count = stringRedisTemplate.opsForSet().size(SESSION_INDEX_KEY);
        return count != null ? count : 0;
    }

    /**
     * Gets session count for current pod.
     */
    public long getCurrentPodSessionCount() {
        Long count = stringRedisTemplate.opsForSet().size(POD_SESSIONS_PREFIX + podId);
        return count != null ? count : 0;
    }

    /**
     * Checks which pod owns a session.
     */
    public Optional<String> getSessionOwnerPod(UUID sessionId) {
        return getSession(sessionId).map(SessionRegistryEntry::getPodId);
    }

    /**
     * Checks if this pod owns the session.
     */
    public boolean isSessionOwnedByCurrentPod(UUID sessionId) {
        return getSession(sessionId)
                .map(entry -> entry.isOwnedByPod(podId))
                .orElse(false);
    }

    /**
     * Cleans up expired sessions.
     */
    public int cleanupExpiredSessions(long ttlSeconds) {
        Set<UUID> allSessions = getAllSessionIds();
        int cleaned = 0;

        for (UUID sessionId : allSessions) {
            Optional<SessionRegistryEntry> entry = getSession(sessionId);
            if (entry.isPresent() && entry.get().isExpired(ttlSeconds)) {
                if (removeSession(sessionId)) {
                    cleaned++;
                    log.info("Cleaned up expired session from Redis: {}", sessionId);
                }
            }
        }

        return cleaned;
    }

    /**
     * Removes all sessions owned by current pod (for graceful shutdown).
     */
    public int removeAllCurrentPodSessions() {
        Set<UUID> podSessions = getSessionsForCurrentPod();
        int removed = 0;

        for (UUID sessionId : podSessions) {
            if (removeSession(sessionId)) {
                removed++;
            }
        }

        log.info("Removed {} sessions for pod {} from Redis", removed, podId);
        return removed;
    }

    /**
     * Checks Redis connectivity.
     */
    public boolean isHealthy() {
        try {
            stringRedisTemplate.opsForValue().get("health-check");
            return true;
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }

    public String getPodId() {
        return podId;
    }

    public String getPodAddress() {
        return podAddress;
    }
}

