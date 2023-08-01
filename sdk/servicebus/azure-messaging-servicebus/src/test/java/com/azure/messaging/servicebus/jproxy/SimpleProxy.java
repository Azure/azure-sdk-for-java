// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.jproxy;

import com.azure.core.util.logging.ClientLogger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Creates a simple unauthenticated service on localhost that proxies requests.
 */
public class SimpleProxy implements ProxyServer {
    static final int PROXY_BUFFER_SIZE = 1024;

    private static final String HOSTNAME = "localhost";

    private static final ClientLogger LOGGER = new ClientLogger(SimpleProxy.class);
    private final AtomicBoolean isRunning;
    private final InetSocketAddress host;
    private final List<ProxyNegotiationHandler> connectedClients = new ArrayList<>();

    private Consumer<Throwable> onErrorHandler;
    private AsynchronousServerSocketChannel serverSocket;

    public SimpleProxy(final int port) {
        this.host = new InetSocketAddress(HOSTNAME, port);
        this.isRunning = new AtomicBoolean(false);
    }

    @Override
    public InetSocketAddress getHost() {
        return host;
    }

    @Override
    public void start(final Consumer<Throwable> onError) throws IOException {
        if (isRunning.getAndSet(true)) {
            throw new IllegalStateException("ProxyServer is already running.");
        }

        onErrorHandler = onError != null
            ? onError
            : error -> LOGGER.error("Error occurred when running proxy.", error);

        LOGGER.info("Opening proxy server on: '{}'", host);

        serverSocket = AsynchronousServerSocketChannel.open();
        serverSocket.bind(host);
        serverSocket.accept(serverSocket, new ClientConnectedHandler());
    }

    @Override
    public void stop() throws IOException {
        if (isRunning.getAndSet(false)) {
            serverSocket.close();

            for (ProxyNegotiationHandler client : connectedClients) {
                try {
                    client.close();
                } catch (IOException e) {
                    final AsynchronousSocketChannel clientSocket = client.connection.getClientSocket();
                    LOGGER.warning("Error closing client: {}.", clientSocket.getRemoteAddress(), e);
                }
            }
        }
    }

    /**
     * Handler invoked when a client connects to the proxy server.
     */
    private class ClientConnectedHandler implements
        CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

        /**
         * When a client has successfully connected to the proxy server.
         *
         * @param client Client that connects to the service.
         * @param serverSocket The socket this proxy server is running on.
         */
        @Override
        public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel serverSocket) {
            try {
                LOGGER.info("Client connected from: {}", client.getRemoteAddress());
            } catch (IOException error) {
                LOGGER.error("Unable to get socket address for: {}", client, error);
            }

            // Invoke again to accept additional client connections.
            serverSocket.accept(serverSocket, this);

            try {
                connectedClients.add(new ProxyNegotiationHandler(client));
            } catch (IOException e) {
                LOGGER.error("Error creating proxy negotiation handler.", e);
                onErrorHandler.accept(e);
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
            isRunning.set(false);

            if (exc instanceof AsynchronousCloseException) {
                LOGGER.info("Closed proxy server.");
            } else {
                onErrorHandler.accept(exc);
            }
        }
    }

    private static class ProxyNegotiationHandler implements Closeable {
        private final ConnectionProperties connection;

        ProxyNegotiationHandler(AsynchronousSocketChannel clientSocket)
            throws IOException {
            Objects.requireNonNull(clientSocket);

            final AsynchronousSocketChannel serviceSocket = AsynchronousSocketChannel.open();
            connection = new ConnectionProperties(ProxyConnectionState.PROXY_NOT_STARTED, clientSocket, serviceSocket);
            final ReadWriteState state = new ReadWriteState(ReadWriteState.Target.SERVICE,
                ByteBuffer.allocate(PROXY_BUFFER_SIZE), true);

            clientSocket.read(state.getBuffer(), state, new ReadWriteHandler(connection));
        }

        @Override
        public void close() throws IOException {
            connection.close();
        }
    }
}
