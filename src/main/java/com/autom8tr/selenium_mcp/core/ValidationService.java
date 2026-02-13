package com.autom8tr.selenium_mcp.core;

import com.autom8tr.selenium_mcp.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service for validating and sanitizing input parameters.
 * Prevents injection attacks and ensures data integrity.
 */
@Service
public class ValidationService {

    // Patterns for validation
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[\\w\\s\\-_.@#$%&*()+=\\[\\]{}|:;'\"<>,./\\\\!?`~^]+$");
    private static final Pattern JAVASCRIPT_INJECTION_PATTERN = Pattern.compile("(?i)<script|javascript:|on\\w+\\s*=");
    private static final int MAX_LOCATOR_LENGTH = 5000;
    private static final int MAX_TEXT_INPUT_LENGTH = 100000;
    private static final int MAX_SCRIPT_LENGTH = 1000000;
    private static final int MAX_URL_LENGTH = 8192;

    /**
     * Validates a session ID.
     */
    public void validateSessionId(UUID sessionId) {
        if (sessionId == null) {
            throw ValidationException.missingParameter("sessionId");
        }
    }

    /**
     * Validates a locator strategy.
     */
    public void validateLocatorStrategy(String strategy) {
        if (strategy == null || strategy.isBlank()) {
            throw ValidationException.missingParameter("locatorStrategy");
        }

        if (!SeleniumLocatorBuilder.isValidStrategy(strategy)) {
            throw ValidationException.invalidLocatorStrategy(strategy);
        }
    }

    /**
     * Validates a locator value.
     */
    public void validateLocatorValue(String value) {
        if (value == null || value.isBlank()) {
            throw ValidationException.missingParameter("locatorValue");
        }

        if (value.length() > MAX_LOCATOR_LENGTH) {
            throw ValidationException.invalidParameter("locatorValue",
                    "Exceeds maximum length of " + MAX_LOCATOR_LENGTH);
        }
    }

    /**
     * Validates a URL.
     */
    public void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw ValidationException.missingParameter("url");
        }

        if (url.length() > MAX_URL_LENGTH) {
            throw ValidationException.invalidParameter("url",
                    "Exceeds maximum length of " + MAX_URL_LENGTH);
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol().toLowerCase();

            // Only allow http and https
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw ValidationException.invalidUrl("Only HTTP and HTTPS protocols are allowed");
            }
        } catch (MalformedURLException e) {
            throw ValidationException.invalidUrl(url);
        }
    }

    /**
     * Validates text input.
     */
    public void validateTextInput(String text) {
        if (text == null) {
            throw ValidationException.missingParameter("text");
        }

        if (text.length() > MAX_TEXT_INPUT_LENGTH) {
            throw ValidationException.invalidParameter("text",
                    "Exceeds maximum length of " + MAX_TEXT_INPUT_LENGTH);
        }
    }

    /**
     * Validates JavaScript code.
     */
    public String validateAndSanitizeScript(String script) {
        if (script == null || script.isBlank()) {
            throw ValidationException.missingParameter("script");
        }

        if (script.length() > MAX_SCRIPT_LENGTH) {
            throw ValidationException.invalidParameter("script",
                    "Exceeds maximum length of " + MAX_SCRIPT_LENGTH);
        }

        // Note: We allow JavaScript execution but log it for security auditing
        return script;
    }

    /**
     * Validates browser type.
     */
    public void validateBrowserType(String browserType) {
        if (browserType == null || browserType.isBlank()) {
            return; // Will use default
        }

        String normalized = browserType.toLowerCase().trim();
        if (!normalized.matches("chrome|firefox|edge")) {
            throw ValidationException.invalidParameter("browserType",
                    "Must be one of: chrome, firefox, edge");
        }
    }

    /**
     * Validates device type.
     */
    public void validateDeviceType(String deviceType) {
        if (deviceType == null || deviceType.isBlank()) {
            return; // Will use default
        }

        String normalized = deviceType.toLowerCase().trim();
        if (!normalized.matches("desktop|mobile")) {
            throw ValidationException.invalidParameter("deviceType",
                    "Must be one of: desktop, mobile");
        }
    }

    /**
     * Validates an attribute name.
     */
    public void validateAttributeName(String attributeName) {
        if (attributeName == null || attributeName.isBlank()) {
            throw ValidationException.missingParameter("attributeName");
        }

        if (attributeName.length() > 256) {
            throw ValidationException.invalidParameter("attributeName",
                    "Exceeds maximum length of 256");
        }

        // Check for potentially dangerous attribute names
        if (JAVASCRIPT_INJECTION_PATTERN.matcher(attributeName).find()) {
            throw ValidationException.invalidParameter("attributeName",
                    "Contains potentially dangerous content");
        }
    }

    /**
     * Validates a key name for keyboard actions.
     */
    public void validateKeyName(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            throw ValidationException.missingParameter("keyName");
        }

        if (keyName.length() > 50) {
            throw ValidationException.invalidParameter("keyName",
                    "Exceeds maximum length of 50");
        }
    }

    /**
     * Sanitizes a string for safe logging.
     */
    public String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }

        // Truncate long strings
        String truncated = input.length() > 500 ? input.substring(0, 500) + "..." : input;

        // Remove control characters
        return truncated.replaceAll("[\\x00-\\x1F\\x7F]", "");
    }

    /**
     * Checks if a string contains potential XSS content.
     */
    public boolean containsPotentialXss(String input) {
        if (input == null) {
            return false;
        }

        return JAVASCRIPT_INJECTION_PATTERN.matcher(input).find();
    }
}

