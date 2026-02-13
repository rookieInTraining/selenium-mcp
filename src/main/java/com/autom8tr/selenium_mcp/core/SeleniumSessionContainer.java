package com.autom8tr.selenium_mcp.core;

import com.autom8tr.selenium_mcp.config.SessionProperties;
import com.autom8tr.selenium_mcp.core.session.SessionStorage;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Thread-safe session container for managing WebDriver instances.
 * Delegates to SessionStorage implementation (in-memory or distributed).
 * Provides session lifecycle management, metadata tracking, and automatic cleanup.
 */
@Service
public class SeleniumSessionContainer {

    private static final Logger log = LoggerFactory.getLogger(SeleniumSessionContainer.class);

    private final SessionStorage sessionStorage;
    private final SessionProperties sessionProperties;

    public SeleniumSessionContainer(SessionStorage sessionStorage, SessionProperties sessionProperties) {
        this.sessionStorage = sessionStorage;
        this.sessionProperties = sessionProperties;
        log.info("Initialized SeleniumSessionContainer with {} storage, maxSessions={}, ttl={}",
                sessionStorage.getClass().getSimpleName(),
                sessionProperties.getMaxSessions(),
                sessionProperties.getTtl());
    }

    /**
     * Creates a new session and stores the WebDriver instance.
     *
     * @param sessionId   Unique session identifier
     * @param driver      WebDriver instance
     * @param browserType Type of browser (e.g., "chrome", "firefox")
     * @param deviceType  Type of device (e.g., "desktop", "mobile")
     * @return SessionMetadata for the created session
     * @throws SessionException if max sessions limit is reached
     */
    public SessionMetadata createSession(UUID sessionId, WebDriver driver, String browserType, String deviceType) {
        if (sessionStorage.getSessionCount() >= sessionProperties.getMaxSessions()) {
            log.warn("Session limit reached. Current sessions: {}, Max: {}",
                    sessionStorage.getSessionCount(), sessionProperties.getMaxSessions());
            throw SessionException.limitExceeded(sessionProperties.getMaxSessions());
        }

        SessionMetadata metadata = new SessionMetadata(sessionId, browserType, deviceType);
        metadata.setState(SessionMetadata.SessionState.ACTIVE);

        sessionStorage.save(sessionId, driver, metadata);

        log.info("Created session: {} (browser: {}, device: {}). Total active sessions: {}",
                sessionId, browserType, deviceType, sessionStorage.getSessionCount());

        return metadata;
    }

    /**
     * Retrieves a WebDriver instance by session ID.
     * Automatically updates the last accessed time.
     *
     * @param sessionId Session identifier
     * @return WebDriver instance
     * @throws SessionException if session does not exist
     */
    public WebDriver getDriver(UUID sessionId) {
        // Touch the session to update last accessed time
        sessionStorage.touch(sessionId);
        
        return sessionStorage.getDriver(sessionId)
                .orElseThrow(() -> {
                    log.warn("Session not found: {}", sessionId);
                    return SessionException.notFound(sessionId);
                });
    }

    /**
     * Retrieves a WebDriver instance, returning Optional instead of throwing exception.
     *
     * @param sessionId Session identifier
     * @return Optional containing WebDriver if found
     */
    public Optional<WebDriver> getDriverOptional(UUID sessionId) {
        return sessionStorage.getDriver(sessionId);
    }

    /**
     * Retrieves session metadata by session ID.
     *
     * @param sessionId Session identifier
     * @return Optional containing SessionMetadata if found
     */
    public Optional<SessionMetadata> getSessionMetadata(UUID sessionId) {
        return sessionStorage.getMetadata(sessionId);
    }

    /**
     * Removes a session and closes the WebDriver.
     *
     * @param sessionId Session identifier
     * @return true if session was removed, false if it didn't exist
     */
    public boolean removeSession(UUID sessionId) {
        WebDriver driver = sessionStorage.remove(sessionId);
        if (driver != null) {
            try {
                driver.quit();
                log.info("Removed and closed session: {}. Remaining sessions: {}",
                        sessionId, sessionStorage.getSessionCount());
                return true;
            } catch (Exception e) {
                log.warn("Error closing WebDriver for session {}: {}", sessionId, e.getMessage());
                return true; // Session was still removed
            }
        }

        log.debug("Attempted to remove non-existent session: {}", sessionId);
        return false;
    }

