package com.autom8tr.selenium_mcp.core;

import com.autom8tr.selenium_mcp.config.SessionProperties;
import com.autom8tr.selenium_mcp.core.session.InMemorySessionStorage;
import com.autom8tr.selenium_mcp.core.session.SessionStorage;
import com.autom8tr.selenium_mcp.exception.SessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeleniumSessionContainerTest {

    @Mock
    private WebDriver mockDriver;

    private SeleniumSessionContainer sessionContainer;
    private SessionStorage sessionStorage;
    private SessionProperties sessionProperties;

    @BeforeEach
    void setUp() {
        sessionStorage = new InMemorySessionStorage();
        sessionProperties = new SessionProperties();
        sessionProperties.setMaxSessions(10);
        sessionProperties.setTtl(Duration.ofMinutes(30));

        sessionContainer = new SeleniumSessionContainer(sessionStorage, sessionProperties);
    }

    @Test
    void createSession_shouldStoreSessionAndReturnMetadata() {
        UUID sessionId = UUID.randomUUID();

        SessionMetadata metadata = sessionContainer.createSession(
                sessionId, mockDriver, "chrome", "desktop");

        assertNotNull(metadata);
        assertEquals(sessionId, metadata.getSessionId());
        assertEquals("chrome", metadata.getBrowserType());
        assertEquals("desktop", metadata.getDeviceType());
        assertEquals(SessionMetadata.SessionState.ACTIVE, metadata.getState());
        assertEquals(1, sessionContainer.getActiveSessionCount());
    }

    @Test
    void createSession_shouldThrowExceptionWhenLimitReached() {
        // Fill up to max sessions
        for (int i = 0; i < 10; i++) {
            WebDriver driver = mock(WebDriver.class);
            sessionContainer.createSession(UUID.randomUUID(), driver, "chrome", "desktop");
        }

        // Attempt to create one more should fail
        assertThrows(SessionException.class, () ->
                sessionContainer.createSession(UUID.randomUUID(), mockDriver, "chrome", "desktop"));
    }

    @Test
    void getDriver_shouldReturnDriverWhenSessionExists() {
        UUID sessionId = UUID.randomUUID();
        sessionContainer.createSession(sessionId, mockDriver, "chrome", "desktop");

        WebDriver retrieved = sessionContainer.getDriver(sessionId);

        assertSame(mockDriver, retrieved);
    }

    @Test
    void getDriver_shouldThrowExceptionWhenSessionNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(SessionException.class, () ->
                sessionContainer.getDriver(nonExistentId));
    }

    @Test
    void getDriverOptional_shouldReturnEmptyWhenSessionNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<WebDriver> result = sessionContainer.getDriverOptional(nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void removeSession_shouldCloseDriverAndRemoveSession() {
        UUID sessionId = UUID.randomUUID();
        sessionContainer.createSession(sessionId, mockDriver, "chrome", "desktop");

        boolean removed = sessionContainer.removeSession(sessionId);

        assertTrue(removed);
        assertEquals(0, sessionContainer.getActiveSessionCount());
        verify(mockDriver).quit();
    }

    @Test
    void removeSession_shouldReturnFalseWhenSessionNotFound() {
        boolean removed = sessionContainer.removeSession(UUID.randomUUID());

        assertFalse(removed);
    }

    @Test
    void sessionExists_shouldReturnTrueForExistingSession() {
        UUID sessionId = UUID.randomUUID();
        sessionContainer.createSession(sessionId, mockDriver, "chrome", "desktop");

        assertTrue(sessionContainer.sessionExists(sessionId));
    }

    @Test
    void sessionExists_shouldReturnFalseForNonExistingSession() {
        assertFalse(sessionContainer.sessionExists(UUID.randomUUID()));
    }

    @Test
    void getAllSessionIds_shouldReturnAllActiveSessionIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        WebDriver driver2 = mock(WebDriver.class);

        sessionContainer.createSession(id1, mockDriver, "chrome", "desktop");
        sessionContainer.createSession(id2, driver2, "firefox", "desktop");

        var sessionIds = sessionContainer.getAllSessionIds();

        assertEquals(2, sessionIds.size());
        assertTrue(sessionIds.contains(id1));
        assertTrue(sessionIds.contains(id2));
    }

    @Test
    void closeAllSessions_shouldCloseAllDrivers() {
        WebDriver driver2 = mock(WebDriver.class);
        sessionContainer.createSession(UUID.randomUUID(), mockDriver, "chrome", "desktop");
        sessionContainer.createSession(UUID.randomUUID(), driver2, "firefox", "desktop");

        int closed = sessionContainer.closeAllSessions();

        assertEquals(2, closed);
        assertEquals(0, sessionContainer.getActiveSessionCount());
        verify(mockDriver).quit();
        verify(driver2).quit();
    }

    @Test
    void updateSessionUrl_shouldUpdateMetadata() {
        UUID sessionId = UUID.randomUUID();
        sessionContainer.createSession(sessionId, mockDriver, "chrome", "desktop");

        sessionContainer.updateSessionUrl(sessionId, "https://example.com");

        Optional<SessionMetadata> metadata = sessionContainer.getSessionMetadata(sessionId);
        assertTrue(metadata.isPresent());
        assertEquals("https://example.com", metadata.get().getCurrentUrl());
    }

    @Test
    void updateSessionState_shouldUpdateMetadata() {
        UUID sessionId = UUID.randomUUID();
        sessionContainer.createSession(sessionId, mockDriver, "chrome", "desktop");

        sessionContainer.updateSessionState(sessionId, SessionMetadata.SessionState.IDLE);

        Optional<SessionMetadata> metadata = sessionContainer.getSessionMetadata(sessionId);
        assertTrue(metadata.isPresent());
        assertEquals(SessionMetadata.SessionState.IDLE, metadata.get().getState());
    }
}

