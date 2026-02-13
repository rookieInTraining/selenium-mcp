package com.autom8tr.selenium_mcp.metrics;

import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom metrics for Selenium MCP server.
 * Provides observability for session management, tool execution, and errors.
 */
@Component
public class SeleniumMetrics {

    private static final Logger log = LoggerFactory.getLogger(SeleniumMetrics.class);

    private final MeterRegistry meterRegistry;
    private final SeleniumSessionContainer sessionContainer;

    // Counters
    private final Counter sessionsCreatedCounter;
    private final Counter sessionsClosedCounter;
    private final Counter sessionCreationFailuresCounter;
    private final Counter toolExecutionsCounter;
    private final Counter toolErrorsCounter;

    // Gauges
    private final AtomicInteger activeSessionsGauge;
    private final AtomicLong totalToolExecutionTime;

    // Timers
    private final Timer sessionCreationTimer;
    private final Timer toolExecutionTimer;
    private final Timer pageLoadTimer;

    public SeleniumMetrics(MeterRegistry meterRegistry, SeleniumSessionContainer sessionContainer) {
        this.meterRegistry = meterRegistry;
        this.sessionContainer = sessionContainer;

        // Initialize counters
        this.sessionsCreatedCounter = Counter.builder("selenium.sessions.created")
                .description("Total number of browser sessions created")
                .register(meterRegistry);

        this.sessionsClosedCounter = Counter.builder("selenium.sessions.closed")
                .description("Total number of browser sessions closed")
                .register(meterRegistry);

        this.sessionCreationFailuresCounter = Counter.builder("selenium.sessions.creation.failures")
                .description("Total number of session creation failures")
                .register(meterRegistry);

        this.toolExecutionsCounter = Counter.builder("selenium.tools.executions")
                .description("Total number of tool executions")
                .register(meterRegistry);

        this.toolErrorsCounter = Counter.builder("selenium.tools.errors")
                .description("Total number of tool execution errors")
                .register(meterRegistry);

        // Initialize gauges
        this.activeSessionsGauge = new AtomicInteger(0);
        Gauge.builder("selenium.sessions.active", activeSessionsGauge, AtomicInteger::get)
                .description("Current number of active browser sessions")
                .register(meterRegistry);

        Gauge.builder("selenium.sessions.max", sessionContainer, sc -> sc.getMaxSessions())
                .description("Maximum allowed browser sessions")
                .register(meterRegistry);

        Gauge.builder("selenium.sessions.utilization", this, m ->
                        (double) sessionContainer.getActiveSessionCount() / sessionContainer.getMaxSessions())
                .description("Session utilization ratio (active/max)")
                .register(meterRegistry);

        this.totalToolExecutionTime = new AtomicLong(0);
        Gauge.builder("selenium.tools.execution.time.total", totalToolExecutionTime, AtomicLong::get)
                .description("Total tool execution time in milliseconds")
                .register(meterRegistry);

        // Initialize timers
        this.sessionCreationTimer = Timer.builder("selenium.session.creation.duration")
                .description("Time to create a browser session")
                .register(meterRegistry);

        this.toolExecutionTimer = Timer.builder("selenium.tool.execution.duration")
                .description("Tool execution duration")
                .register(meterRegistry);

        this.pageLoadTimer = Timer.builder("selenium.page.load.duration")
                .description("Page load duration")
                .register(meterRegistry);

        log.info("Selenium metrics initialized");
    }

    /**
     * Records a session creation event.
     */
    public void recordSessionCreated(String browserType, String deviceType) {
        sessionsCreatedCounter.increment();
        activeSessionsGauge.set(sessionContainer.getActiveSessionCount());

        Counter.builder("selenium.sessions.created.by.browser")
                .tag("browser", browserType)
                .tag("device", deviceType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a session closure event.
     */
    public void recordSessionClosed(String reason) {
        sessionsClosedCounter.increment();
        activeSessionsGauge.set(sessionContainer.getActiveSessionCount());

        Counter.builder("selenium.sessions.closed.by.reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a session creation failure.
     */
    public void recordSessionCreationFailure(String browserType, String reason) {
        sessionCreationFailuresCounter.increment();

        Counter.builder("selenium.sessions.creation.failures.by.browser")
                .tag("browser", browserType)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a tool execution.
     */
    public void recordToolExecution(String toolName, boolean success, long durationMs) {
        toolExecutionsCounter.increment();
        totalToolExecutionTime.addAndGet(durationMs);

        if (!success) {
            toolErrorsCounter.increment();
        }

        Counter.builder("selenium.tools.executions.by.tool")
                .tag("tool", toolName)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder("selenium.tool.execution.duration.by.tool")
                .tag("tool", toolName)
                .register(meterRegistry)
                .record(java.time.Duration.ofMillis(durationMs));
    }

    /**
     * Records a tool error.
     */
    public void recordToolError(String toolName, String errorType) {
        toolErrorsCounter.increment();

        Counter.builder("selenium.tools.errors.by.type")
                .tag("tool", toolName)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records session creation time.
     */
    public Timer.Sample startSessionCreationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopSessionCreationTimer(Timer.Sample sample, String browserType) {
        sample.stop(Timer.builder("selenium.session.creation.duration")
                .tag("browser", browserType)
                .register(meterRegistry));
    }

    /**
     * Records tool execution time.
     */
    public Timer.Sample startToolExecutionTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopToolExecutionTimer(Timer.Sample sample, String toolName) {
        sample.stop(Timer.builder("selenium.tool.execution.duration")
                .tag("tool", toolName)
                .register(meterRegistry));
    }

    /**
     * Updates active session gauge.
     */
    public void updateActiveSessionsGauge() {
        activeSessionsGauge.set(sessionContainer.getActiveSessionCount());
    }

    /**
     * Gets current metrics summary.
     */
    public String getMetricsSummary() {
        return String.format(
                "Sessions: active=%d, max=%d, created=%.0f, closed=%.0f, failures=%.0f | " +
                        "Tools: executions=%.0f, errors=%.0f",
                sessionContainer.getActiveSessionCount(),
                sessionContainer.getMaxSessions(),
                sessionsCreatedCounter.count(),
                sessionsClosedCounter.count(),
                sessionCreationFailuresCounter.count(),
                toolExecutionsCounter.count(),
                toolErrorsCounter.count()
        );
    }
}

