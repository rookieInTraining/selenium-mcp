package com.autom8tr.selenium_mcp.lenium;

public class AbstractWebDriverFactory {

    public static WebDrivers getDeviceDriver(String device) {
        switch (device) {
            case "mobile" -> {
                return new AppiumWebDriverFactory();
            }
            case "desktop" -> {
                return new SeleniumWebDriverFactory();
            }
            default -> {
                throw new RuntimeException("The mentioned browser <-> web-driver configuration is not supported or found!");
            }
        }
    }

}
