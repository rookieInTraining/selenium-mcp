package com.autom8tr.selenium_mcp.core;

import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SeleniumSessionContainer {

    public static final Map<UUID, WebDriver> sessions = new HashMap<>();

    public static Object getDriver(UUID sessionId) {
        WebDriver wD = sessions.get(sessionId);
        if (Objects.isNull(wD)) {
            return new SeleniumMCPResponse(false,  "Session not found. Please create a session first using browser_init()");
        }

        return wD;
    }

}
