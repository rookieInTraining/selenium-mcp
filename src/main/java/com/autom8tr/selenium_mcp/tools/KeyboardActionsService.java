package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
public class KeyboardActionsService {

    private static final Logger log = LoggerFactory.getLogger(KeyboardActionsService.class);

    private final SeleniumSessionContainer sessionContainer;

    public KeyboardActionsService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Tool(description = "Sends text input to a web element. Waits for element to be visible and clickable.")
    public SeleniumMCPResponse send_keys_to_element(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue,
            @ToolParam(description = "The text to send to the element.") String textToSend
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            
            // Wait for element to be clickable (visible and enabled)
            org.openqa.selenium.support.ui.WebDriverWait wait = 
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(locator));
            
            element.sendKeys(textToSend);

            log.debug("Sent keys to element for session {}: {}={}", sessionId, locatorStrategy, locatorValue);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully sent text to element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.TimeoutException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found or not interactable within 10 seconds with " + locatorStrategy + "='" + locatorValue + "'. The element may be hidden or disabled.");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error sending keys to element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error sending keys: " + e.getMessage());
        }
    }

    @Tool(description = "Clears the content of an input element and optionally types new text.")
    public SeleniumMCPResponse clear_and_type(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue,
            @ToolParam(description = "The text to type after clearing. Pass empty string to just clear.") String textToType
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            element.clear();
            if (textToType != null && !textToType.isEmpty()) {
                element.sendKeys(textToType);
            }

            log.debug("Cleared and typed in element for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully cleared and typed in element with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error clearing and typing for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error clearing and typing: " + e.getMessage());
        }
    }

    @Tool(description = "Presses a special key (Enter, Tab, Escape, etc.) on an element or globally.")
    public SeleniumMCPResponse press_key(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "The key to press: 'ENTER', 'TAB', 'ESCAPE', 'BACKSPACE', 'DELETE', 'ARROW_UP', 'ARROW_DOWN', 'ARROW_LEFT', 'ARROW_RIGHT', 'F1'-'F12', 'HOME', 'END', 'PAGE_UP', 'PAGE_DOWN'.") String keyName,
            @ToolParam(description = "Optional locator strategy. If not provided, key is pressed globally.") String locatorStrategy,
            @ToolParam(description = "Optional locator value. Required if locatorStrategy is provided.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            Keys key = resolveKey(keyName);

            if (key == null) {
                return new SeleniumMCPResponse(sessionId, false, "Unknown key: " + keyName);
            }

            if (locatorStrategy != null && !locatorStrategy.isEmpty() &&
                    locatorValue != null && !locatorValue.isEmpty()) {
                // Press key on specific element
                By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
                WebElement element = driver.findElement(locator);
                element.sendKeys(key);
            } else {
                // Press key globally using Actions
                Actions actions = new Actions(driver);
                actions.sendKeys(key).perform();
            }

            log.debug("Pressed key {} for session {}", keyName, sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Successfully pressed key: " + keyName);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error pressing key for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error pressing key: " + e.getMessage());
        }
    }

    @Tool(description = "Performs a keyboard shortcut (e.g., Ctrl+C, Ctrl+V, Ctrl+A).")
    public SeleniumMCPResponse keyboard_shortcut(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Modifier key: 'CTRL', 'ALT', 'SHIFT', 'COMMAND' (for Mac).") String modifier,
            @ToolParam(description = "The key to combine with the modifier (e.g., 'a', 'c', 'v', 'x', 'z').") String key
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            Keys modifierKey = resolveModifier(modifier);

            if (modifierKey == null) {
                return new SeleniumMCPResponse(sessionId, false, "Unknown modifier: " + modifier);
            }

            Actions actions = new Actions(driver);
            actions.keyDown(modifierKey)
                    .sendKeys(key.toLowerCase())
                    .keyUp(modifierKey)
                    .perform();

            log.debug("Performed keyboard shortcut {}+{} for session {}", modifier, key, sessionId);
            return new SeleniumMCPResponse(sessionId, true,
                    "Successfully performed shortcut: " + modifier + "+" + key);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error performing keyboard shortcut for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error performing shortcut: " + e.getMessage());
        }
    }

    @Tool(description = "Submits a form containing the specified element.")
    public SeleniumMCPResponse submit_form(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find an element within the form.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);
            element.submit();

            log.debug("Submitted form for session {}", sessionId);
            return new SeleniumMCPResponse(sessionId, true, "Successfully submitted form");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error submitting form for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error submitting form: " + e.getMessage());
        }
    }

    private Keys resolveKey(String keyName) {
        if (keyName == null) return null;

        return switch (keyName.toUpperCase()) {
            case "ENTER", "RETURN" -> Keys.ENTER;
            case "TAB" -> Keys.TAB;
            case "ESCAPE", "ESC" -> Keys.ESCAPE;
            case "BACKSPACE" -> Keys.BACK_SPACE;
            case "DELETE" -> Keys.DELETE;
            case "SPACE" -> Keys.SPACE;
            case "ARROW_UP", "UP" -> Keys.ARROW_UP;
            case "ARROW_DOWN", "DOWN" -> Keys.ARROW_DOWN;
            case "ARROW_LEFT", "LEFT" -> Keys.ARROW_LEFT;
            case "ARROW_RIGHT", "RIGHT" -> Keys.ARROW_RIGHT;
            case "HOME" -> Keys.HOME;
            case "END" -> Keys.END;
            case "PAGE_UP" -> Keys.PAGE_UP;
            case "PAGE_DOWN" -> Keys.PAGE_DOWN;
            case "F1" -> Keys.F1;
            case "F2" -> Keys.F2;
            case "F3" -> Keys.F3;
            case "F4" -> Keys.F4;
            case "F5" -> Keys.F5;
            case "F6" -> Keys.F6;
            case "F7" -> Keys.F7;
            case "F8" -> Keys.F8;
            case "F9" -> Keys.F9;
            case "F10" -> Keys.F10;
            case "F11" -> Keys.F11;
            case "F12" -> Keys.F12;
            default -> null;
        };
    }

    private Keys resolveModifier(String modifier) {
        if (modifier == null) return null;

        return switch (modifier.toUpperCase()) {
            case "CTRL", "CONTROL" -> Keys.CONTROL;
            case "ALT" -> Keys.ALT;
            case "SHIFT" -> Keys.SHIFT;
            case "COMMAND", "META", "CMD" -> Keys.COMMAND;
            default -> null;
        };
    }
}
