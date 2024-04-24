// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.implementation.util.CoreUtils;
import io.clientcore.core.models.SocketConnection.SocketConnectionProperties;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to maintain a cache of socket connections
 */
public final class SocketConnectionCache {

    private static SocketConnectionCache instance;
    private static final Map<SocketConnectionProperties, List<SocketConnection>> CONNECTION_POOL
        = new ConcurrentHashMap<>();
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
        if (instance == null) {
            instance = new SocketConnectionCache(connectionKeepAlive, maximumConnections, readTimeout);
        }
        return instance;
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
        synchronized (CONNECTION_POOL) {
            List<SocketConnection> connections = CONNECTION_POOL.get(socketConnectionProperties);
            while (!CoreUtils.isNullOrEmpty(connections)) {
                connection = connections.remove(connections.size() - 1);
                if (connections.isEmpty()) {
                    CONNECTION_POOL.remove(socketConnectionProperties);
                    connections = null;
                } else {
                    CONNECTION_POOL.put(socketConnectionProperties, connections);
                }
                // keep removing connections from list until we find a use-a one, disregard other connections in use
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

        synchronized (CONNECTION_POOL) {
            List<SocketConnection> connections = CONNECTION_POOL.get(socketConnectionProperties);
            if (connections == null) {
                connections = new ArrayList<>();
                CONNECTION_POOL.put(socketConnectionProperties, connections);
            }
        }
        return connection;
    }

    /**
     * Clear the cache of connections
     */
    static void clearCache() {
        synchronized (CONNECTION_POOL) {
            CONNECTION_POOL.clear();
        }
        instance = null;
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
            synchronized (CONNECTION_POOL) {
                List<SocketConnection> connections = CONNECTION_POOL.get(connectionProperties);
                if (connections == null) {
                    connections = new ArrayList<SocketConnection>();
                    CONNECTION_POOL.put(connectionProperties, connections);
                }
                if (connections.size() < maxConnections) {
                    // mark the connection as available for reuse
                    connection.markAvailableForReuse();
                    connections.add(connection);
                    CONNECTION_POOL.put(connectionProperties, connections);
                }
                return; // keep the connection open
            }
        }

        // close streams when connection cannot be reused.
        connection.closeSocketAndStreams();
    }

    private static SocketConnection getSocketSocketConnection(SocketConnectionProperties socketConnectionProperties,
        int readTimeout, boolean keepConnectionAlive) throws IOException {
        URL requestUrl = socketConnectionProperties.getRequestUrl();
        String protocol = requestUrl.getProtocol();
        String host = requestUrl.getHost();
        int port = requestUrl.getPort();

        Socket socket;
        if ("https".equals(protocol)) {
            SSLSocketFactory sslSocketFactory = socketConnectionProperties.getSslSocketFactory();
            socket = sslSocketFactory.createSocket(host, port);
        } else {
            socket = new Socket(host, port);
        }

        if (keepConnectionAlive) {
            socket.setKeepAlive(true);
            socket.setReuseAddress(true);
        }
        if (readTimeout != -1) {
            socket.setSoTimeout(readTimeout);
        }

        return new SocketConnection(socket, socketConnectionProperties);
    }
}
