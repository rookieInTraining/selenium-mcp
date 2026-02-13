package com.autom8tr.selenium_mcp.health;

import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.core.SessionMetadata;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health indicator for Selenium session management.
 * Reports session status, capacity, and storage health.
 */
@Component
public class SessionHealthIndicator implements HealthIndicator {

    private final SeleniumSessionContainer sessionContainer;

    // Health thresholds
    private static final double WARNING_UTILIZATION_THRESHOLD = 0.8;
    private static final double CRITICAL_UTILIZATION_THRESHOLD = 0.95;

    public SessionHealthIndicator(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Override
    public Health health() {
        try {
            int activeSessions = sessionContainer.getActiveSessionCount();
            int maxSessions = sessionContainer.getMaxSessions();
            double utilization = maxSessions > 0 ? (double) activeSessions / maxSessions : 0;

            boolean storageHealthy = sessionContainer.isHealthy();

            // Count sessions by state
            List<SessionMetadata> metadata = sessionContainer.getAllSessionMetadata();
            Map<String, Long> sessionsByState = metadata.stream()
                    .collect(Collectors.groupingBy(
                            m -> m.getState().name(),
                            Collectors.counting()
                    ));

            // Count sessions by browser
            Map<String, Long> sessionsByBrowser = metadata.stream()
                    .collect(Collectors.groupingBy(
                            SessionMetadata::getBrowserType,
                            Collectors.counting()
                    ));

            // Calculate average session age
            double avgAgeSeconds = metadata.stream()
                    .mapToLong(SessionMetadata::getAgeInSeconds)
                    .average()
                    .orElse(0);

            // Calculate average idle time
            double avgIdleSeconds = metadata.stream()
                    .mapToLong(SessionMetadata::getIdleTimeInSeconds)
                    .average()
                    .orElse(0);

            // Build health details
            Health.Builder healthBuilder;

            if (!storageHealthy) {
                healthBuilder = Health.down()
                        .withDetail("reason", "Session storage is unhealthy");
            } else if (utilization >= CRITICAL_UTILIZATION_THRESHOLD) {
                healthBuilder = Health.down()
                        .withDetail("reason", "Session capacity critical (>95%)");
            } else if (utilization >= WARNING_UTILIZATION_THRESHOLD) {
                healthBuilder = Health.status("WARNING")
                        .withDetail("reason", "Session capacity high (>80%)");
            } else {
                healthBuilder = Health.up();
            }

            return healthBuilder
                    .withDetail("activeSessions", activeSessions)
                    .withDetail("maxSessions", maxSessions)
                    .withDetail("utilization", String.format("%.1f%%", utilization * 100))
                    .withDetail("storageHealthy", storageHealthy)
                    .withDetail("storageType", sessionContainer.getSessionStorage().getClass().getSimpleName())
                    .withDetail("sessionsByState", sessionsByState)
                    .withDetail("sessionsByBrowser", sessionsByBrowser)
                    .withDetail("avgSessionAgeSeconds", String.format("%.1f", avgAgeSeconds))
                    .withDetail("avgIdleTimeSeconds", String.format("%.1f", avgIdleSeconds))
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

