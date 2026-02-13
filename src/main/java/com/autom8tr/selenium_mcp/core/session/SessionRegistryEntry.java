package com.autom8tr.selenium_mcp.core.session;

import com.autom8tr.selenium_mcp.core.SessionMetadata;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Serializable entry for storing session information in Redis.
 * Contains session metadata and pod affinity information.
 * Note: WebDriver instances are NOT stored here as they are not serializable.
 */
public class SessionRegistryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String browserType;
    private String deviceType;
    private String podId;       // Identifies which pod owns this session
    private String podAddress;  // Pod address for routing
    private long createdAt;
    private long lastAccessedAt;
    private String currentUrl;
    private String state;

    public SessionRegistryEntry() {
    }

    public SessionRegistryEntry(UUID sessionId, SessionMetadata metadata, String podId, String podAddress) {
        this.sessionId = sessionId.toString();
        this.browserType = metadata.getBrowserType();
        this.deviceType = metadata.getDeviceType();
        this.podId = podId;
        this.podAddress = podAddress;
        this.createdAt = metadata.getCreatedAt().toEpochMilli();
        this.lastAccessedAt = metadata.getLastAccessedAt().toEpochMilli();
        this.currentUrl = metadata.getCurrentUrl();
        this.state = metadata.getState().name();
    }

    public static SessionRegistryEntry fromMetadata(SessionMetadata metadata, String podId, String podAddress) {
        return new SessionRegistryEntry(metadata.getSessionId(), metadata, podId, podAddress);
    }

    public UUID getSessionIdAsUUID() {
        return UUID.fromString(sessionId);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(String podId) {
        this.podId = podId;
    }

    public String getPodAddress() {
        return podAddress;
    }

    public void setPodAddress(String podAddress) {
        this.podAddress = podAddress;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public void updateLastAccessed() {
        this.lastAccessedAt = Instant.now().toEpochMilli();
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isExpired(long ttlSeconds) {
        long idleTime = (Instant.now().toEpochMilli() - lastAccessedAt) / 1000;
        return idleTime > ttlSeconds;
    }

    public boolean isOwnedByPod(String currentPodId) {
        return currentPodId != null && currentPodId.equals(this.podId);
    }

    @Override
    public String toString() {
        return "SessionRegistryEntry{" +
                "sessionId='" + sessionId + '\'' +
                ", browserType='" + browserType + '\'' +
                ", podId='" + podId + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}

