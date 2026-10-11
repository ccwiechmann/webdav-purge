package io.ccw.webdav.purge;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

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
 *
 * <p>Exit codes: {@code 0} success, {@code 1} purge or verification failure,
 * {@code 2} invalid command line usage.</p>
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
     * <p>Invalid command line options are reported by picocli (message plus
     * usage on standard error) and exit with code {@code 2}. Any purge or
     * verification failure is reported as a single short message on standard
     * error (no stack trace) and exits with code {@code 1}.</p>
     *
     * @param args command line arguments as passed by the JVM
     */
    public static void main(String[] args) {
        final ParameterContainer parameters = new ParameterContainer();
        final CommandLine commandLine = new CommandLine(parameters);

        try {
            commandLine.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.err.println();
            commandLine.usage(System.err);
            System.exit(2);
        }

        try {
            purge(parameters);
        } catch (WebDavPurgeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Purges the directory given on the command line and verifies the result.
     *
     * @param parameters parsed command line options
     * @throws WebDavPurgeException if the listing, deletion or verification fails
     */
    private static void purge(final ParameterContainer parameters) {
        final WebDavConnection connection = new WebDavConnection(parameters.getUrl(), parameters.getUser(),
                parameters.getPassword());
        connection.connect();
        final String directory = parameters.getDirectoryToPurge();

        try {
            connection.deleteDirectoryContent(directory);
            if (!connection.isDirectoryEmpty(directory)) {
                throw new WebDavPurgeException("the directory is not empty after the purge");
            }
        } catch (WebDavPurgeException e) {
            throw e;
        } catch (Exception e) {
            throw new WebDavPurgeException("Error while purging WebDAV directory '" + directory
                    + "': " + e.getMessage(), e);
        }
        System.out.println("WebDAV directory '" + directory + "' successfully purged.");
    }
}
