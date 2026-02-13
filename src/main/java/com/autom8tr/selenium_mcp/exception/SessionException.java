package com.autom8tr.selenium_mcp.exception;

import java.util.UUID;

/**
 * Exception for session-related errors.
 */
public class SessionException extends SeleniumMcpException {

    public SessionException(String message) {
        super(ErrorCode.SESSION_INVALID, message);
    }

    public SessionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SessionException(UUID sessionId, ErrorCode errorCode, String message) {
        super(sessionId, errorCode, message);
    }

    /**
     * Creates an exception for session not found.
     */
    public static SessionException notFound(UUID sessionId) {
        return new SessionException(sessionId, ErrorCode.SESSION_NOT_FOUND,
                "Session not found: " + sessionId + ". Please create a session first using browser_init()");
    }

    /**
     * Creates an exception for session limit exceeded.
     */
    public static SessionException limitExceeded(int maxSessions) {
        return new SessionException(ErrorCode.SESSION_LIMIT_EXCEEDED,
                "Maximum session limit (" + maxSessions + ") reached. Please close existing sessions first.");
    }

    /**
     * Creates an exception for expired session.
     */
    public static SessionException expired(UUID sessionId) {
        return new SessionException(sessionId, ErrorCode.SESSION_EXPIRED,
                "Session expired: " + sessionId + ". Please create a new session.");
    }
}

