package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.lenium.AbstractWebDriverFactory;
import com.autom8tr.selenium_mcp.lenium.WebDrivers;
import org.openqa.selenium.WebDriver;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.sessions;

@Service
public class WebDriverService {

    @Tool(description = "Creates a new session of the browser")
    public SeleniumMCPResponse browser_init(
            @ToolParam(description = "The device for which the driver needs to be created. Would be either desktop or mobile. Default will be desktop") String driverType,
            @ToolParam(description = "The type of webdriver to be instantiated. Will be browser or mobile device like Android or iOS. Default value would be edge") String device
    ) {
        try {
            UUID sessionId = UUID.randomUUID();
            WebDrivers deviceDriver = AbstractWebDriverFactory.getDeviceDriver(driverType);
            WebDriver driver = deviceDriver.getDriver(device);
            sessions.put(sessionId, driver);
            return new SeleniumMCPResponse(true, "Initialized browser session: " + sessionId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Tool(description = "Instructs the browser to go to a page")
    public SeleniumMCPResponse go_to(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam(description = "The value of the url to browse to") String url
    ) {
        try {
            WebDriver driver = (WebDriver) SeleniumSessionContainer.getDriver(sessionId);
            driver.navigate().to(url);
            return new SeleniumMCPResponse(true, "Navigated to: " + url);
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Navigation failed: " + e.getMessage());
        }
    }

    @Tool(description = "Fetch's the page source of the current navigated page")
    public SeleniumMCPResponse get_page_source(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        try {
            WebDriver driver = (WebDriver) SeleniumSessionContainer.getDriver(sessionId);
            String ps = driver.getPageSource();
            return new SeleniumMCPResponse(true, ps);
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to fetch the page source");
        }
    }

    @Tool(description = "Instructs the browser to exit the current session")
    public SeleniumMCPResponse quit_browser(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        try {
            WebDriver driver = (WebDriver) SeleniumSessionContainer.getDriver(sessionId);
            driver.quit();
            return new SeleniumMCPResponse(true, "Successfully exited the browser");
        } catch (Exception e) {
            System.err.println(e);
            return new SeleniumMCPResponse(false, "Unable to close the session or Session not found");
        }
    }

}
