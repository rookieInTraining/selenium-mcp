package com.autom8tr.selenium_mcp.core.session;

import com.autom8tr.selenium_mcp.core.SessionMetadata;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for session storage abstraction.
 * Allows different implementations (in-memory, Redis, etc.) for session management.
 */
public interface SessionStorage {

    /**
     * Stores a new session with its WebDriver and metadata.
     *
     * @param sessionId   Unique session identifier
     * @param driver      WebDriver instance
     * @param metadata    Session metadata
     * @throws SessionStorageException if storage fails
     */
    void save(UUID sessionId, WebDriver driver, SessionMetadata metadata);

    /**
     * Retrieves a WebDriver by session ID.
     *
     * @param sessionId Session identifier
     * @return Optional containing WebDriver if found
     */
    Optional<WebDriver> getDriver(UUID sessionId);

    /**
     * Retrieves session metadata by session ID.
     *
     * @param sessionId Session identifier
     * @return Optional containing SessionMetadata if found
     */
    Optional<SessionMetadata> getMetadata(UUID sessionId);

    /**
     * Removes a session from storage.
     *
     * @param sessionId Session identifier
     * @return The removed WebDriver, or null if not found
     */
    WebDriver remove(UUID sessionId);

    /**
     * Checks if a session exists.
     *
     * @param sessionId Session identifier
     * @return true if session exists
     */
    boolean exists(UUID sessionId);

    /**
     * Gets all session IDs.
     *
     * @return Set of session IDs
     */
    Set<UUID> getAllSessionIds();

    /**
     * Gets all session metadata.
     *
     * @return List of all session metadata
     */
    List<SessionMetadata> getAllMetadata();

    /**
     * Gets the current count of stored sessions.
     *
     * @return Number of sessions
     */
    int getSessionCount();

    /**
     * Updates session metadata.
     *
     * @param sessionId Session identifier
     * @param metadata  Updated metadata
     */
    void updateMetadata(UUID sessionId, SessionMetadata metadata);

    /**
     * Updates session's last accessed timestamp.
     *
     * @param sessionId Session identifier
     */
    void touch(UUID sessionId);

    /**
     * Cleans up expired sessions based on TTL.
     *
     * @param ttlSeconds Session time-to-live in seconds
     * @return Number of sessions cleaned up
     */
    int cleanupExpired(long ttlSeconds);

    /**
     * Closes and removes all sessions.
     *
     * @return Number of sessions closed
     */
    int closeAll();

    /**
     * Checks if the storage is healthy and accessible.
     *
     * @return true if storage is healthy
     */
    boolean isHealthy();

    /**
     * Exception thrown when session storage operations fail.
     */
    class SessionStorageException extends RuntimeException {
        public SessionStorageException(String message) {
            super(message);
        }

        public SessionStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

