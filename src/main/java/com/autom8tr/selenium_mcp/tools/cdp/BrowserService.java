package com.autom8tr.selenium_mcp.tools.cdp;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v138.browser.Browser;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.getDriver;

@Service
public class BrowserService {

    @Tool(description = "Fetch the browser version using CDP")
    public SeleniumMCPResponse get_browser_version(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        WebDriver driver = (WebDriver) getDriver(sessionId);
        try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
            devTools.createSessionIfThereIsNotOne();
            Browser.GetVersionResponse getVersionResponse = devTools.send(Browser.getVersion());
            return new SeleniumMCPResponse(true, "Browser Details : \n" + getVersionResponse.toString());
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to find the expected element. Cause : " + e);
        }
    }

}