    /**
     * Removes a session without closing the WebDriver (for external cleanup).
     *
     * @param sessionId Session identifier
     * @return The removed WebDriver, or null if not found
     */
    public WebDriver removeSessionWithoutClose(UUID sessionId) {
        WebDriver driver = sessionStorage.remove(sessionId);
        if (driver != null) {
            log.info("Removed session (without close): {}. Remaining sessions: {}",
                    sessionId, sessionStorage.getSessionCount());
        }
        return driver;
    }

    /**
     * Checks if a session exists.
     *
     * @param sessionId Session identifier
     * @return true if session exists
     */
    public boolean sessionExists(UUID sessionId) {
        return sessionStorage.exists(sessionId);
    }

    /**
     * Gets all active session IDs.
     *
     * @return Set of active session IDs
     */
    public Set<UUID> getAllSessionIds() {
        return sessionStorage.getAllSessionIds();
    }

    /**
     * Gets all session metadata.
     *
     * @return List of all session metadata
     */
    public List<SessionMetadata> getAllSessionMetadata() {
        return sessionStorage.getAllMetadata();
    }

    /**
     * Gets the current number of active sessions.
     *
     * @return Number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionStorage.getSessionCount();
    }

    /**
     * Gets the maximum number of allowed sessions.
     *
     * @return Maximum session count
     */
    public int getMaxSessions() {
        return sessionProperties.getMaxSessions();
    }

    /**
     * Cleans up expired sessions based on TTL.
     *
     * @return Number of sessions cleaned up
     */
    public int cleanupExpiredSessions() {
        return sessionStorage.cleanupExpired(sessionProperties.getTtlSeconds());
    }

    /**
     * Scheduled cleanup task.
     * Runs at fixed rate defined by cleanup-interval property.
     */
    @Scheduled(fixedDelayString = "#{@sessionProperties.getCleanupIntervalMillisString()}")
    public void scheduledCleanup() {
        log.debug("Running scheduled session cleanup...");
        int cleaned = cleanupExpiredSessions();
        if (cleaned > 0) {
            log.info("Session cleanup completed. Removed {} expired sessions.", cleaned);
        } else {
            log.debug("Session cleanup completed. No expired sessions found.");
        }
    }

    /**
     * Closes all active sessions. Used during application shutdown.
     *
     * @return Number of sessions closed
     */
    public int closeAllSessions() {
        log.info("Closing all {} active sessions...", sessionStorage.getSessionCount());
        int closed = sessionStorage.closeAll();
        log.info("Closed {} sessions during shutdown.", closed);
        return closed;
    }

    /**
     * Updates session state and current URL.
     *
     * @param sessionId Session identifier
     * @param url       Current URL
     */
    public void updateSessionUrl(UUID sessionId, String url) {
        sessionStorage.getMetadata(sessionId).ifPresent(metadata -> {
            metadata.setCurrentUrl(url);
            sessionStorage.updateMetadata(sessionId, metadata);
        });
    }

    /**
     * Updates session state.
     *
     * @param sessionId Session identifier
     * @param state     New session state
     */
    public void updateSessionState(UUID sessionId, SessionMetadata.SessionState state) {
        sessionStorage.getMetadata(sessionId).ifPresent(metadata -> {
            metadata.setState(state);
            sessionStorage.updateMetadata(sessionId, metadata);
        });
    }

    /**
     * Checks if the session storage is healthy.
     *
     * @return true if storage is healthy
     */
    public boolean isHealthy() {
        return sessionStorage.isHealthy();
    }

    /**
     * Gets the underlying session storage implementation.
     */
    public SessionStorage getSessionStorage() {
        return sessionStorage;
    }
}
