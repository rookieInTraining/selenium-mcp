package com.autom8tr.selenium_mcp.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

class SeleniumLocatorBuilderTest {

    @ParameterizedTest
    @CsvSource({
            "id, test-id",
            "ID, test-id",
            "Id, test-id"
    })
    void getLocator_shouldCreateIdLocator(String strategy, String value) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, value);

        assertNotNull(locator);
        assertEquals(By.id(value), locator);
    }

    @ParameterizedTest
    @CsvSource({
            "xpath, //div[@class='test']",
            "XPATH, //div[@id='test']"
    })
    void getLocator_shouldCreateXpathLocator(String strategy, String value) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, value);

        assertNotNull(locator);
        assertEquals(By.xpath(value), locator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"css", "CSS", "css selector", "css_selector", "cssselector"})
    void getLocator_shouldCreateCssSelectorLocator(String strategy) {
        String value = "div.test-class";

        By locator = SeleniumLocatorBuilder.getLocator(strategy, value);

        assertNotNull(locator);
        assertEquals(By.cssSelector(value), locator);
    }

    @Test
    void getLocator_shouldCreateNameLocator() {
        By locator = SeleniumLocatorBuilder.getLocator("name", "test-name");

        assertNotNull(locator);
        assertEquals(By.name("test-name"), locator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"tag", "tagname", "tag_name", "tag name"})
    void getLocator_shouldCreateTagNameLocator(String strategy) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, "div");

        assertNotNull(locator);
        assertEquals(By.tagName("div"), locator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"link", "link text", "link_text", "linktext"})
    void getLocator_shouldCreateLinkTextLocator(String strategy) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, "Click here");

        assertNotNull(locator);
        assertEquals(By.linkText("Click here"), locator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"partial_link", "partial link", "partial_link_text", "partiallinktext"})
    void getLocator_shouldCreatePartialLinkTextLocator(String strategy) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, "Click");

        assertNotNull(locator);
        assertEquals(By.partialLinkText("Click"), locator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"class", "classname", "class_name", "class name"})
    void getLocator_shouldCreateClassNameLocator(String strategy) {
        By locator = SeleniumLocatorBuilder.getLocator(strategy, "test-class");

        assertNotNull(locator);
        assertEquals(By.className("test-class"), locator);
    }

    @Test
    void getLocator_shouldThrowExceptionForInvalidStrategy() {
        assertThrows(SeleniumLocatorBuilder.InvalidLocatorStrategyException.class, () ->
                SeleniumLocatorBuilder.getLocator("invalid", "value"));
    }

    @Test
    void getLocator_shouldThrowExceptionForNullStrategy() {
        assertThrows(IllegalArgumentException.class, () ->
                SeleniumLocatorBuilder.getLocator(null, "value"));
    }

    @Test
    void getLocator_shouldThrowExceptionForEmptyStrategy() {
        assertThrows(IllegalArgumentException.class, () ->
                SeleniumLocatorBuilder.getLocator("", "value"));
    }

    @Test
    void getLocator_shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class, () ->
                SeleniumLocatorBuilder.getLocator("id", null));
    }

    @Test
    void getLocator_shouldThrowExceptionForEmptyValue() {
        assertThrows(IllegalArgumentException.class, () ->
                SeleniumLocatorBuilder.getLocator("id", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "xpath", "css", "name", "tag", "link", "partial_link", "class"})
    void isValidStrategy_shouldReturnTrueForValidStrategies(String strategy) {
        assertTrue(SeleniumLocatorBuilder.isValidStrategy(strategy));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "unknown", "foo"})
    void isValidStrategy_shouldReturnFalseForInvalidStrategies(String strategy) {
        assertFalse(SeleniumLocatorBuilder.isValidStrategy(strategy));
    }

    @Test
    void isValidStrategy_shouldReturnFalseForNull() {
        assertFalse(SeleniumLocatorBuilder.isValidStrategy(null));
    }

    @Test
    void getSupportedStrategies_shouldReturnAllStrategies() {
        String[] strategies = SeleniumLocatorBuilder.getSupportedStrategies();

        assertNotNull(strategies);
        assertEquals(8, strategies.length);
    }
}

