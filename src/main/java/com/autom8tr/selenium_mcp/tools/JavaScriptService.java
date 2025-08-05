package com.autom8tr.selenium_mcp.tools;

import com.autom8tr.selenium_mcp.core.SeleniumLocatorBuilder;
import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.getDriver;

@Service
public class JavaScriptService {

    @Tool(description = "Executes a javascript on the page")
    public SeleniumMCPResponse js_execute_sync(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam(description = "The javascript snippet to be executed on the page") String script
    ) {
        try {
            WebDriver driver = (WebDriver) getDriver(sessionId);
            Object result = ((JavascriptExecutor) driver).executeScript(script);

            return new SeleniumMCPResponse(sessionId, true, "Successfully executed js with result : " + result);
        } catch (Exception e) {
            return new SeleniumMCPResponse(sessionId, false, "Unable to execute the js script. Cause : " + e);
        }
    }

    @Tool(description = "Sends text input to the browser")
    public SeleniumMCPResponse js_execute_async(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam(description = "The javascript snippet to be executed on the page") String script
    ) {
        try {
            WebDriver driver = (WebDriver) getDriver(sessionId);
            Object result = ((JavascriptExecutor) driver).executeAsyncScript(script);

            return new SeleniumMCPResponse(sessionId, true, "Successfully executed js with result : " + result);
        } catch (Exception e) {
            return new SeleniumMCPResponse(sessionId, false, "Unable to execute the js script. Cause : " + e);
        }
    }

    @Tool(description = "Sends text input to the browser")
    public SeleniumMCPResponse get_page_html(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        try {
            WebDriver driver = (WebDriver) getDriver(sessionId);
            String htmlPage = (String) ((JavascriptExecutor) driver).executeScript("return document.querySelector('*').innerHTML");

            return new SeleniumMCPResponse(sessionId, true, htmlPage);
        } catch (Exception e) {
            return new SeleniumMCPResponse(sessionId, false, "Unable to find page html. Cause : " + e);
        }
    }


}
