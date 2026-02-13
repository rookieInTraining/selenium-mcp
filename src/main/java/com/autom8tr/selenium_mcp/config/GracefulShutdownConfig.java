package com.autom8tr.selenium_mcp.config;

import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Handles graceful shutdown of the application.
 * Ensures all WebDriver sessions are properly closed before shutdown.
 */
@Component
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownConfig.class);

    private final SeleniumSessionContainer sessionContainer;
    private final SessionProperties sessionProperties;

    // Shutdown timeout in seconds
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    public GracefulShutdownConfig(SeleniumSessionContainer sessionContainer, SessionProperties sessionProperties) {
        this.sessionContainer = sessionContainer;
        this.sessionProperties = sessionProperties;

        // Register shutdown hook as fallback
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered - cleaning up sessions");
            performShutdown();
        }, "selenium-shutdown-hook"));

        log.info("Graceful shutdown handler initialized");
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Application context closing - initiating graceful shutdown");
        performShutdown();
    }

    private void performShutdown() {
        int activeSessions = sessionContainer.getActiveSessionCount();

        if (activeSessions == 0) {
            log.info("No active sessions to close");
            return;
        }

        log.info("Starting graceful shutdown with {} active sessions", activeSessions);

        try {
            // Close all sessions with timeout
            CountDownLatch shutdownLatch = new CountDownLatch(1);

            Thread shutdownThread = new Thread(() -> {
                try {
                    int closed = sessionContainer.closeAllSessions();
                    log.info("Graceful shutdown completed. Closed {} sessions.", closed);
                } finally {
                    shutdownLatch.countDown();
                }
            }, "session-shutdown-thread");

            shutdownThread.start();

            // Wait for shutdown to complete or timeout
            boolean completed = shutdownLatch.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                log.warn("Graceful shutdown timed out after {} seconds. Some sessions may not be properly closed.",
                        SHUTDOWN_TIMEOUT_SECONDS);
                shutdownThread.interrupt();
            }

        } catch (InterruptedException e) {
            log.warn("Graceful shutdown interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error during graceful shutdown", e);
        }
    }
}

