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
public class KeyboardActionsService {

    @Tool(description = "Sends text input to the browser")
    public SeleniumMCPResponse send_keys_to_element(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam(description = "The locator strategy to be used in order to fetch the WebElement") String locatorStrategy,
            @ToolParam(description = "The WebElement to which the input needs to be sent") String webElement,
            @ToolParam(description = "The text which needs to be sent to the WebElement") String textToSend
    ) {
        try {
            WebDriver driver = (WebDriver) getDriver(sessionId);
            By inputElement = SeleniumLocatorBuilder.getLocator(locatorStrategy, webElement);
            WebElement located_input_element = driver.findElement(inputElement);
            located_input_element.sendKeys(textToSend);

            return new SeleniumMCPResponse(true, "Successfully entered the text into the webElement : " + located_input_element);
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to find the expected element. Cause : " + e);
        }
    }

}
