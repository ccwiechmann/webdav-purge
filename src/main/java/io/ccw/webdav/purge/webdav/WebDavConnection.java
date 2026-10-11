package io.ccw.webdav.purge.webdav;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * A thin wrapper around the Sardine WebDAV client that knows how to purge the
 * direct content of one directory.
 *
 * <p>The class is intentionally small: it only performs the operations a
 * purge run needs. Instances are stateful, call {@link #connect()} before any
 * other method and treat each call to {@link #connect()} as a fresh
 * session.</p>
 */
public class WebDavConnection {

    private final String url;

    private final String user;

    private final String password;

    private Sardine connection;

    /**
     * Creates a new connection.
     *
     * @param url      base URL of the WebDAV server, e.g. {@code https://example.com/remote.php/dav/files/alice}
     * @param user     user name for basic authentication
     * @param password password for basic authentication
     */
    public WebDavConnection(String url, String user, String password) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
    }

    /**
     * Establishes the underlying Sardine session using the configured
     * credentials.
     */
    public void connect() {
        connection = SardineFactory.begin(user, password);
    }

    /**
     * Returns the active Sardine session, intended for testing purposes.
     *
     * @return the Sardine session active after the most recent call to
     *         {@link #connect()}
     */
    public Sardine getSardine() {
        return connection;
    }

    /**
     * Returns the paths of all direct entries inside the given directory.
     *
     * <p>The path of the directory itself is excluded from the result.
     * Entries are returned exactly as the WebDAV server reports them, which
     * usually includes a leading slash and the base path of the server.</p>
     *
     * @param directory directory to list, relative to the server base URL
     * @return paths of all direct entries, empty if the directory is empty
     * @throws IOException          if a communication problem occurs
     * @throws URISyntaxException   if the built URL cannot be parsed
     */
    public List<String> listDirectoryContent(String directory) throws IOException, URISyntaxException {
        final String encoded = buildEncodedPath(directory);
        final List<DavResource> resources = connection.list(url + encoded);
        return resources.stream()
                .map(DavResource::getPath)
                .filter(path -> !path.equals("/" + directory))
                .collect(Collectors.toList());
    }

    /**
     * Deletes every direct entry of the given directory.
     *
     * <p>Deleting a single entry does not abort the whole purge; failures are
     * reported on standard error so remaining entries can be inspected by
     * {@link #listDirectoryContent(String)}.</p>
     *
     * @param directory directory to purge, relative to the server base URL
     * @throws IOException      if listing the directory fails
     * @throws URISyntaxException if the built URL cannot be parsed
     */
    public void deleteDirectoryContent(String directory) throws IOException, URISyntaxException {
        final List<String> content = listDirectoryContent(directory);
        content.forEach(this::deleteFile);
    }

    /**
     * Checks whether a directory contains no direct entries.
     *
     * @param directory directory to check, relative to the server base URL
     * @return {@code true} if the directory is empty, otherwise {@code false}
     * @throws IOException      if a communication problem occurs
     * @throws URISyntaxException if the built URL cannot be parsed
     */
    public boolean isDirectoryEmpty(String directory) throws IOException, URISyntaxException {
        return listDirectoryContent(directory).isEmpty();
    }

    private void deleteFile(String path) {
        try {
            System.out.println("Delete file: " + path);
            final URIBuilder builder = new URIBuilder(url);
            builder.setPath(path);
            connection.delete(builder.build().toString());
        } catch (IOException | URISyntaxException e) {
            System.err.println("Could not delete '" + path + "': " + e.getMessage());
        }
    }

    private String buildEncodedPath(String directory) throws URISyntaxException {
        final URIBuilder builder = new URIBuilder(url);
        builder.setPath("/" + directory);
        return builder.build().getRawPath();
    }
}
