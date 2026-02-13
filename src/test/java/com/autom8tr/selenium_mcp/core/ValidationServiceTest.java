package com.autom8tr.selenium_mcp.core;

import com.autom8tr.selenium_mcp.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void validateSessionId_shouldAcceptValidUUID() {
        assertDoesNotThrow(() -> validationService.validateSessionId(UUID.randomUUID()));
    }

    @Test
    void validateSessionId_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateSessionId(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "xpath", "css", "name", "tag", "link", "class"})
    void validateLocatorStrategy_shouldAcceptValidStrategies(String strategy) {
        assertDoesNotThrow(() -> validationService.validateLocatorStrategy(strategy));
    }

    @Test
    void validateLocatorStrategy_shouldThrowForInvalidStrategy() {
        assertThrows(ValidationException.class, () ->
                validationService.validateLocatorStrategy("invalid"));
    }

    @Test
    void validateLocatorStrategy_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateLocatorStrategy(null));
    }

    @Test
    void validateLocatorValue_shouldAcceptValidValue() {
        assertDoesNotThrow(() -> validationService.validateLocatorValue("test-locator"));
    }

    @Test
    void validateLocatorValue_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateLocatorValue(null));
    }

    @Test
    void validateLocatorValue_shouldThrowForEmpty() {
        assertThrows(ValidationException.class, () ->
                validationService.validateLocatorValue(""));
    }

    @Test
    void validateLocatorValue_shouldThrowForTooLong() {
        String longValue = "a".repeat(6000);
        assertThrows(ValidationException.class, () ->
                validationService.validateLocatorValue(longValue));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://example.com", "http://localhost:8080", "https://test.com/path?query=value"})
    void validateUrl_shouldAcceptValidUrls(String url) {
        assertDoesNotThrow(() -> validationService.validateUrl(url));
    }

    @Test
    void validateUrl_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateUrl(null));
    }

    @Test
    void validateUrl_shouldThrowForInvalidUrl() {
        assertThrows(ValidationException.class, () ->
                validationService.validateUrl("not-a-url"));
    }

    @Test
    void validateUrl_shouldThrowForNonHttpProtocol() {
        assertThrows(ValidationException.class, () ->
                validationService.validateUrl("file:///etc/passwd"));
    }

    @Test
    void validateTextInput_shouldAcceptValidText() {
        assertDoesNotThrow(() -> validationService.validateTextInput("Hello World"));
    }

    @Test
    void validateTextInput_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateTextInput(null));
    }

    @Test
    void validateTextInput_shouldThrowForTooLong() {
        String longText = "a".repeat(150000);
        assertThrows(ValidationException.class, () ->
                validationService.validateTextInput(longText));
    }

    @Test
    void validateAndSanitizeScript_shouldReturnScript() {
        String script = "return document.title;";
        String result = validationService.validateAndSanitizeScript(script);
        assertEquals(script, result);
    }

    @Test
    void validateAndSanitizeScript_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateAndSanitizeScript(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"chrome", "firefox", "edge"})
    void validateBrowserType_shouldAcceptValidBrowsers(String browser) {
        assertDoesNotThrow(() -> validationService.validateBrowserType(browser));
    }

    @Test
    void validateBrowserType_shouldThrowForInvalidBrowser() {
        assertThrows(ValidationException.class, () ->
                validationService.validateBrowserType("safari"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"desktop", "mobile"})
    void validateDeviceType_shouldAcceptValidDevices(String device) {
        assertDoesNotThrow(() -> validationService.validateDeviceType(device));
    }

    @Test
    void validateDeviceType_shouldThrowForInvalidDevice() {
        assertThrows(ValidationException.class, () ->
                validationService.validateDeviceType("tablet"));
    }

    @Test
    void validateAttributeName_shouldAcceptValidName() {
        assertDoesNotThrow(() -> validationService.validateAttributeName("class"));
    }

    @Test
    void validateAttributeName_shouldThrowForNull() {
        assertThrows(ValidationException.class, () ->
                validationService.validateAttributeName(null));
    }

    @Test
    void validateAttributeName_shouldThrowForDangerousContent() {
        assertThrows(ValidationException.class, () ->
                validationService.validateAttributeName("onclick=alert(1)"));
    }

    @Test
    void sanitizeForLogging_shouldTruncateLongStrings() {
        String longString = "a".repeat(1000);
        String sanitized = validationService.sanitizeForLogging(longString);
        assertTrue(sanitized.length() < longString.length());
        assertTrue(sanitized.endsWith("..."));
    }

    @Test
    void sanitizeForLogging_shouldHandleNull() {
        assertEquals("null", validationService.sanitizeForLogging(null));
    }

    @Test
    void containsPotentialXss_shouldDetectScriptTag() {
        assertTrue(validationService.containsPotentialXss("<script>alert(1)</script>"));
    }

    @Test
    void containsPotentialXss_shouldDetectJavascriptUri() {
        assertTrue(validationService.containsPotentialXss("javascript:alert(1)"));
    }

    @Test
    void containsPotentialXss_shouldDetectEventHandler() {
        assertTrue(validationService.containsPotentialXss("onclick=alert(1)"));
    }

    @Test
    void containsPotentialXss_shouldReturnFalseForSafeContent() {
        assertFalse(validationService.containsPotentialXss("Hello World"));
    }

    @Test
    void containsPotentialXss_shouldReturnFalseForNull() {
        assertFalse(validationService.containsPotentialXss(null));
    }
}

