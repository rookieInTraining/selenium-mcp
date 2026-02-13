package com.autom8tr.selenium_mcp.tools.cdp;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NetworkService {

    private static final Logger log = LoggerFactory.getLogger(NetworkService.class);

    private final SeleniumSessionContainer sessionContainer;
    private final Gson gson;

    public NetworkService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Tool(description = "Enables network monitoring via CDP to track network requests.")
    public SeleniumMCPResponse enable_network_cdp(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Maximum total buffer size in bytes. Default: 10000000.") Integer maxTotalBufferSize,
            @ToolParam(description = "Maximum resource buffer size in bytes. Default: 5000000.") Integer maxResourceBufferSize,
            @ToolParam(description = "Maximum post data size in bytes. Default: 65536.") Integer maxPostDataSize,
            @ToolParam(description = "Whether to report direct socket traffic. Default: false.") Boolean reportDirectSocketTraffic
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            if (!(driver instanceof HasDevTools)) {
                return new SeleniumMCPResponse(sessionId, false,
                        "CDP is not supported for this browser. Only Chrome and Edge support CDP.");
            }

            // Set defaults
            int totalBuffer = maxTotalBufferSize != null ? maxTotalBufferSize : 10000000;
            int resourceBuffer = maxResourceBufferSize != null ? maxResourceBufferSize : 5000000;
            int postData = maxPostDataSize != null ? maxPostDataSize : 65536;
            boolean reportSocket = reportDirectSocketTraffic != null && reportDirectSocketTraffic;

            try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
                devTools.createSessionIfThereIsNotOne();

                // Actually send the enable command
                devTools.send(Network.enable(
                        Optional.of(totalBuffer),
                        Optional.of(resourceBuffer),
                        Optional.of(postData),
                        Optional.of(reportSocket)
                ));

                log.info("Enabled CDP network monitoring for session {}", sessionId);
                return new SeleniumMCPResponse(sessionId, true,
                        "CDP Network monitoring enabled with buffers: total=" + totalBuffer +
                                ", resource=" + resourceBuffer + ", postData=" + postData);
            }
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error enabling CDP network for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error enabling CDP: " + e.getMessage());
        }
    }

    @Tool(description = "Disables network monitoring via CDP.")
    public SeleniumMCPResponse disable_network_cdp(
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
                devTools.send(Network.disable());

                log.info("Disabled CDP network monitoring for session {}", sessionId);
                return new SeleniumMCPResponse(sessionId, true, "CDP Network monitoring disabled");
            }
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error disabling CDP network for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error disabling CDP: " + e.getMessage());
        }
    }

    @Tool(description = "Retrieves all cookies from the browser session.")
    public SeleniumMCPResponse get_all_cookies(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            // Use WebDriver's cookie management instead of CDP for better compatibility
            Set<Cookie> cookies = driver.manage().getCookies();

            if (cookies.isEmpty()) {
                return new SeleniumMCPResponse(sessionId, true, "No cookies found");
            }

            List<CookieInfo> cookieInfos = cookies.stream()
                    .map(c -> new CookieInfo(
                            c.getName(),
                            c.getValue(),
                            c.getDomain(),
                            c.getPath(),
                            c.getExpiry() != null ? c.getExpiry().toString() : null,
                            c.isSecure(),
                            c.isHttpOnly()
                    ))
                    .collect(Collectors.toList());

            String cookiesJson = gson.toJson(cookieInfos);
            log.debug("Retrieved {} cookies for session {}", cookies.size(), sessionId);

            return new SeleniumMCPResponse(sessionId, true,
                    "Found " + cookies.size() + " cookie(s):\n" + cookiesJson);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error getting cookies for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting cookies: " + e.getMessage());
        }
    }

    @Tool(description = "Adds a cookie to the browser session.")
    public SeleniumMCPResponse add_cookie(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Cookie name.") String name,
            @ToolParam(description = "Cookie value.") String value,
            @ToolParam(description = "Cookie domain (optional).") String domain,
            @ToolParam(description = "Cookie path (optional, defaults to '/').") String path
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            Cookie.Builder cookieBuilder = new Cookie.Builder(name, value);
            if (domain != null && !domain.isEmpty()) {
                cookieBuilder.domain(domain);
            }
            if (path != null && !path.isEmpty()) {
                cookieBuilder.path(path);
            } else {
                cookieBuilder.path("/");
            }

            driver.manage().addCookie(cookieBuilder.build());

            log.debug("Added cookie '{}' for session {}", name, sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Cookie '" + name + "' added successfully");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error adding cookie for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error adding cookie: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes a specific cookie from the browser session.")
    public SeleniumMCPResponse delete_cookie(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Name of the cookie to delete.") String cookieName
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            driver.manage().deleteCookieNamed(cookieName);

            log.debug("Deleted cookie '{}' for session {}", cookieName, sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Cookie '" + cookieName + "' deleted");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting cookie for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error deleting cookie: " + e.getMessage());
        }
    }

    @Tool(description = "Deletes all cookies from the browser session.")
    public SeleniumMCPResponse delete_all_cookies(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            driver.manage().deleteAllCookies();

            log.debug("Deleted all cookies for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "All cookies deleted");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting all cookies for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error deleting cookies: " + e.getMessage());
        }
    }

    @Tool(description = "Clears browser cache and cookies.")
    public SeleniumMCPResponse clear_browser_data(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);

            // Clear cookies
            driver.manage().deleteAllCookies();

            // Clear local storage and session storage via JavaScript
            if (driver instanceof org.openqa.selenium.JavascriptExecutor) {
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                try {
                    js.executeScript("window.localStorage.clear();");
                    js.executeScript("window.sessionStorage.clear();");
                } catch (Exception e) {
                    log.warn("Could not clear storage for session {}: {}", sessionId, e.getMessage());
                }
            }

            log.info("Cleared browser data for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Browser data cleared (cookies, localStorage, sessionStorage)");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error clearing browser data for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error clearing browser data: " + e.getMessage());
        }
    }

    /**
     * Helper record for cookie info serialization.
     */
    private record CookieInfo(
            String name,
            String value,
            String domain,
            String path,
            String expiry,
            boolean secure,
            boolean httpOnly
    ) {}
}
