package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JavaScriptService {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptService.class);

    private final SeleniumSessionContainer sessionContainer;

    public JavaScriptService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Tool(description = "Executes a synchronous JavaScript script on the page.")
    public SeleniumMCPResponse js_execute_sync(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "The JavaScript code to execute.") String script
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            Object result = ((JavascriptExecutor) driver).executeScript(script);

            String resultStr = result != null ? result.toString() : "null";
            log.debug("Executed sync JS for session {}: result length = {}", sessionId, resultStr.length());

            return new SeleniumMCPResponse(sessionId, true, "Script executed. Result: " + truncate(resultStr, 5000));
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error executing sync JS for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "JavaScript error: " + e.getMessage());
        }
    }

    @Tool(description = "Executes an asynchronous JavaScript script on the page.")
    public SeleniumMCPResponse js_execute_async(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "The JavaScript code to execute. Must call the callback function when done.") String script
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            Object result = ((JavascriptExecutor) driver).executeAsyncScript(script);

            String resultStr = result != null ? result.toString() : "null";
            log.debug("Executed async JS for session {}", sessionId);

            return new SeleniumMCPResponse(sessionId, true, "Async script executed. Result: " + truncate(resultStr, 5000));
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error executing async JS for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "JavaScript error: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the full HTML content of the current page.")
    public SeleniumMCPResponse get_page_html(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            String htmlPage = (String) ((JavascriptExecutor) driver)
                    .executeScript("return document.documentElement.outerHTML");

            log.debug("Retrieved page HTML for session {}: {} chars", sessionId, htmlPage.length());
            return new SeleniumMCPResponse(sessionId, true, htmlPage);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error getting page HTML for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting page HTML: " + e.getMessage());
        }
    }

    @Tool(description = "Scrolls the page by a specified amount in pixels.")
    public SeleniumMCPResponse scroll_page(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Horizontal scroll amount in pixels. Positive = right, negative = left.") int x,
            @ToolParam(description = "Vertical scroll amount in pixels. Positive = down, negative = up.") int y
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1])", x, y);

            log.debug("Scrolled page for session {}: x={}, y={}", sessionId, x, y);
            return new SeleniumMCPResponse(sessionId, true, "Scrolled page by (" + x + ", " + y + ") pixels");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error scrolling page for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error scrolling page: " + e.getMessage());
        }
    }

    @Tool(description = "Scrolls to the top of the page.")
    public SeleniumMCPResponse scroll_to_top(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");

            log.debug("Scrolled to top for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Scrolled to top of page");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error scrolling to top for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error scrolling to top: " + e.getMessage());
        }
    }

    @Tool(description = "Scrolls to the bottom of the page.")
    public SeleniumMCPResponse scroll_to_bottom(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

            log.debug("Scrolled to bottom for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Scrolled to bottom of page");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error scrolling to bottom for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error scrolling to bottom: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the current scroll position of the page.")
    public SeleniumMCPResponse get_scroll_position(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            Long scrollX = (Long) ((JavascriptExecutor) driver).executeScript("return window.pageXOffset");
            Long scrollY = (Long) ((JavascriptExecutor) driver).executeScript("return window.pageYOffset");

            return new SeleniumMCPResponse(sessionId, true,
                    "Scroll position: x=" + scrollX + ", y=" + scrollY);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error getting scroll position for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting scroll position: " + e.getMessage());
        }
    }

    @Tool(description = "Clicks an element using JavaScript (useful when regular click doesn't work).")
    public SeleniumMCPResponse js_click(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);

            log.debug("JS clicked element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully clicked element via JavaScript with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error JS clicking element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error clicking element: " + e.getMessage());
        }
    }

    @Tool(description = "Sets a value on an input element using JavaScript.")
    public SeleniumMCPResponse js_set_value(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue,
            @ToolParam(description = "The value to set on the element.") String value
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]", element, value);

            log.debug("JS set value for element in session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully set value via JavaScript on element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error JS setting value for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error setting value: " + e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "null";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "... (truncated, total length: " + str.length() + ")";
    }
}
