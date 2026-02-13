package com.autom8tr.selenium_mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for session management.
 */
@Component
@ConfigurationProperties(prefix = "selenium.session")
public class SessionProperties {

    /**
     * Maximum number of concurrent sessions per instance.
     */
    private int maxSessions = 100;

    /**
     * Session time-to-live (idle timeout).
     */
    private Duration ttl = Duration.ofMinutes(30);

    /**
     * Interval between session cleanup runs.
     */
    private Duration cleanupInterval = Duration.ofMinutes(5);

    /**
     * Enable distributed session management via Redis.
     */
    private boolean distributedEnabled = false;

    /**
     * Pod identifier for distributed mode (auto-generated if not set).
     */
    private String podId;

    /**
     * Pod address for session routing in distributed mode.
     */
    private String podAddress;

    public int getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public boolean isDistributedEnabled() {
        return distributedEnabled;
    }

    public void setDistributedEnabled(boolean distributedEnabled) {
        this.distributedEnabled = distributedEnabled;
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

    public long getTtlSeconds() {
        return ttl.toSeconds();
    }

    public long getCleanupIntervalMillis() {
        return cleanupInterval.toMillis();
    }
    
    /**
     * Gets cleanup interval in milliseconds as a string (for @Scheduled annotation).
     */
    public String getCleanupIntervalMillisString() {
        return String.valueOf(cleanupInterval.toMillis());
    }
}

