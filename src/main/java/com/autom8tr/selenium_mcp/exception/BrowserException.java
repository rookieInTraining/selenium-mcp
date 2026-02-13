package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Exception for browser-related errors.
 */
public class BrowserException extends SeleniumMcpException {

    public BrowserException(String message) {
        super(ErrorCode.BROWSER_INIT_FAILED, message);
    }

    public BrowserException(String message, Throwable cause) {
        super(ErrorCode.BROWSER_INIT_FAILED, message, cause);
    }

    public BrowserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BrowserException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BrowserException(UUID sessionId, ErrorCode errorCode, String message) {
        super(sessionId, errorCode, message);
    }

    public BrowserException(UUID sessionId, ErrorCode errorCode, String message, Throwable cause) {
        super(sessionId, errorCode, message, cause);
    }

    /**
     * Creates an exception for unsupported browser.
     */
    public static BrowserException unsupported(String browserType) {
        return new BrowserException(ErrorCode.BROWSER_UNSUPPORTED,
                "Unsupported browser: '" + browserType + "'. Supported browsers: chrome, firefox, edge");
    }

    /**
     * Creates an exception for initialization failure.
     */
    public static BrowserException initFailed(String browserType, Throwable cause) {
        return new BrowserException(ErrorCode.BROWSER_INIT_FAILED,
                "Failed to initialize " + browserType + " browser: " + cause.getMessage(), cause);
    }

    /**
     * Creates an exception for browser crash.
     */
    public static BrowserException crashed(UUID sessionId) {
        return new BrowserException(sessionId, ErrorCode.BROWSER_CRASHED,
                "Browser crashed for session: " + sessionId);
    }

    /**
     * Creates an exception for timeout.
     */
    public static BrowserException timeout(UUID sessionId, String operation) {
        return new BrowserException(sessionId, ErrorCode.BROWSER_TIMEOUT,
                "Browser operation timed out: " + operation + " for session: " + sessionId);
    }
}

