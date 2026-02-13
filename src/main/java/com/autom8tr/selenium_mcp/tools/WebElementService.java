package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import com.autom8tr.selenium_mcp.core.SeleniumSessionContainer;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebElementService {

    private static final Logger log = LoggerFactory.getLogger(WebElementService.class);

    private final SeleniumSessionContainer sessionContainer;

    public WebElementService(SeleniumSessionContainer sessionContainer) {
        this.sessionContainer = sessionContainer;
    }

    @Tool(description = "Finds a single element on the page using the specified locator strategy. Waits up to 10 seconds for element to be present.")
    public SeleniumMCPResponse find_element(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            
            // Wait for element to be present
            org.openqa.selenium.support.ui.WebDriverWait wait = 
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
            WebElement element = wait.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(locator));

            String elementInfo = formatElementInfo(element);
            log.debug("Found element for session {}: {}", sessionId, elementInfo);

            return new SeleniumMCPResponse(sessionId, true, "Element found: " + elementInfo);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.TimeoutException e) {
            log.debug("Element not found within 10 seconds for session {} with {}={}", sessionId, locatorStrategy, locatorValue);
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found within 10 seconds with " + locatorStrategy + "='" + locatorValue + "'. The page may still be loading or the locator may be incorrect.");
        } catch (org.openqa.selenium.NoSuchElementException e) {
            log.debug("Element not found for session {} with {}={}", sessionId, locatorStrategy, locatorValue);
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error finding element for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error finding element: " + e.getMessage());
        }
    }

    @Tool(description = "Finds all elements on the page matching the specified locator.")
    public SeleniumMCPResponse find_elements(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find elements.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            List<WebElement> elements = driver.findElements(locator);

            if (elements.isEmpty()) {
                return new SeleniumMCPResponse(sessionId, true,
                        "No elements found with " + locatorStrategy + "='" + locatorValue + "'");
            }

            String elementsInfo = elements.stream()
                    .limit(50) // Limit output for large result sets
                    .map(this::formatElementInfo)
                    .collect(Collectors.joining("\n"));

            String result = "Found " + elements.size() + " element(s):\n" + elementsInfo;
            if (elements.size() > 50) {
                result += "\n... and " + (elements.size() - 50) + " more";
            }

            log.debug("Found {} elements for session {}", elements.size(), sessionId);
            return new SeleniumMCPResponse(sessionId, true, result);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (Exception e) {
            log.error("Error finding elements for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error finding elements: " + e.getMessage());
        }
    }

    @Tool(description = "Gets an attribute value from an element.")
    public SeleniumMCPResponse get_attribute(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue,
            @ToolParam(description = "Name of the attribute to retrieve.") String attributeName
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            String attributeValue = element.getAttribute(attributeName);

            if (attributeValue == null) {
                return new SeleniumMCPResponse(sessionId, true,
                        "Attribute '" + attributeName + "' not found on element");
            }

            return new SeleniumMCPResponse(sessionId, true,
                    "Attribute '" + attributeName + "' = '" + attributeValue + "'");
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error getting attribute for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting attribute: " + e.getMessage());
        }
    }

    @Tool(description = "Gets the visible text content of an element.")
    public SeleniumMCPResponse get_text(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            String text = element.getText();

            return new SeleniumMCPResponse(sessionId, true, "Element text: " + text);
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error getting text for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error getting text: " + e.getMessage());
        }
    }

    @Tool(description = "Checks if an element is displayed/visible on the page.")
    public SeleniumMCPResponse is_element_displayed(
            @ToolParam(description = "The session ID of the browser instance.") UUID sessionId,
            @ToolParam(description = "Locator strategy: 'id', 'xpath', 'css', 'name', 'tag', 'link', 'class'.") String locatorStrategy,
            @ToolParam(description = "The locator value to find the element.") String locatorValue
    ) {
        try {
            WebDriver driver = sessionContainer.getDriver(sessionId);
            By locator = SeleniumLocatorBuilder.getLocator(locatorStrategy, locatorValue);
            WebElement element = driver.findElement(locator);

            boolean displayed = element.isDisplayed();

            return new SeleniumMCPResponse(sessionId, true,
                    "Element is " + (displayed ? "displayed" : "not displayed"));
        } catch (SessionException e) {
            return new SeleniumMCPResponse(sessionId, false, e.getMessage());
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return new SeleniumMCPResponse(sessionId, false,
                    "Element not found with " + locatorStrategy + "='" + locatorValue + "'");
        } catch (Exception e) {
            log.error("Error checking element display for session {}", sessionId, e);
            return new SeleniumMCPResponse(sessionId, false, "Error checking element: " + e.getMessage());
        }
    }

    private String formatElementInfo(WebElement element) {
        try {
            String tagName = element.getTagName();
            String id = element.getAttribute("id");
            String className = element.getAttribute("class");
            String text = element.getText();

            StringBuilder info = new StringBuilder("<" + tagName);
            if (id != null && !id.isEmpty()) {
                info.append(" id='").append(id).append("'");
            }
            if (className != null && !className.isEmpty()) {
                info.append(" class='").append(truncate(className, 50)).append("'");
            }
            info.append(">");

            if (text != null && !text.isEmpty()) {
                info.append(" text='").append(truncate(text, 100)).append("'");
            }

            return info.toString();
        } catch (Exception e) {
            return element.toString();
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
