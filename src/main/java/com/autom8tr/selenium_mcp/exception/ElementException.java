package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Exception for element-related errors.
 */
public class ElementException extends SeleniumMcpException {

    private final String locatorStrategy;
    private final String locatorValue;

    public ElementException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.locatorStrategy = null;
        this.locatorValue = null;
    }

    public ElementException(UUID sessionId, ErrorCode errorCode, String message,
                            String locatorStrategy, String locatorValue) {
        super(sessionId, errorCode, message);
        this.locatorStrategy = locatorStrategy;
        this.locatorValue = locatorValue;
    }

    public ElementException(UUID sessionId, ErrorCode errorCode, String message,
                            String locatorStrategy, String locatorValue, Throwable cause) {
        super(sessionId, errorCode, message, cause);
        this.locatorStrategy = locatorStrategy;
        this.locatorValue = locatorValue;
    }

    public String getLocatorStrategy() {
        return locatorStrategy;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    /**
     * Creates an exception for element not found.
     */
    public static ElementException notFound(UUID sessionId, String locatorStrategy, String locatorValue) {
        return new ElementException(sessionId, ErrorCode.ELEMENT_NOT_FOUND,
                "Element not found with " + locatorStrategy + "='" + locatorValue + "'",
                locatorStrategy, locatorValue);
    }

    /**
     * Creates an exception for element not visible.
     */
    public static ElementException notVisible(UUID sessionId, String locatorStrategy, String locatorValue) {
        return new ElementException(sessionId, ErrorCode.ELEMENT_NOT_VISIBLE,
                "Element not visible with " + locatorStrategy + "='" + locatorValue + "'",
                locatorStrategy, locatorValue);
    }

    /**
     * Creates an exception for element not interactable.
     */
    public static ElementException notInteractable(UUID sessionId, String locatorStrategy, String locatorValue) {
        return new ElementException(sessionId, ErrorCode.ELEMENT_NOT_INTERACTABLE,
                "Element not interactable with " + locatorStrategy + "='" + locatorValue + "'. " +
                        "Try scrolling to the element or waiting for it to become interactable.",
                locatorStrategy, locatorValue);
    }

    /**
     * Creates an exception for stale element.
     */
    public static ElementException stale(UUID sessionId, String locatorStrategy, String locatorValue) {
        return new ElementException(sessionId, ErrorCode.ELEMENT_STALE,
                "Element is stale (page may have refreshed) with " + locatorStrategy + "='" + locatorValue + "'",
                locatorStrategy, locatorValue);
    }
}

