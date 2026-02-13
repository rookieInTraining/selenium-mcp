package com.autom8tr.selenium_mcp.core;

import java.util.Objects;
import java.util.UUID;

/**
 * Standardized response object for all Selenium MCP tool operations.
 * Contains session information, success status, and result message.
 */
public class SeleniumMCPResponse {

    private UUID sessionId;
    private boolean success;
    private String message;

    /**
     * Creates a response without a session ID (for operations that don't require a session).
     *
     * @param success Whether the operation succeeded
     * @param message Result message or error description
     */
    public SeleniumMCPResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a response with a session ID.
     *
     * @param sessionId The session ID associated with the operation
     * @param success   Whether the operation succeeded
     * @param message   Result message or error description
     */
    public SeleniumMCPResponse(UUID sessionId, boolean success, String message) {
        this.sessionId = sessionId;
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful response with a session ID.
     */
    public static SeleniumMCPResponse success(UUID sessionId, String message) {
        return new SeleniumMCPResponse(sessionId, true, message);
    }

    /**
     * Creates a successful response without a session ID.
     */
    public static SeleniumMCPResponse success(String message) {
        return new SeleniumMCPResponse(true, message);
    }

    /**
     * Creates a failure response with a session ID.
     */
    public static SeleniumMCPResponse failure(UUID sessionId, String message) {
        return new SeleniumMCPResponse(sessionId, false, message);
    }

    /**
     * Creates a failure response without a session ID.
     */
    public static SeleniumMCPResponse failure(String message) {
        return new SeleniumMCPResponse(false, message);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeleniumMCPResponse that = (SeleniumMCPResponse) o;
        return success == that.success &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, success, message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SeleniumMCPResponse{");
        if (sessionId != null) {
            sb.append("sessionId=").append(sessionId).append(", ");
        }
        sb.append("success=").append(success);
        sb.append(", message='").append(truncateMessage(message, 200)).append("'");
        sb.append('}');
        return sb.toString();
    }

    private String truncateMessage(String msg, int maxLength) {
        if (msg == null) return "null";
        if (msg.length() <= maxLength) return msg;
        return msg.substring(0, maxLength) + "...";
    }
}
