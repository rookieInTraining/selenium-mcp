package com.autom8tr.selenium_mcp.core;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata for tracking WebDriver session lifecycle and state.
 * Used for session management, TTL enforcement, and observability.
 */
public class SessionMetadata {

    private final UUID sessionId;
    private final String browserType;
    private final String deviceType;
    private final Instant createdAt;
    private volatile Instant lastAccessedAt;
    private volatile String currentUrl;
    private volatile SessionState state;

    public enum SessionState {
        INITIALIZING,
        ACTIVE,
        IDLE,
        CLOSING,
        CLOSED,
        ERROR
    }

    public SessionMetadata(UUID sessionId, String browserType, String deviceType) {
        this.sessionId = sessionId;
        this.browserType = browserType;
        this.deviceType = deviceType;
        this.createdAt = Instant.now();
        this.lastAccessedAt = this.createdAt;
        this.state = SessionState.INITIALIZING;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getBrowserType() {
        return browserType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void updateLastAccessed() {
        this.lastAccessedAt = Instant.now();
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
        updateLastAccessed();
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
        updateLastAccessed();
    }

    public long getAgeInSeconds() {
        return Instant.now().getEpochSecond() - createdAt.getEpochSecond();
    }

    public long getIdleTimeInSeconds() {
        return Instant.now().getEpochSecond() - lastAccessedAt.getEpochSecond();
    }

    public boolean isExpired(long ttlSeconds) {
        return getIdleTimeInSeconds() > ttlSeconds;
    }

    @Override
    public String toString() {
        return "SessionMetadata{" +
                "sessionId=" + sessionId +
                ", browserType='" + browserType + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                ", currentUrl='" + currentUrl + '\'' +
                ", state=" + state +
                '}';
    }
}

