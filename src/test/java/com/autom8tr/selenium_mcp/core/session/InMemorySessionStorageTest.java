package com.autom8tr.selenium_mcp.core.session;

import com.autom8tr.selenium_mcp.core.SessionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemorySessionStorageTest {

    @Mock
    private WebDriver mockDriver;

    private InMemorySessionStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemorySessionStorage();
    }

    @Test
    void save_shouldStoreSessionSuccessfully() {
        UUID sessionId = UUID.randomUUID();
        SessionMetadata metadata = new SessionMetadata(sessionId, "chrome", "desktop");

        storage.save(sessionId, mockDriver, metadata);

        assertTrue(storage.exists(sessionId));
        assertEquals(1, storage.getSessionCount());
    }

    @Test
    void save_shouldThrowExceptionForNullParameters() {
        assertThrows(SessionStorage.SessionStorageException.class, () ->
                storage.save(null, mockDriver, new SessionMetadata(UUID.randomUUID(), "chrome", "desktop")));

        assertThrows(SessionStorage.SessionStorageException.class, () ->
                storage.save(UUID.randomUUID(), null, new SessionMetadata(UUID.randomUUID(), "chrome", "desktop")));

        assertThrows(SessionStorage.SessionStorageException.class, () ->
                storage.save(UUID.randomUUID(), mockDriver, null));
    }

    @Test
    void getDriver_shouldReturnDriverWhenExists() {
        UUID sessionId = UUID.randomUUID();
        SessionMetadata metadata = new SessionMetadata(sessionId, "chrome", "desktop");
        storage.save(sessionId, mockDriver, metadata);

        Optional<WebDriver> result = storage.getDriver(sessionId);

        assertTrue(result.isPresent());
        assertSame(mockDriver, result.get());
    }

    @Test
    void getDriver_shouldReturnEmptyWhenNotExists() {
        Optional<WebDriver> result = storage.getDriver(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void getMetadata_shouldReturnMetadataWhenExists() {
        UUID sessionId = UUID.randomUUID();
        SessionMetadata metadata = new SessionMetadata(sessionId, "chrome", "desktop");
        storage.save(sessionId, mockDriver, metadata);

        Optional<SessionMetadata> result = storage.getMetadata(sessionId);

        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get().getSessionId());
    }

    @Test
    void remove_shouldRemoveSessionAndReturnDriver() {
        UUID sessionId = UUID.randomUUID();
        SessionMetadata metadata = new SessionMetadata(sessionId, "chrome", "desktop");
        storage.save(sessionId, mockDriver, metadata);

        WebDriver removed = storage.remove(sessionId);

        assertSame(mockDriver, removed);
        assertFalse(storage.exists(sessionId));
        assertEquals(0, storage.getSessionCount());
    }

    @Test
    void remove_shouldReturnNullWhenNotExists() {
        WebDriver removed = storage.remove(UUID.randomUUID());

        assertNull(removed);
    }

    @Test
    void getAllSessionIds_shouldReturnAllIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        storage.save(id1, mockDriver, new SessionMetadata(id1, "chrome", "desktop"));
        storage.save(id2, mock(WebDriver.class), new SessionMetadata(id2, "firefox", "desktop"));

        Set<UUID> ids = storage.getAllSessionIds();

        assertEquals(2, ids.size());
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));
    }

    @Test
    void touch_shouldUpdateLastAccessedTime() throws InterruptedException {
        UUID sessionId = UUID.randomUUID();
        SessionMetadata metadata = new SessionMetadata(sessionId, "chrome", "desktop");
        storage.save(sessionId, mockDriver, metadata);

        long initialTime = metadata.getLastAccessedAt().toEpochMilli();
        Thread.sleep(10);

        storage.touch(sessionId);

        Optional<SessionMetadata> updated = storage.getMetadata(sessionId);
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getLastAccessedAt().toEpochMilli() > initialTime);
    }

    @Test
    void closeAll_shouldCloseAllDrivers() {
        WebDriver driver2 = mock(WebDriver.class);
        storage.save(UUID.randomUUID(), mockDriver, new SessionMetadata(UUID.randomUUID(), "chrome", "desktop"));
        storage.save(UUID.randomUUID(), driver2, new SessionMetadata(UUID.randomUUID(), "firefox", "desktop"));

        int closed = storage.closeAll();

        assertEquals(2, closed);
        assertEquals(0, storage.getSessionCount());
        verify(mockDriver).quit();
        verify(driver2).quit();
    }

    @Test
    void isHealthy_shouldAlwaysReturnTrue() {
        assertTrue(storage.isHealthy());
    }
}

