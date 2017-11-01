package io.ccw.webdav.purge.webdav;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

public class WebDavConnection {

    private final String url;

    private final String user;

    private final String password;

    private Sardine connection;

    public WebDavConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void connect() {
        connection = SardineFactory.begin(user, password);
    }

    private List<String> getContentOfDirectory(String directory) throws IOException, URISyntaxException {
        final URIBuilder builder = new URIBuilder(url);
        builder.setPath("/" + directory);
        final String encoded = builder.build().getRawPath();

        final List<DavResource> resources = connection.list(url + encoded);
        return resources.stream().map(r -> r.getPath()).filter(s -> !s.equals("/" + directory))
                .collect(Collectors.toList());
    }

    public void deleteDirectoryContent(String directory) throws IOException, URISyntaxException {
        final List<String> content = getContentOfDirectory(directory);
        content.stream().forEach(s -> deleteFile(s));
    }

    private void deleteFile(String file) {
        try {
            System.out.println("Delete file: " + file);

            final URIBuilder builder = new URIBuilder(url);
            builder.setPath(file);
            final String encoded = builder.build().getRawPath();

            connection.delete(url + encoded);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean isDirectoryEmpty(String directory) throws IOException, URISyntaxException {
        return getContentOfDirectory(directory).isEmpty();
    }
}
