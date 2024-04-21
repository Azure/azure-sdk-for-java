// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.implementation.util.CoreUtils;
import io.clientcore.core.models.SocketConnection.SocketConnectionProperties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Class maintaining a cache of socket connections
 */
public final class SocketConnectionCache {

    private static SocketConnectionCache INSTANCE;
    private static final Map<SocketConnectionProperties, List<SocketConnection>> connectionPool
        = new HashMap<SocketConnectionProperties, List<SocketConnection>>();
    private final int readTimeout;
    private final boolean keepConnectionAlive;
    private final int maxConnections;

    private SocketConnectionCache(boolean connectionKeepAlive, int maximumConnections, int readTimeout) {
        this.keepConnectionAlive = connectionKeepAlive;
        this.maxConnections = maximumConnections;
        this.readTimeout = readTimeout;
    }

    /**
     * Get the instance of the SocketConnectionCache (Singleton)
     * @param connectionKeepAlive boolean to keep the connection alive
     * @param maximumConnections maximum number of connections to keep alive
     * @param readTimeout read timeout for the connection
     * @return the instance of the SocketConnectionCache if it exists, else create a new one
     */
    public static synchronized SocketConnectionCache getInstance(boolean connectionKeepAlive, int maximumConnections,
        int readTimeout) {
        if (INSTANCE == null) {
            INSTANCE = new SocketConnectionCache(connectionKeepAlive, maximumConnections, readTimeout);
        }
        return INSTANCE;
    }


    /**
     * Get a {@link SocketConnection connection} from the cache based on the {@link SocketConnectionProperties}
     * or create a new one
     * @param socketConnectionProperties the properties of the connection
     * @return the connection
     * @throws IOException if an I/O error occurs
     */
    public SocketConnection get(SocketConnectionProperties socketConnectionProperties) throws IOException {
        SocketConnection connection = null;
        // try to get a connection from the cache
        synchronized (connectionPool) {
            List<SocketConnection> connections = connectionPool.get(socketConnectionProperties);
            while (!CoreUtils.isNullOrEmpty(connections)) {
                connection = connections.remove(connections.size() - 1);
                if (connections.isEmpty()) {
                    connectionPool.remove(socketConnectionProperties);
                    connections = null;
                } else {
                    connectionPool.put(socketConnectionProperties, connections);
                }
                // keep removing connections from list until we find a use-able one, disregard other connections in use
                if (connection.canBeReused()) {
                    return connection;
                }
            }
        }

        // If no connection is available, create a new one
        if (connection == null) {
            // If the request is a PATCH request, we need to use a socket connection
            connection = getSocketSocketConnection(socketConnectionProperties, readTimeout, keepConnectionAlive);
        }

        synchronized (connectionPool) {
            List<SocketConnection> connections = connectionPool.get(socketConnectionProperties);
            if (connections == null) {
                connections = new ArrayList<>();
                connectionPool.put(socketConnectionProperties, connections);
            }
        }
        return connection;
    }

    /**
     * Clear the cache of connections
     */
    static void clearCache() {
        synchronized (connectionPool) {
            connectionPool.clear();
        }
        INSTANCE = null;
    }

    /**
     * Reuse the {@link SocketConnection connection} if it can be reused, else close the connection
     * @param connection the connection to be reused
     * @throws IOException if an I/O error occurs
     */
    public void reuseConnection(SocketConnection connection) throws IOException {
        if (maxConnections > 0 && keepConnectionAlive && connection.canBeReused()) {
            SocketConnectionProperties connectionProperties = connection.getConnectionProperties();

            // try and put the connection back in the pool
            synchronized (connectionPool) {
                List<SocketConnection> connections = connectionPool.get(connectionProperties);
                if (connections == null) {
                    connections = new ArrayList<SocketConnection>();
                    connectionPool.put(connectionProperties, connections);
                }
                if (connections.size() < maxConnections) {
                    // mark the connection as available for reuse
                    connection.markAvailableForReuse();
                    connection.getSocket().setKeepAlive(true);
                    connections.add(connection);
                    connectionPool.put(connectionProperties, connections);
                }
                return; // keep the connection open
            }
        }

        // close streams when connection cannot be reused.
        connection.closeSocketAndStreams();
    }

    private static SocketConnection getSocketSocketConnection(SocketConnectionProperties socketConnectionProperties,
        int readTimeout, boolean keepConnectionAlive) throws IOException {
        SocketConnection connection;
        URL requestUrl = socketConnectionProperties.getRequestUrl();
        String protocol = requestUrl.getProtocol();
        String host = requestUrl.getHost();
        int port = requestUrl.getPort();

        Socket socket;
        socket = protocol.equals("https")
            ? (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port)
            : new Socket(host, port);

        if (keepConnectionAlive) {
            socket.setKeepAlive(true);
        }
        if (readTimeout != -1) {
            socket.setSoTimeout(readTimeout);
        }
        connection = new SocketConnection(socket, socketConnectionProperties);
        return connection;
    }
}
