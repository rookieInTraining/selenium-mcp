package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Exception for validation-related errors.
 */
public class ValidationException extends SeleniumMcpException {

    private final String parameterName;

    public ValidationException(String message) {
        super(ErrorCode.INVALID_PARAMETER, message);
        this.parameterName = null;
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.parameterName = null;
    }

    public ValidationException(ErrorCode errorCode, String message, String parameterName) {
        super(errorCode, message);
        this.parameterName = parameterName;
    }

    public ValidationException(UUID sessionId, ErrorCode errorCode, String message, String parameterName) {
        super(sessionId, errorCode, message);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    /**
     * Creates an exception for invalid locator strategy.
     */
    public static ValidationException invalidLocatorStrategy(String strategy) {
        return new ValidationException(ErrorCode.INVALID_LOCATOR_STRATEGY,
                "Invalid locator strategy: '" + strategy + "'. " +
                        "Supported strategies: id, xpath, css, name, tag, link, partial_link, class",
                "locatorStrategy");
    }

    /**
     * Creates an exception for invalid parameter.
     */
    public static ValidationException invalidParameter(String parameterName, String reason) {
        return new ValidationException(ErrorCode.INVALID_PARAMETER,
                "Invalid parameter '" + parameterName + "': " + reason,
                parameterName);
    }

    /**
     * Creates an exception for missing required parameter.
     */
    public static ValidationException missingParameter(String parameterName) {
        return new ValidationException(ErrorCode.MISSING_PARAMETER,
                "Missing required parameter: " + parameterName,
                parameterName);
    }

    /**
     * Creates an exception for invalid URL.
     */
    public static ValidationException invalidUrl(String url) {
        return new ValidationException(ErrorCode.INVALID_PARAMETER,
                "Invalid URL: " + url,
                "url");
    }
}

