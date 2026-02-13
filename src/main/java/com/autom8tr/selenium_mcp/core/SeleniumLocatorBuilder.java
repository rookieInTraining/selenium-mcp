package com.autom8tr.selenium_mcp.core;

import org.openqa.selenium.By;

/**
 * Utility class for building Selenium locators from strategy strings.
 * Supports all major locator strategies with input validation.
 */
public final class SeleniumLocatorBuilder {

    private SeleniumLocatorBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a Selenium By locator from a strategy name and value.
     *
     * @param locatorStrategy The locator strategy (id, xpath, css, name, tag, link, class, partial_link)
     * @param locatorValue    The locator value
     * @return By locator instance
     * @throws IllegalArgumentException if strategy or value is invalid
     */
    public static By getLocator(String locatorStrategy, String locatorValue) {
        validateInputs(locatorStrategy, locatorValue);

        return switch (locatorStrategy.toLowerCase().trim()) {
            case "id" -> By.id(locatorValue);
            case "xpath" -> By.xpath(locatorValue);
            case "css", "css selector", "css_selector", "cssselector" -> By.cssSelector(locatorValue);
            case "name" -> By.name(locatorValue);
            case "tag", "tagname", "tag_name", "tag name" -> By.tagName(locatorValue);
            case "link", "link text", "link_text", "linktext" -> By.linkText(locatorValue);
            case "partial_link", "partial link", "partial_link_text", "partiallinktext" -> By.partialLinkText(locatorValue);
            case "class", "classname", "class_name", "class name" -> By.className(locatorValue);
            default -> throw new InvalidLocatorStrategyException(
                    "Unknown locator strategy: '" + locatorStrategy + "'. " +
                            "Supported strategies: id, xpath, css, name, tag, link, partial_link, class");
        };
    }

    /**
     * Validates that strategy and value are not null or empty.
     */
    private static void validateInputs(String locatorStrategy, String locatorValue) {
        if (locatorStrategy == null || locatorStrategy.isBlank()) {
            throw new IllegalArgumentException("Locator strategy cannot be null or empty");
        }
        if (locatorValue == null || locatorValue.isBlank()) {
            throw new IllegalArgumentException("Locator value cannot be null or empty");
        }
    }

    /**
     * Checks if a given strategy name is valid.
     *
     * @param strategy The strategy name to check
     * @return true if the strategy is supported
     */
    public static boolean isValidStrategy(String strategy) {
        if (strategy == null || strategy.isBlank()) {
            return false;
        }

        return switch (strategy.toLowerCase().trim()) {
            case "id", "xpath", "css", "css selector", "css_selector", "cssselector",
                 "name", "tag", "tagname", "tag_name", "tag name",
                 "link", "link text", "link_text", "linktext",
                 "partial_link", "partial link", "partial_link_text", "partiallinktext",
                 "class", "classname", "class_name", "class name" -> true;
            default -> false;
        };
    }

    /**
     * Returns a list of all supported locator strategy names.
     *
     * @return Array of supported strategy names
     */
    public static String[] getSupportedStrategies() {
        return new String[]{"id", "xpath", "css", "name", "tag", "link", "partial_link", "class"};
    }

    /**
     * Exception thrown when an invalid locator strategy is used.
     */
    public static class InvalidLocatorStrategyException extends RuntimeException {
        public InvalidLocatorStrategyException(String message) {
            super(message);
        }
    }
}
