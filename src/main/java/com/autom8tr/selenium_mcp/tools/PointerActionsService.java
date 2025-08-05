package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.getDriver;

@Service
public class PointerActionsService {

    @Tool(description = "Click an element on the browser page")
    public SeleniumMCPResponse left_click(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam(description = "The locator strategy to be used in order to fetch the WebElement") String locatorStrategy,
            @ToolParam(description = "The WebElement to which the input needs to be sent") String webElement
    ) {
        try {
            WebDriver driver = (WebDriver) getDriver(sessionId);
            By inputElement = SeleniumLocatorBuilder.getLocator(locatorStrategy, webElement);
            WebElement located_input_element = driver.findElement(inputElement);
            located_input_element.click();

            return new SeleniumMCPResponse(sessionId, true, "Successfully clicked the WebElement : " + located_input_element);
        } catch (Exception e) {
            return new SeleniumMCPResponse(sessionId, false, "Unable to click the expected element. Cause : " + e);
        }
    }

}
