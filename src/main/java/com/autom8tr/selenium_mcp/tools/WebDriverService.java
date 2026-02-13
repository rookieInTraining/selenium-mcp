package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.core.SessionMetadata;
import com.autom8tr.selenium_mcp.exception.SessionException;
import com.autom8tr.selenium_mcp.sulphur.AbstractWebDriverFactory;
import com.autom8tr.selenium_mcp.sulphur.WebDrivers;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebDriverService {

    private static final Logger log = LoggerFactory.getLogger(WebDriverService.class);

    private final SeleniumSessionContainer sessionContainer;

    public WebDriverService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Tool(description = "Creates a new browser session. Returns a session ID that must be used for all subsequent operations.")
    public SeleniumMCPResponse browser_init(
            @ToolParam(description = "The device type: 'desktop' or 'mobile'. Default is 'desktop'.") String deviceType,
            @ToolParam(description = "The browser type: 'chrome', 'firefox', 'edge'. Default is 'chrome'.") String browserType
    ) {
        try {
            // Validate and set defaults
            if (deviceType == null || deviceType.isBlank()) {
                deviceType = "desktop";
            }
            if (browserType == null || browserType.isBlank()) {
                browserType = "chrome";
            }

            log.info("Initializing browser session: device={}, browser={}", deviceType, browserType);

            UUID sessionId = UUID.randomUUID();
            WebDrivers deviceDriver = AbstractWebDriverFactory.getDeviceDriver(deviceType);
            WebDriver driver = deviceDriver.getDriver(browserType);

            sessionContainer.createSession(sessionId, driver, browserType, deviceType);

            log.info("Browser session initialized successfully: {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Initialized browser session: " + sessionId + " (browser: " + browserType + ", device: " + deviceType + "). IMPORTANT: Use this session ID for all subsequent operations.");

        } catch (SessionException e) {
            log.warn("Session exception: {}", e.getMessage());
            return new SeleniumMCPResponse(false, "Session error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to initialize browser session", e);
            return new SeleniumMCPResponse(false, "Failed to initialize browser: " + e.getMessage());
        }
    }

    @Tool(description = "Lists all active browser sessions with their metadata. Use this BEFORE creating a new session to check if one already exists.")
    public SeleniumMCPResponse get_sessions() {
        try {
            List<SessionMetadata> metadata = sessionContainer.getAllSessionMetadata();
            
            if (metadata.isEmpty()) {
                return new SeleniumMCPResponse(true,
                        "Active sessions: 0/" + sessionContainer.getMaxSessions() + "\nNo active sessions found. You should create a new session using browser_init().");
            }
            
            String sessionInfo = metadata.stream()
                    .map(m -> String.format("Session %s: browser=%s, device=%s, state=%s, age=%ds, idle=%ds, currentUrl=%s",
                            m.getSessionId(), m.getBrowserType(), m.getDeviceType(),
                            m.getState(), m.getAgeInSeconds(), m.getIdleTimeInSeconds(),
                            m.getCurrentUrl() != null ? m.getCurrentUrl() : "none"))
                    .collect(Collectors.joining("\n"));

            return new SeleniumMCPResponse(true,
                    "Active sessions: " + sessionContainer.getActiveSessionCount() + "/" + sessionContainer.getMaxSessions() + "\n" + 
                    sessionInfo + "\n\nIMPORTANT: Reuse an existing session ID for subsequent operations instead of creating a new session.");
        } catch (Exception e) {
            log.error("Failed to get sessions", e);
            return new SeleniumMCPResponse(false, "Failed to get sessions: " + e.getMessage());
        }
    }
    
    @Tool(description = "Checks if a specific session exists and is active. Returns detailed session information if found.")
    public SeleniumMCPResponse check_session(
            @ToolParam(description = "The session ID to check.") UUID sessionId
    ) {
        try {
            java.util.Optional<SessionMetadata> metadata = sessionContainer.getSessionMetadata(sessionId);
            
            if (metadata.isEmpty()) {
                return new SeleniumMCPResponse(sessionId, false, 
                        "Session " + sessionId + " does not exist. Create a new session or use get_sessions() to find active sessions.");
            }
            
            SessionMetadata m = metadata.get();
            String info = String.format(
                    "Session %s is ACTIVE\n" +
                    "Browser: %s\n" +
                    "Device: %s\n" +
                    "State: %s\n" +
                    "Age: %d seconds\n" +
                    "Idle: %d seconds\n" +
                    "Current URL: %s",
                    m.getSessionId(), m.getBrowserType(), m.getDeviceType(),
                    m.getState(), m.getAgeInSeconds(), m.getIdleTimeInSeconds(),
                    m.getCurrentUrl() != null ? m.getCurrentUrl() : "none"
            );
            
            return new SeleniumMCPResponse(sessionId, true, info);
        } catch (Exception e) {
            log.error("Failed to check session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Failed to check session: " + e.getMessage());
        }
    }

    @Tool(description = "Navigates the browser to a specified URL and waits for page load.")
    public SeleniumMCPResponse go_to(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "The URL to navigate to.") String url
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            driver.get(url); // Use driver.get() instead of navigate().to() - it waits for page load
            sessionContainer.updateSessionUrl(sessionId, url);

            // Additional wait for document ready state
            try {
                org.openqa.selenium.support.ui.WebDriverWait wait = 
                    new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30));
                wait.until(webDriver -> 
                    ((org.openqa.selenium.JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
            } catch (Exception waitException) {
                log.debug("Document ready state wait timed out for session {}", sessionId);
            }

            log.info("Session {} navigated to: {}", sessionId, url);
            return new SeleniumMCPResponse(sessionId, true, "Navigated to: " + url);
        } catch (SessionException e) {
            log.warn("Session not found: {}", sessionId);
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Navigation failed for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Navigation failed: " + e.getMessage());
        }
    }

    @Tool(description = "Retrieves the page source HTML of the current page.")
    public SeleniumMCPResponse get_page_source(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            String pageSource = driver.getPageSource();

            log.debug("Retrieved page source for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, pageSource);
        } catch (SessionException e) {
            log.warn("Session not found: {}", sessionId);
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get page source for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Unable to fetch the page source: " + e.getMessage());
        }
    }

    @Tool(description = "Closes the browser session and releases resources.")
    public SeleniumMCPResponse quit_browser(
            @ToolParam(description = "The session ID of the browser instance to close.") UUID sessionId
    ) {
        try {
            boolean removed = sessionContainer.removeSession(sessionId);
            if (removed) {
                log.info("Browser session closed: {}", sessionId);
                return new SeleniumMCPResponse(sessionId, true, "Successfully closed browser session: " + sessionId);
            } else {
                log.warn("Session not found for closure: {}", sessionId);
                return new SeleniumMCPResponse(sessionId, false, "Session not found: " + sessionId);
            }
        } catch (Exception e) {
            log.error("Failed to close browser session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Failed to close session: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the current URL of the browser.")
    public SeleniumMCPResponse get_current_url(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            String currentUrl = driver.getCurrentUrl();

            return new SeleniumMCPResponse(sessionId, true, "Current URL: " + currentUrl);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get current URL for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Failed to get current URL: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the title of the current page.")
    public SeleniumMCPResponse get_page_title(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            String title = driver.getTitle();

            return new SeleniumMCPResponse(sessionId, true, "Page title: " + title);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get page title for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Failed to get page title: " + e.getMessage());
        }
    }
}
