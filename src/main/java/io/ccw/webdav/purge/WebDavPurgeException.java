package io.ccw.webdav.purge;

/**
 * Checked exception that reports a failed purge or verification of a WebDAV
 * directory to the caller as a user readable message.
 */
public class WebDavPurgeException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message user readable description of the failure
     */
    public WebDavPurgeException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message user readable description of the failure
     * @param cause   underlying cause, may be {@code null}
     */
    public WebDavPurgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
