package com.autom8tr.selenium_mcp.core;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SeleniumMCPResponseTest {

    @Test
    void constructor_withoutSessionId_shouldCreateResponse() {
        SeleniumMCPResponse response = new SeleniumMCPResponse(true, "Success message");

        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getMessage());
        assertNull(response.getSessionId());
    }

    @Test
    void constructor_withSessionId_shouldCreateResponse() {
        UUID sessionId = UUID.randomUUID();
        SeleniumMCPResponse response = new SeleniumMCPResponse(sessionId, true, "Success message");

        assertEquals(sessionId, response.getSessionId());
        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getMessage());
    }

    @Test
    void success_factoryMethod_withSessionId_shouldCreateSuccessResponse() {
        UUID sessionId = UUID.randomUUID();
        SeleniumMCPResponse response = SeleniumMCPResponse.success(sessionId, "Operation completed");

        assertEquals(sessionId, response.getSessionId());
        assertTrue(response.isSuccess());
        assertEquals("Operation completed", response.getMessage());
    }

    @Test
    void success_factoryMethod_withoutSessionId_shouldCreateSuccessResponse() {
        SeleniumMCPResponse response = SeleniumMCPResponse.success("Operation completed");

        assertNull(response.getSessionId());
        assertTrue(response.isSuccess());
        assertEquals("Operation completed", response.getMessage());
    }

    @Test
    void failure_factoryMethod_withSessionId_shouldCreateFailureResponse() {
        UUID sessionId = UUID.randomUUID();
        SeleniumMCPResponse response = SeleniumMCPResponse.failure(sessionId, "Operation failed");

        assertEquals(sessionId, response.getSessionId());
        assertFalse(response.isSuccess());
        assertEquals("Operation failed", response.getMessage());
    }

    @Test
    void failure_factoryMethod_withoutSessionId_shouldCreateFailureResponse() {
        SeleniumMCPResponse response = SeleniumMCPResponse.failure("Operation failed");

        assertNull(response.getSessionId());
        assertFalse(response.isSuccess());
        assertEquals("Operation failed", response.getMessage());
    }

    @Test
    void equals_shouldReturnTrueForEqualResponses() {
        UUID sessionId = UUID.randomUUID();
        SeleniumMCPResponse response1 = new SeleniumMCPResponse(sessionId, true, "Test");
        SeleniumMCPResponse response2 = new SeleniumMCPResponse(sessionId, true, "Test");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentResponses() {
        SeleniumMCPResponse response1 = new SeleniumMCPResponse(true, "Test1");
        SeleniumMCPResponse response2 = new SeleniumMCPResponse(true, "Test2");

        assertNotEquals(response1, response2);
    }

    @Test
    void toString_shouldIncludeAllFields() {
        UUID sessionId = UUID.randomUUID();
        SeleniumMCPResponse response = new SeleniumMCPResponse(sessionId, true, "Test message");

        String str = response.toString();

        assertTrue(str.contains("sessionId=" + sessionId));
        assertTrue(str.contains("success=true"));
        assertTrue(str.contains("message='Test message'"));
    }

    @Test
    void toString_shouldTruncateLongMessages() {
        String longMessage = "A".repeat(300);
        SeleniumMCPResponse response = new SeleniumMCPResponse(true, longMessage);

        String str = response.toString();

        assertTrue(str.contains("..."));
        assertTrue(str.length() < longMessage.length() + 100);
    }

    @Test
    void setters_shouldUpdateFields() {
        SeleniumMCPResponse response = new SeleniumMCPResponse(true, "Initial");
        UUID sessionId = UUID.randomUUID();

        response.setSessionId(sessionId);
        response.setSuccess(false);
        response.setMessage("Updated");

        assertEquals(sessionId, response.getSessionId());
        assertFalse(response.isSuccess());
        assertEquals("Updated", response.getMessage());
    }
}

