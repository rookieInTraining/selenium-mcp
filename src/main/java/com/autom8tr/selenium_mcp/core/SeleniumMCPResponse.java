package com.autom8tr.selenium_mcp.core;

public class SeleniumMCPResponse {

    private boolean success;
    private String message;

    public SeleniumMCPResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
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
