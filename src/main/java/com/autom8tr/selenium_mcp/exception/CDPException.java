package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Exception for Chrome DevTools Protocol (CDP) related errors.
 */
public class CDPException extends SeleniumMcpException {

    public CDPException(String message) {
        super(ErrorCode.CDP_COMMAND_FAILED, message);
    }

    public CDPException(String message, Throwable cause) {
        super(ErrorCode.CDP_COMMAND_FAILED, message, cause);
    }

    public CDPException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CDPException(UUID sessionId, ErrorCode errorCode, String message) {
        super(sessionId, errorCode, message);
    }

    public CDPException(UUID sessionId, ErrorCode errorCode, String message, Throwable cause) {
        super(sessionId, errorCode, message, cause);
    }

    /**
     * Creates an exception for CDP not supported.
     */
    public static CDPException notSupported(UUID sessionId) {
        return new CDPException(sessionId, ErrorCode.CDP_NOT_SUPPORTED,
                "CDP is not supported for this browser. Only Chrome and Edge support CDP.");
    }

    /**
     * Creates an exception for CDP command failure.
     */
    public static CDPException commandFailed(UUID sessionId, String command, Throwable cause) {
        return new CDPException(sessionId, ErrorCode.CDP_COMMAND_FAILED,
                "CDP command '" + command + "' failed: " + cause.getMessage(), cause);
    }

    /**
     * Creates an exception for CDP connection failure.
     */
    public static CDPException connectionFailed(UUID sessionId, Throwable cause) {
        return new CDPException(sessionId, ErrorCode.CDP_CONNECTION_FAILED,
                "Failed to connect to CDP: " + cause.getMessage(), cause);
    }
}

