package com.autom8tr.selenium_mcp.tools.cdp;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v138.browser.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BrowserService {

    private static final Logger log = LoggerFactory.getLogger(BrowserService.class);

    private final SeleniumSessionContainer sessionContainer;
    private final Gson gson;

    public BrowserService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Tool(description = "Fetches browser version information using Chrome DevTools Protocol.")
    public SeleniumMCPResponse get_browser_version(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            if (!(driver instanceof HasDevTools)) {
                return new SeleniumMCPResponse(sessionId, false,
                        "CDP is not supported for this browser. Only Chrome and Edge support CDP.");
            }

            try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
                devTools.createSessionIfThereIsNotOne();
                Browser.GetVersionResponse versionResponse = devTools.send(Browser.getVersion());

                String versionJson = gson.toJson(new BrowserVersionInfo(
                        versionResponse.getProtocolVersion(),
                        versionResponse.getProduct(),
                        versionResponse.getRevision(),
                        versionResponse.getUserAgent(),
                        versionResponse.getJsVersion()
                ));

                log.debug("Retrieved browser version for session {}", sessionId);
                return new SeleniumMCPResponse(sessionId, true, "Browser Details:\n" + versionJson);
            }
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error getting browser version for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting browser version: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the browser window bounds (position and size).")
    public SeleniumMCPResponse get_window_bounds(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            if (!(driver instanceof HasDevTools)) {
                return new SeleniumMCPResponse(sessionId, false,
                        "CDP is not supported for this browser.");
            }

            try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
                devTools.createSessionIfThereIsNotOne();

                // Use regular WebDriver API for window bounds
                org.openqa.selenium.Dimension size = driver.manage().window().getSize();
                org.openqa.selenium.Point position = driver.manage().window().getPosition();

                String boundsInfo = String.format(
                        "Window bounds:\n  Position: x=%d, y=%d\n  Size: width=%d, height=%d",
                        position.getX(), position.getY(), size.getWidth(), size.getHeight());

                return new SeleniumMCPResponse(sessionId, true, boundsInfo);
            }
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error getting window bounds for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting window bounds: " + e.getMessage());
        }
    }

    @Tool(description = "Sets the browser window size.")
    public SeleniumMCPResponse set_window_size(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Window width in pixels.") int width,
            @ToolParam(description = "Window height in pixels.") int height
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));

            log.debug("Set window size for session {}: {}x{}", sessionId, width, height);
            return new SeleniumMCPResponse(sessionId, true,
                    "Window size set to " + width + "x" + height);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error setting window size for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error setting window size: " + e.getMessage());
        }
    }

    @Tool(description = "Maximizes the browser window.")
    public SeleniumMCPResponse maximize_window(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            driver.manage().window().maximize();

            log.debug("Maximized window for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Window maximized");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error maximizing window for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error maximizing window: " + e.getMessage());
        }
    }

    @Tool(description = "Takes a screenshot of the current page and returns it as base64.")
    public SeleniumMCPResponse take_screenshot(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            if (driver instanceof org.openqa.selenium.TakesScreenshot) {
                String screenshot = ((org.openqa.selenium.TakesScreenshot) driver)
                        .getScreenshotAs(org.openqa.selenium.OutputType.BASE64);

                log.debug("Took screenshot for session {}", sessionId);
                return new SeleniumMCPResponse(sessionId, true,
                        "Screenshot (base64):\n" + screenshot);
            } else {
                return new SeleniumMCPResponse(sessionId, false,
                        "Screenshot capability not supported for this browser.");
            }
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error taking screenshot for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error taking screenshot: " + e.getMessage());
        }
    }

    /**
     * Helper record for browser version info serialization.
     */
    private record BrowserVersionInfo(
            String protocolVersion,
            String product,
            String revision,
            String userAgent,
            String jsVersion
    ) {}
}
