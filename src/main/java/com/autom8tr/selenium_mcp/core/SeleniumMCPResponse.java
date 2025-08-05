package com.autom8tr.selenium_mcp.core;

import java.util.UUID;

public class SeleniumMCPResponse {

    private UUID sessionId;
    private boolean success;
    private String message;

    public SeleniumMCPResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public SeleniumMCPResponse(UUID sessionId, boolean success, String message) {
        this.sessionId = sessionId;
        this.success = success;
        this.message = message;
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
}
