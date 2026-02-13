package com.autom8tr.selenium_mcp.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Provides per-session and global rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "selenium.rate-limit")
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    // Global rate limiter
    private Bucket globalBucket;

    // Per-session rate limiters
    private final Map<UUID, Bucket> sessionBuckets = new ConcurrentHashMap<>();

    // Configuration
    private boolean enabled = true;
    private int globalRequestsPerMinute = 1000;
    private int sessionRequestsPerMinute = 60;
    private int sessionBurstSize = 10;

    public RateLimitConfig() {
        initializeGlobalBucket();
    }

    private void initializeGlobalBucket() {
        Bandwidth globalLimit = Bandwidth.classic(
                globalRequestsPerMinute,
                Refill.greedy(globalRequestsPerMinute, Duration.ofMinutes(1))
        );
        this.globalBucket = Bucket.builder()
                .addLimit(globalLimit)
                .build();

        log.info("Initialized global rate limiter: {} requests/minute", globalRequestsPerMinute);
    }

    /**
     * Gets or creates a rate limiter bucket for a session.
     */
    public Bucket getSessionBucket(UUID sessionId) {
        return sessionBuckets.computeIfAbsent(sessionId, id -> {
            Bandwidth limit = Bandwidth.classic(
                    sessionRequestsPerMinute,
                    Refill.greedy(sessionRequestsPerMinute, Duration.ofMinutes(1))
            );
            Bandwidth burst = Bandwidth.classic(
                    sessionBurstSize,
                    Refill.intervally(sessionBurstSize, Duration.ofSeconds(1))
            );

            return Bucket.builder()
                    .addLimit(limit)
                    .addLimit(burst)
                    .build();
        });
    }

    /**
     * Checks if a request is allowed for a session (applies both global and session limits).
     */
    public boolean tryConsume(UUID sessionId) {
        if (!enabled) {
            return true;
        }

        // Check global limit first
        if (!globalBucket.tryConsume(1)) {
            log.warn("Global rate limit exceeded");
            return false;
        }

        // Check session limit
        Bucket sessionBucket = getSessionBucket(sessionId);
        if (!sessionBucket.tryConsume(1)) {
            log.warn("Session rate limit exceeded for session: {}", sessionId);
            return false;
        }

        return true;
    }

    /**
     * Removes rate limiter for a closed session.
     */
    public void removeSession(UUID sessionId) {
        sessionBuckets.remove(sessionId);
    }

    /**
     * Gets available tokens for a session.
     */
    public long getAvailableTokens(UUID sessionId) {
        Bucket bucket = sessionBuckets.get(sessionId);
        return bucket != null ? bucket.getAvailableTokens() : sessionRequestsPerMinute;
    }

    // Configuration getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getGlobalRequestsPerMinute() {
        return globalRequestsPerMinute;
    }

    public void setGlobalRequestsPerMinute(int globalRequestsPerMinute) {
        this.globalRequestsPerMinute = globalRequestsPerMinute;
        initializeGlobalBucket();
    }

    public int getSessionRequestsPerMinute() {
        return sessionRequestsPerMinute;
    }

    public void setSessionRequestsPerMinute(int sessionRequestsPerMinute) {
        this.sessionRequestsPerMinute = sessionRequestsPerMinute;
    }

    public int getSessionBurstSize() {
        return sessionBurstSize;
    }

    public void setSessionBurstSize(int sessionBurstSize) {
        this.sessionBurstSize = sessionBurstSize;
    }
}
