package com.autom8tr.selenium_mcp.core;

import org.openqa.selenium.By;

public class SeleniumLocatorBuilder {

    public static By getLocator(String locatorStrategy, String locator) {
        switch (locatorStrategy.toLowerCase()) {
            case "id" -> {
                return By.id(locator);
            }
            case "xpath" -> {
                return By.xpath(locator);
            }
            case "css", "css selector","css_selector" -> {
                return By.cssSelector(locator);
            }
            case "name" -> {
                return By.name(locator);
            }
            case "tag" -> {
                return By.tagName(locator);
            }
            case "link", "link text", "link_text" -> {
                return By.linkText(locator);
            }
            default -> throw new RuntimeException("The requested locator strategy does not exist");
        }
    }

}
