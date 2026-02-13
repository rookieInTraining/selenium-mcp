package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PointerActionsService {

    private static final Logger log = LoggerFactory.getLogger(PointerActionsService.class);

    private final SeleniumSessionContainer sessionContainer;

    public PointerActionsService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Tool(description = "Performs a left click on an element. Waits for element to be clickable.")
    public SeleniumMCPResponse left_click(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            
            // Wait for element to be clickable
            org.openqa.selenium.support.ui.WebDriverWait wait = 
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(locator));
            
            element.click();

            log.debug("Left clicked element for session {}: {}={}", sessionId, locatorStrategy, locatorValue);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully clicked element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.TimeoutException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found or not clickable within 10 seconds with " + locatorStrategy + "='" + locatorValue + "'. The element may be hidden, disabled, or still loading.");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            log.warn("Click intercepted for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, false,
                    "Element click was intercepted by another element. Try scrolling to the element first or using js_click instead.");
        } catch (Exception e) {
            log.error("Error clicking element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error clicking element: " + e.getMessage());
        }
    }

    @Tool(description = "Performs a double click on an element.")
    public SeleniumMCPResponse double_click(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            Actions actions = new Actions(driver);
            actions.doubleClick(element).perform();

            log.debug("Double clicked element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully double-clicked element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error double-clicking element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error double-clicking element: " + e.getMessage());
        }
    }

    @Tool(description = "Performs a right click (context menu) on an element.")
    public SeleniumMCPResponse right_click(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            Actions actions = new Actions(driver);
            actions.contextClick(element).perform();

            log.debug("Right clicked element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully right-clicked element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error right-clicking element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error right-clicking element: " + e.getMessage());
        }
    }

    @Tool(description = "Hovers the mouse over an element.")
    public SeleniumMCPResponse hover(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();

            log.debug("Hovered over element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully hovered over element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error hovering over element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error hovering over element: " + e.getMessage());
        }
    }

    @Tool(description = "Drags an element and drops it onto another element.")
    public SeleniumMCPResponse drag_and_drop(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy for source element.") String sourceLocatorStrategy,
            @ToolParam(description = "Locator value for source element.") String sourceLocatorValue,
            @ToolParam(description = "Locator strategy for target element.") String targetLocatorStrategy,
            @ToolParam(description = "Locator value for target element.") String targetLocatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By sourceLocator = SeleniumLocatorBuilder.getLocator(sourceLocatorStrategy, sourceLocatorValue);
            By targetLocator = SeleniumLocatorBuilder.getLocator(targetLocatorStrategy, targetLocatorValue);

            WebElement sourceElement = driver.findElement(sourceLocator);
            WebElement targetElement = driver.findElement(targetLocator);

            Actions actions = new Actions(driver);
            actions.dragAndDrop(sourceElement, targetElement).perform();

            log.debug("Drag and drop performed for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Successfully performed drag and drop");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false, "Source or target element not found");
        } catch (Exception e) {
            log.error("Error performing drag and drop for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error performing drag and drop: " + e.getMessage());
        }
    }

    @Tool(description = "Scrolls to bring an element into view.")
    public SeleniumMCPResponse scroll_to_element(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            Actions actions = new Actions(driver);
            actions.scrollToElement(element).perform();

            log.debug("Scrolled to element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully scrolled to element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error scrolling to element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error scrolling to element: " + e.getMessage());
        }
    }
}
