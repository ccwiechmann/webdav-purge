package io.ccw.webdav.purge;

import java.io.IOException;
import java.net.URISyntaxException;

import com.beust.jcommander.JCommander;

import io.ccw.webdav.purge.webdav.WebDavConnection;

public class WebDavPurge {

    public static void main(String[] args) throws IOException, URISyntaxException {

        ParameterContainer parameters = new ParameterContainer();
        JCommander jCommander = new JCommander(parameters);
        jCommander.setProgramName(WebDavPurge.class.getSimpleName());
        jCommander.parseWithoutValidation(args);

        if (parameters.isHelp()) {
            jCommander.usage();
            return;
        }

        parameters = new ParameterContainer();
        jCommander = new JCommander(parameters);
        jCommander.setProgramName(WebDavPurge.class.getSimpleName());
        jCommander.parse(args);

        final WebDavConnection connection = new WebDavConnection(parameters.getUrl(), parameters.getUser(),
                parameters.getPassword());
        connection.connect();

        connection.deleteDirectoryContent(parameters.getDirectoryToPurge());

        final boolean result = connection.isDirectoryEmpty(parameters.getDirectoryToPurge());

        if (result) {
            System.out.println("WebDAV directory '" + parameters.getDirectoryToPurge() + "' successfully purged.");
        } else {
            System.err.println("Error while purging WebDAV directory '" + parameters.getDirectoryToPurge() + "'.");
        }
    }
}
