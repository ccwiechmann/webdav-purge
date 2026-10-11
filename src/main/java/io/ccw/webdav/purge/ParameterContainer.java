package io.ccw.webdav.purge;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command line options accepted by the webdav-purge tool.
 *
 * <p>The option names and short letters mirror the legacy JCommander based
 * interface so existing scripts and shell aliases keep working.</p>
 */
@Command(name = "webdav-purge", version = "webdav-purge 0.0.1-SNAPSHOT",
        description = "Purges the content of a directory on a WebDAV server.")
public class ParameterContainer {

    @Option(names = {"-u", "--url"}, required = true,
            description = "URL of the WebDAV server (example: https://example.com/webdav).")
    private String url;

    @Option(names = {"-n", "--username"}, required = true,
            description = "User name for the WebDAV connection.")
    private String user;

    @Option(names = {"-p", "--password"}, required = true,
            description = "Password for the WebDAV connection.")
    private String password;

    @Option(names = {"-d", "--directory"}, required = true,
            description = "The directory on the WebDAV server whose content shall be deleted.")
    private String directoryToPurge;

    /**
     * Returns the base URL of the WebDAV server.
     *
     * @return the base URL, never {@code null} after successful parsing
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the user name used for authentication.
     *
     * @return the user name, never {@code null} after successful parsing
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the password used for authentication.
     *
     * @return the password, never {@code null} after successful parsing
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the directory whose content shall be purged.
     *
     * @return the directory path, never {@code null} after successful parsing
     */
    public String getDirectoryToPurge() {
        return directoryToPurge;
    }
}
