package com.autom8tr.selenium_mcp.tools.cdp;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v138.browser.Browser;
import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.devtools.v138.network.model.Cookie;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.getDriver;

@Service
public class NetworkService {

    @Tool(description = "Enable CDP for network requests")
    public SeleniumMCPResponse enable_network_cdp(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId,
            @ToolParam int maxTotalBufferSize,
            @ToolParam int maxResourceBufferSize,
            @ToolParam int maxPostDataSize,
            @ToolParam boolean reportDirectSocketTraffic
    ) {
        WebDriver driver = (WebDriver) getDriver(sessionId);
        try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
            devTools.createSessionIfThereIsNotOne();
            Network.enable(
                    Optional.of(maxTotalBufferSize),
                    Optional.of(maxResourceBufferSize),
                    Optional.of(maxPostDataSize),
                    Optional.of(reportDirectSocketTraffic)
            );
            return new SeleniumMCPResponse(true, "CDP for Network enabled!");
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to enable CDP. Cause : " + e);
        }
    }

    @Tool(description = "Fetch all cookies from the browser session")
    public SeleniumMCPResponse get_all_cookies(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        WebDriver driver = (WebDriver) getDriver(sessionId);
        try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
            devTools.createSessionIfThereIsNotOne();
            List<Cookie> cookies = devTools.send(Network.getAllCookies());
            return new SeleniumMCPResponse(true, "Browser Cookies : \n" + Arrays.asList(cookies));
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to execute CDP request. Cause : " + e);
        }
    }

    @Tool(description = "Disable CDP for network requests")
    public SeleniumMCPResponse disable_network_cdp(
            @ToolParam(description = "The session id of the browser instance to be used for performing the action") UUID sessionId
    ) {
        WebDriver driver = (WebDriver) getDriver(sessionId);
        try (DevTools devTools = ((HasDevTools) driver).getDevTools()) {
            devTools.createSessionIfThereIsNotOne();
            Network.disable();
            return new SeleniumMCPResponse(true, "CDP for Network disabled!");
        } catch (Exception e) {
            return new SeleniumMCPResponse(false, "Unable to execute CDP request. Cause : " + e);
        }
    }

}
