// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.models.SocketConnection.SocketConnectionProperties;
import io.clientcore.core.util.ClientLogger;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;

/**
 * Class to maintain a cache of socket connections
 */
public final class SocketConnectionCache {
    private static final ClientLogger LOGGER = new ClientLogger(SocketConnectionCache.class);
    private static SocketConnectionCache instance;
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Map<SocketConnectionProperties, List<SocketConnection>> CONNECTION_POOL
        = new ConcurrentHashMap<>();
    private final boolean keepConnectionAlive;
    private final int maxConnections;

    private SocketConnectionCache(boolean connectionKeepAlive, int maximumConnections) {
        this.keepConnectionAlive = connectionKeepAlive;
        this.maxConnections = maximumConnections;
    }

    /**
     * Get the instance of the SocketConnectionCache (Singleton)
     *
     * @param connectionKeepAlive boolean to keep the connection alive
     * @param maximumConnections maximum number of connections to keep alive
     * @return the instance of the SocketConnectionCache if it exists, else create a new one
     */
    public static SocketConnectionCache getInstance(boolean connectionKeepAlive, int maximumConnections) {
        LOCK.lock();
        try {
            if (instance == null) {
                instance = new SocketConnectionCache(connectionKeepAlive, maximumConnections);
            }
            return instance;
        } finally {
            LOCK.unlock();
        }
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
        LOCK.lock();
        try {

            List<SocketConnection> connections = CONNECTION_POOL.get(socketConnectionProperties);
            while (!isNullOrEmpty(connections)) {
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
        } finally {
            LOCK.unlock();
        }

        // If no connection is available, create a new one
        if (connection == null) {
            // If the request is a PATCH request, we need to use a socket connection
            connection = getSocketSocketConnection(socketConnectionProperties, keepConnectionAlive);
        }

        LOCK.lock();
        try {
            List<SocketConnection> connections = CONNECTION_POOL.get(socketConnectionProperties);
            if (connections == null) {
                connections = new ArrayList<>();
                CONNECTION_POOL.put(socketConnectionProperties, connections);
            }
        } finally {
            LOCK.unlock();
        }
        return connection;
    }

    /**
     * Clear the cache of connections
     */
    static void clearCache() {
        LOCK.lock();
        try {
            CONNECTION_POOL.clear();
        } finally {
            LOCK.unlock();
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
            LOCK.lock();
            try {
                List<SocketConnection> connections = CONNECTION_POOL.get(connectionProperties);
                if (connections == null) {
                    connections = new ArrayList<SocketConnection>();
                    CONNECTION_POOL.put(connectionProperties, connections);
                }
                if (connections.size() < maxConnections) {
                    BufferedInputStream is = connection.getSocketInputStream();
                    // buildResponse might not consume the entire inputstream,
                    // so we need to skip the remaining bytes to reuse the connection (keep alive).
                    skipRemaining(is);
                    // mark the connection as available for reuse
                    connection.markAvailableForReuse();
                    connections.add(connection);
                    CONNECTION_POOL.put(connectionProperties, connections);
                }
                return; // keep the connection open
            } finally {
                LOCK.unlock();
            }
        }

        // close streams when connection cannot be reused.
        connection.closeSocketAndStreams();
    }

    private static SocketConnection getSocketSocketConnection(SocketConnectionProperties socketConnectionProperties,
        boolean keepConnectionAlive) throws IOException {
        String protocol = socketConnectionProperties.getProtocol();
        String host = socketConnectionProperties.getHost();
        int port = socketConnectionProperties.getPort();
        int readTimeout = socketConnectionProperties.getReadTimeout();

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

    private void skipRemaining(InputStream is) throws IOException {
        long count = is.available();
        long pos = 0;
        int chunkSize = 4096; // Define your chunk size here

        while (pos < count) {
            long toSkip = Math.min(chunkSize, count - pos);
            long skipped = is.skip(toSkip);
            if (skipped == -1) {
                LOGGER.logThrowableAsError(new IOException("No data, can't skip " + count + " bytes"));
            }
            pos += skipped;
        }
    }
}
