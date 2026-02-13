package com.autom8tr.selenium_mcp.core.session;

import com.autom8tr.selenium_mcp.core.SessionMetadata;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SessionStorage using ConcurrentHashMap.
 * Suitable for single-instance deployments or development.
 */
public class InMemorySessionStorage implements SessionStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemorySessionStorage.class);

    private final ConcurrentHashMap<UUID, WebDriver> drivers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, SessionMetadata> metadata = new ConcurrentHashMap<>();

    @Override
    public void save(UUID sessionId, WebDriver driver, SessionMetadata sessionMetadata) {
        if (sessionId == null || driver == null || sessionMetadata == null) {
            throw new SessionStorageException("Session ID, driver, and metadata are required");
        }

        drivers.put(sessionId, driver);
        metadata.put(sessionId, sessionMetadata);
        log.debug("Saved session: {}", sessionId);
    }

    @Override
    public Optional<WebDriver> getDriver(UUID sessionId) {
        WebDriver driver = drivers.get(sessionId);
        if (driver != null) {
            touch(sessionId);
        }
        return Optional.ofNullable(driver);
    }

    @Override
    public Optional<SessionMetadata> getMetadata(UUID sessionId) {
        return Optional.ofNullable(metadata.get(sessionId));
    }

    @Override
    public WebDriver remove(UUID sessionId) {
        SessionMetadata meta = metadata.remove(sessionId);
        if (meta != null) {
            meta.setState(SessionMetadata.SessionState.CLOSING);
        }

        WebDriver driver = drivers.remove(sessionId);
        if (driver != null) {
            log.debug("Removed session: {}", sessionId);
        }
        return driver;
    }

    @Override
    public boolean exists(UUID sessionId) {
        return drivers.containsKey(sessionId);
    }

    @Override
    public Set<UUID> getAllSessionIds() {
        return Collections.unmodifiableSet(new HashSet<>(drivers.keySet()));
    }

    @Override
    public List<SessionMetadata> getAllMetadata() {
        return new ArrayList<>(metadata.values());
    }

    @Override
    public int getSessionCount() {
        return drivers.size();
    }

    @Override
    public void updateMetadata(UUID sessionId, SessionMetadata sessionMetadata) {
        if (drivers.containsKey(sessionId)) {
            metadata.put(sessionId, sessionMetadata);
        }
    }

    @Override
    public void touch(UUID sessionId) {
        SessionMetadata meta = metadata.get(sessionId);
        if (meta != null) {
            meta.updateLastAccessed();
        }
    }

    @Override
    public int cleanupExpired(long ttlSeconds) {
        List<UUID> expiredSessions = metadata.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(ttlSeconds))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        int cleanedUp = 0;
        for (UUID sessionId : expiredSessions) {
            WebDriver driver = remove(sessionId);
            if (driver != null) {
                try {
                    driver.quit();
                    cleanedUp++;
                    log.info("Cleaned up expired session: {}", sessionId);
                } catch (Exception e) {
                    log.warn("Error closing expired session {}: {}", sessionId, e.getMessage());
                }
            }
        }

        return cleanedUp;
    }

    @Override
    public int closeAll() {
        int closed = 0;
        for (UUID sessionId : new ArrayList<>(drivers.keySet())) {
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
        log.info("Closed {} sessions", closed);
        return closed;
    }

    @Override
    public boolean isHealthy() {
        return true; // In-memory storage is always healthy if JVM is running
    }
}

