package io.ccw.webdav.purge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

/**
 * Tests for the command line parsing of {@link ParameterContainer}.
 */
class ParameterContainerTest {

    /**
     * Asserts that a complete set of options is stored on the container.
     */
    @Test
    void parsesAllOptions() {
        final ParameterContainer parameters = new ParameterContainer();
        new CommandLine(parameters).parseArgs(
                "-u", "https://example.com/remote.php/dav/files/alice",
                "-n", "alice",
                "-p", "secret",
                "-d", "uploads/outbox");

        assertEquals("https://example.com/remote.php/dav/files/alice", parameters.getUrl());
        assertEquals("alice", parameters.getUser());
        assertEquals("secret", parameters.getPassword());
        assertEquals("uploads/outbox", parameters.getDirectoryToPurge());
    }

    /**
     * Asserts that the long option names are accepted as well.
     */
    @Test
    void parsesLongOptionNames() {
        final ParameterContainer parameters = new ParameterContainer();
        new CommandLine(parameters).parseArgs(
                "--url", "https://example.com",
                "--username", "bob",
                "--password", "hunter2",
                "--directory", "tmp");

        assertEquals("https://example.com", parameters.getUrl());
        assertEquals("bob", parameters.getUser());
        assertEquals("hunter2", parameters.getPassword());
        assertEquals("tmp", parameters.getDirectoryToPurge());
    }

    /**
     * Asserts that a missing required option is reported.
     */
    @Test
    void reportsMissingRequiredOptions() {
        final ParameterContainer parameters = new ParameterContainer();
        final CommandLine commandLine = new CommandLine(parameters);

        final MissingParameterException exception = assertThrows(MissingParameterException.class,
                () -> commandLine.parseArgs("--url", "https://example.com"));

        assertEquals(3, exception.getMissing().size());
        assertTrue(exception.getMessage().contains("--username"));
        assertTrue(exception.getMessage().contains("--password"));
        assertTrue(exception.getMessage().contains("--directory"));
    }
}
