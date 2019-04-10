// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.jproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SimpleProxy implements ProxyServer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SimpleProxy.class);

    private final int port;
    private final String hostName;

    private final AtomicBoolean isRunning;

    private volatile Consumer<Throwable> onErrorHandler;
    private volatile AsynchronousServerSocketChannel serverSocket;

    public SimpleProxy(final String hostName, final int port) {
        this.port = port;
        this.hostName = hostName;

        this.isRunning = new AtomicBoolean(false);
    }

    @Override
    public void start(final Consumer<Throwable> onError) throws IOException {
        if (this.isRunning.get()) {
            throw new IllegalStateException("ProxyServer is already running");
        }

        this.isRunning.set(true);
        this.onErrorHandler = onError;

        serverSocket = AsynchronousServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        scheduleListener(serverSocket);
    }

    @Override
    public void stop() throws IOException {
        if (this.isRunning.getAndSet(false)) {
            this.serverSocket.close();
        }
    }

    private void scheduleListener(final AsynchronousServerSocketChannel serverSocket) {
        serverSocket.accept(
            serverSocket,
            new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                @Override
                public void completed(
                    AsynchronousSocketChannel client,
                    AsynchronousServerSocketChannel serverSocket) {

                    if (TRACE_LOGGER.isWarnEnabled()) {
                        try {
                            TRACE_LOGGER.warn("client connected from: " + client.getRemoteAddress().toString());
                        } catch (IOException ignore) {
                        }
                    }

                    serverSocket.accept(serverSocket, this);

                    try {
                        new ProxyNegotiationHandler(client);
                    } catch (IOException e) {
                        onErrorHandler.accept(e);
                    }
                }

                @Override
                public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                    isRunning.set(false);
                    onErrorHandler.accept(exc);
                }
            });
    }
}
