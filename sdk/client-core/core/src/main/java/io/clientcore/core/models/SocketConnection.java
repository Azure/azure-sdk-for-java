// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

/**
 * Class to hold the SocketConnection and its properties
 */
public final class SocketConnection {
    private final Socket socket;
    private OutputStream socketOutputStream;
    private BufferedInputStream socketInputStream;
    private final SocketConnectionProperties connectionProperties;
    private boolean canBeReused = false;

    SocketConnection(Socket socket, SocketConnectionProperties socketConnectionProperties) {
        this.socket = socket;
        this.connectionProperties = socketConnectionProperties;
    }

    /**
     * Get the output stream of the socket
     * @return the output stream
     * @throws IOException if an I/O error occurs
     */
    public OutputStream getSocketOutputStream() throws IOException {
        if (socketOutputStream == null) {
            socketOutputStream = socket.getOutputStream();
        }
        return socketOutputStream;
    }

    /**
     * Get the input stream of the socket
     * @return the input stream
     * @throws IOException if an I/O error occurs
     */
    public BufferedInputStream getSocketInputStream() throws IOException {
        if (socketInputStream == null) {
            socketInputStream = new BufferedInputStream(socket.getInputStream());
        }
        return socketInputStream;
    }

    /**
     * Mark the connection as available for reuse
     */
    public void markAvailableForReuse() {
        this.canBeReused = true;
    }

    /**
     * Returns true if this connection has been used.
     */
    public boolean isAvailableForReuse() {
        return canBeReused;
    }

    /**
     * Close the socket and its streams
     * @throws IOException if an I/O error occurs
     */
    public void closeSocketAndStreams() throws IOException {
        if (socketInputStream != null) {
            socketInputStream.close();
        }
        if (socketOutputStream != null) {
            socketOutputStream.close();
        }
        socket.close();
    }

    Socket getSocket() {
        return socket;
    }

    SocketConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * Check if the connection can be reused
     * @return true if the connection can be reused, false otherwise
     */
    boolean canBeReused() {
        return !socket.isClosed()
            && !socket.isInputShutdown()
            && !socket.isOutputShutdown();
    }

    /**
     * Class to hold the properties of the socket connection
     */
    public static final class SocketConnectionProperties {
        private final URL requestUrl;
        private final String host;
        private final String port;
        private final SSLSocketFactory sslSocketFactory;

        /**
         * Creates a new instance of SocketConnectionProperties
         *
         * @param requestUrl the HTTP request url
         * @param host the host name
         * @param port the port number
         */
        public SocketConnectionProperties(URL requestUrl, String host, String port, SSLSocketFactory sslSocketFactory) {
            this.requestUrl = requestUrl;
            this.host = host;
            this.port = port;
            this.sslSocketFactory = sslSocketFactory;
        }

        @Override public boolean equals(Object other) {
            if (other instanceof SocketConnectionProperties) {
                SocketConnectionProperties that = (SocketConnectionProperties) other;
                boolean p = Objects.equals(this.host, that.host)
                    && this.port.equals(that.port);
                return p;

            }
            return false;
        }

        @Override public int hashCode() {
            return Objects.hash(host, port);
        }

        /**
         * Get the HTTP request URL
         * @return the HTTP request URL
         */
        public URL getRequestUrl() {
            return requestUrl;
        }

        /**
         * Get the SSL socket factory
         * @return SSL socket factory
         */
        public SSLSocketFactory getSslSocketFactory() {
            return sslSocketFactory;
        }
    }
}
