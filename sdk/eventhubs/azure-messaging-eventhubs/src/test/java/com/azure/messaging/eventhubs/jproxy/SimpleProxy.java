// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.jproxy;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *
 */
public class SimpleProxy implements ProxyServer {
    private ClientLogger logger = new ClientLogger(SimpleProxy.class);
    private final int port;
    private final String hostname;

    private final AtomicBoolean isRunning;

    private Consumer<Throwable> onErrorHandler;
    private volatile AsynchronousServerSocketChannel serverSocket;

    public SimpleProxy(final String hostname, final int port) {
        this.port = port;
        this.hostname = hostname;
        this.isRunning = new AtomicBoolean(false);
    }

    @Override
    public void start(final Consumer<Throwable> onError) throws IOException {
        if (isRunning.getAndSet(true)) {
            throw new IllegalStateException("ProxyServer is already running");
        }

        onErrorHandler = onError;
        serverSocket = AsynchronousServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        scheduleListener(serverSocket);
    }

    @Override
    public void stop() throws IOException {
        if (isRunning.getAndSet(false)) {
            serverSocket.close();
        }
    }

    private void scheduleListener(final AsynchronousServerSocketChannel serverSocket) {
        serverSocket.accept(serverSocket, new SocketListener(logger, onErrorHandler, isRunning));
    }

    private static class SocketListener implements
        CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

        private final ClientLogger logger;
        private final Consumer<Throwable> onErrorHandler;
        private final AtomicBoolean isRunning;
        private final AtomicReference<ProxyNegotiationHandler> negotiationHandler = new AtomicReference<>();

        private SocketListener(ClientLogger logger, Consumer<Throwable> onErrorHandler, AtomicBoolean isRunning) {
            this.logger = logger;
            this.onErrorHandler = onErrorHandler;
            this.isRunning = isRunning;
        }

        @Override
        public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel serverSocket) {
            try {
                logger.warning("Client connected from: {}", client.getRemoteAddress().toString());
            } catch (IOException e) {
                logger.error("Error occurred while getting remote address.", e);
            }

            if (negotiationHandler.get() != null) {
                logger.error("Negotiation handler has already been set. We are not setting another.");
                return;
            }

            logger.info("Setting negotiation handler.");
            serverSocket.accept(serverSocket, this);

            try {
                negotiationHandler.set(new ProxyNegotiationHandler(client));
            } catch (IOException e) {
                onErrorHandler.accept(e);
            }
        }

        @Override
        public void failed(Throwable exception, AsynchronousServerSocketChannel attachment) {
            isRunning.set(false);
            onErrorHandler.accept(exception);
        }
    }
}
