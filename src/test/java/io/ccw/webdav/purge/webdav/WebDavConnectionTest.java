package io.ccw.webdav.purge.webdav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

/**
 * Unit tests for {@link WebDavConnection}.
 *
 * <p>The {@link Sardine} session used by the connection under test is a
 * Mockito mock that is installed directly into the connection's private
 * session field, so no network access is required.</p>
 */
class WebDavConnectionTest {

    private static final String BASE_URL = "https://example.com";

    private Sardine sardine;

    private WebDavConnection connection;

    /**
     * Injects the mock session into a fresh connection before every test.
     */
    @BeforeEach
    void setUp() {
        sardine = mock(Sardine.class);
        connection = new WebDavConnection(BASE_URL, "alice", "secret");
        installSession(connection, sardine);
        assertEquals(sardine, connection.getSardine());
    }

    /**
     * Installs the given Sardine session into the connection's private
     * session field via reflection.
     *
     * @param candidate connection to mutate
     * @param session   session to install
     */
    private static void installSession(final WebDavConnection candidate, final Sardine session) {
        try {
            final Field field = WebDavConnection.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(candidate, session);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("cannot inject the mock Sardine session", e);
        }
    }

    /**
     * Asserts that the directory itself is excluded from the listing and the
     * direct entries are returned in server order.
     *
     * @throws Exception if the connection fails to list the directory
     */
    @Test
    void listDirectoryContentExcludesTheDirectoryItself() throws Exception {
        final List<DavResource> stubbed = resources("/uploads/outbox", "/uploads/outbox/a.txt", "/uploads/outbox/sub");
        when(sardine.list(BASE_URL + "/uploads/outbox")).thenReturn(stubbed);

        assertEquals(List.of("/uploads/outbox/a.txt", "/uploads/outbox/sub"),
                connection.listDirectoryContent("uploads/outbox"));
    }

    /**
     * Asserts that every listed entry is deleted with the fully qualified
     * server url.
     *
     * @throws Exception if the purge cannot be executed
     */
    @Test
    void deleteDirectoryContentDeletesEveryEntry() throws Exception {
        final List<DavResource> stubbed = resources("/uploads/outbox", "/uploads/outbox/a.txt", "/uploads/outbox/sub");
        when(sardine.list(BASE_URL + "/uploads/outbox")).thenReturn(stubbed);

        connection.deleteDirectoryContent("uploads/outbox");

        verify(sardine).delete(BASE_URL + "/uploads/outbox/a.txt");
        verify(sardine).delete(BASE_URL + "/uploads/outbox/sub");
    }

    /**
     * Asserts that a delete failure of one entry does not prevent the
     * remaining entries from being deleted.
     *
     * @throws Exception if the purge cannot be executed
     */
    @Test
    void deleteDirectoryContentContinuesAfterFailure() throws Exception {
        final List<DavResource> stubbed = resources("/uploads/outbox", "/uploads/outbox/a.txt", "/uploads/outbox/sub");
        when(sardine.list(BASE_URL + "/uploads/outbox")).thenReturn(stubbed);
        doThrow(new IOException("boom")).when(sardine).delete(BASE_URL + "/uploads/outbox/a.txt");

        connection.deleteDirectoryContent("uploads/outbox");

        verify(sardine).delete(BASE_URL + "/uploads/outbox/a.txt");
        verify(sardine).delete(BASE_URL + "/uploads/outbox/sub");
    }

    /**
     * Asserts that a directory whose listing contains only itself is
     * reported as empty.
     *
     * @throws Exception if the check cannot be executed
     */
    @Test
    void isDirectoryEmptyReturnsTrueForEmptyListing() throws Exception {
        final List<DavResource> emptyListing = resources("/empty/dir");
        when(sardine.list(BASE_URL + "/empty/dir")).thenReturn(emptyListing);

        assertTrue(connection.isDirectoryEmpty("empty/dir"));
    }

    /**
     * Asserts that a directory with entries is reported as non empty.
     *
     * @throws Exception if the check cannot be executed
     */
    @Test
    void isDirectoryEmptyReturnsFalseForNonEmptyListing() throws Exception {
        final List<DavResource> nonEmptyListing = resources("/uploads/outbox", "/uploads/outbox/a.txt");
        when(sardine.list(BASE_URL + "/uploads/outbox")).thenReturn(nonEmptyListing);

        assertFalse(connection.isDirectoryEmpty("uploads/outbox"));
    }

    /**
     * Asserts that null constructor arguments are rejected with a descriptive
     * message.
     */
    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new WebDavConnection(null, "u", "p"));
        assertThrows(NullPointerException.class, () -> new WebDavConnection(BASE_URL, null, "p"));
        assertThrows(NullPointerException.class, () -> new WebDavConnection(BASE_URL, "u", null));
    }

    /**
     * Builds mocked {@link DavResource} instances for the given paths.
     *
     * @param paths paths reported by {@link DavResource#getPath()}
     * @return mocked resources, one per path
     */
    private static List<DavResource> resources(final String... paths) {
        final List<DavResource> result = new ArrayList<>();
        for (String path : paths) {
            final DavResource resource = mock(DavResource.class);
            when(resource.getPath()).thenReturn(path);
            result.add(resource);
        }
        return result;
    }
}
