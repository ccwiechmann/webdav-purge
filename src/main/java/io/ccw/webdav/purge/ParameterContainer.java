package io.ccw.webdav.purge;

import com.beust.jcommander.Parameter;

public class ParameterContainer {

    @Parameter(names = { "-u",
            "--url" }, description = "URL of WebDAV server (Example: https://something.com)", required = true)
    private String url;

    @Parameter(names = { "-n", "--username" }, description = "User name for the WebDAV connection", required = true)
    private String user;

    @Parameter(names = { "-p", "--password" }, description = "Password for the WebDAV connection", required = true)
    private String password;

    @Parameter(names = { "-d",
            "--directory" }, description = "The directory on the WebDAV server to delete the content of", required = true)
    private String directoryToPurge;

    @Parameter(names = { "-h", "--help" }, description = "Displays this help", help = true)
    private boolean help = false;

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDirectoryToPurge() {
        return directoryToPurge;
    }

    public boolean isHelp() {
        return help;
    }
}
