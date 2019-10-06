// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.jproxy;

import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class ConnectionProperties implements Closeable {
    private final ClientLogger logger = new ClientLogger(ConnectionProperties.class);
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final AsynchronousSocketChannel clientSocket;
    private final AsynchronousSocketChannel outgoingSocket;
    private final Consumer<Throwable> onErrorHandler;

    private volatile ProxyConnectionState proxyConnectionState;

    ConnectionProperties(ProxyConnectionState proxyConnectionState, AsynchronousSocketChannel clientSocket,
                         AsynchronousSocketChannel outgoingSocket, Consumer<Throwable> onErrorHandler) {
        this.proxyConnectionState = Objects.requireNonNull(proxyConnectionState);
        this.clientSocket = Objects.requireNonNull(clientSocket);
        this.outgoingSocket = Objects.requireNonNull(outgoingSocket);
        this.onErrorHandler = Objects.requireNonNull(onErrorHandler);
    }

    /**
     * Gets the socket this proxy is connecting to external services with.
     *
     * @return The socket this proxy is connecting to external services with.
     */
    AsynchronousSocketChannel getOutgoingSocket() {
        return outgoingSocket;
    }

    /**
     * Gets the address of the client connecting through this proxy.
     *
     * @return The address of the client conencting through this proxy.
     */
    AsynchronousSocketChannel getClientSocket() {
        return clientSocket;
    }

    /**
     * Gets the current state of the proxy negotiation.
     *
     * @return The current state of the proxy negotiation.
     */
    ProxyConnectionState getProxyConnectionState() {
        return proxyConnectionState;
    }

    /**
     * Sets the current state of the proxy negotiation.
     *
     * @param proxyConnectionState The current state of the proxy negotiation.
     */
    void setProxyConnectionState(ProxyConnectionState proxyConnectionState) {
        this.proxyConnectionState = proxyConnectionState;
    }

    void onError(Throwable error) {
        onErrorHandler.accept(error);
    }

    @Override
    public String toString() {
        SocketAddress clientAddress = null;
        try {
            clientAddress = clientSocket.getRemoteAddress();
        } catch (IOException ignored) {
            // It's possible to get this IOException when we've closed the socket after disposing of our connection.
        }

        SocketAddress serviceAddress = null;
        try {
            serviceAddress = outgoingSocket.getRemoteAddress();
        } catch (IOException ignored) {
            // It's possible to get this IOException when we've closed the socket after disposing of the client.
        }

        return String.format("ConnectionProperties [state='%s', client='%s', service='%s']",
            proxyConnectionState,
            clientAddress != null ? clientAddress.toString() : "n/a",
            serviceAddress != null ? serviceAddress.toString() : "n/a");
    }

    @Override
    public void close() throws IOException {
        if (isClosed.getAndSet(true)) {
            logger.info("Connection already closed.");
            return;
        }

        logger.info("Closing connection.");

        setProxyConnectionState(ProxyConnectionState.PROXY_CLOSED);
        clientSocket.close();
        outgoingSocket.close();
    }
}
