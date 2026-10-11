package io.ccw.webdav.purge;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

import io.ccw.webdav.purge.webdav.WebDavConnection;

/**
 * Entry point of the webdav-purge command line tool.
 *
 * <p>The tool lists the direct content of the given WebDAV directory, deletes
 * every entry and finally verifies that the directory is empty. It is meant
 * for scenarios where mounting the WebDAV share locally is too heavy weight.</p>
 *
 * <p>Example invocation:</p>
 * <pre>
 * java -jar webdav-purge-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
 *     --url https://example.com/webdav \
 *     --username alice --password secret \
 *     --directory uploads/outbox
 * </pre>
 */
public final class WebDavPurge {

    /**
     * Prevents instantiation of this utility class.
     */
    private WebDavPurge() {
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments as passed by the JVM
     */
    public static void main(String[] args) {
        final ParameterContainer parameters = new ParameterContainer();
        final CommandLine commandLine = new CommandLine(parameters);

        try {
            commandLine.execute(args);
        } catch (MissingParameterException e) {
            System.err.println(e.getMessage());
            System.err.println();
            commandLine.usage(System.err);
            return;
        }

        final WebDavConnection connection = new WebDavConnection(parameters.getUrl(), parameters.getUser(),
                parameters.getPassword());
        connection.connect();
        final String directory = parameters.getDirectoryToPurge();

        try {
            connection.deleteDirectoryContent(directory);
        } catch (Exception e) {
            throw new WebDavPurgeException("Error while purging WebDAV directory '" + directory
                    + "': " + e.getMessage(), e);
        }

        try {
            if (connection.isDirectoryEmpty(directory)) {
                System.out.println("WebDAV directory '" + directory + "' successfully purged.");
                return;
            }
        } catch (Exception e) {
            throw new WebDavPurgeException("Error while verifying WebDAV directory '" + directory
                    + "': " + e.getMessage(), e);
        }
        throw new WebDavPurgeException(
                "Error while purging WebDAV directory '" + directory + "': the directory is not empty.");
    }
}
