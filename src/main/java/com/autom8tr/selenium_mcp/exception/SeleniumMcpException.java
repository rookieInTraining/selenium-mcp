package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Base exception class for all Selenium MCP exceptions.
 * Provides consistent error handling across the application.
 */
public class SeleniumMcpException extends RuntimeException {

    private final UUID sessionId;
    private final ErrorCode errorCode;

    public enum ErrorCode {
        // Session errors (1xxx)
        SESSION_NOT_FOUND(1001, "Session not found"),
        SESSION_LIMIT_EXCEEDED(1002, "Session limit exceeded"),
        SESSION_EXPIRED(1003, "Session expired"),
        SESSION_INVALID(1004, "Invalid session"),

        // Browser errors (2xxx)
        BROWSER_INIT_FAILED(2001, "Browser initialization failed"),
        BROWSER_UNSUPPORTED(2002, "Unsupported browser type"),
        BROWSER_CRASHED(2003, "Browser crashed"),
        BROWSER_TIMEOUT(2004, "Browser operation timed out"),

        // Element errors (3xxx)
        ELEMENT_NOT_FOUND(3001, "Element not found"),
        ELEMENT_NOT_VISIBLE(3002, "Element not visible"),
        ELEMENT_NOT_INTERACTABLE(3003, "Element not interactable"),
        ELEMENT_STALE(3004, "Element is stale"),

        // Navigation errors (4xxx)
        NAVIGATION_FAILED(4001, "Navigation failed"),
        PAGE_LOAD_TIMEOUT(4002, "Page load timeout"),
        INVALID_URL(4003, "Invalid URL"),

        // CDP errors (5xxx)
        CDP_NOT_SUPPORTED(5001, "CDP not supported for this browser"),
        CDP_COMMAND_FAILED(5002, "CDP command failed"),
        CDP_CONNECTION_FAILED(5003, "CDP connection failed"),

        // JavaScript errors (6xxx)
        JS_EXECUTION_FAILED(6001, "JavaScript execution failed"),
        JS_TIMEOUT(6002, "JavaScript execution timed out"),

        // Validation errors (7xxx)
        INVALID_LOCATOR_STRATEGY(7001, "Invalid locator strategy"),
        INVALID_PARAMETER(7002, "Invalid parameter"),
        MISSING_PARAMETER(7003, "Missing required parameter"),

        // System errors (9xxx)
        INTERNAL_ERROR(9001, "Internal error"),
        RESOURCE_EXHAUSTED(9002, "Resource exhausted"),
        RATE_LIMITED(9003, "Rate limited");

        private final int code;
        private final String description;

        ErrorCode(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public SeleniumMcpException(String message) {
        super(message);
        this.sessionId = null;
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public SeleniumMcpException(String message, Throwable cause) {
        super(message, cause);
        this.sessionId = null;
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public SeleniumMcpException(ErrorCode errorCode, String message) {
        super(message);
        this.sessionId = null;
        this.errorCode = errorCode;
    }

    public SeleniumMcpException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = null;
        this.errorCode = errorCode;
    }

    public SeleniumMcpException(UUID sessionId, ErrorCode errorCode, String message) {
        super(message);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
    }

    public SeleniumMcpException(UUID sessionId, ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getErrorCodeNumber() {
        return errorCode.getCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[code=").append(errorCode.getCode());
        sb.append(", error=").append(errorCode.name());
        if (sessionId != null) {
            sb.append(", sessionId=").append(sessionId);
        }
        sb.append("]: ").append(getMessage());
        return sb.toString();
    }
}

