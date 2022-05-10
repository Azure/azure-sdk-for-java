// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.jproxy;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.jproxy.ReadWriteState.Target;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Objects;
import java.util.Scanner;

import static com.azure.messaging.servicebus.jproxy.SimpleProxy.PROXY_BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;

class ReadWriteHandler implements CompletionHandler<Integer, ReadWriteState> {
    private static final ClientLogger LOGGER = new ClientLogger(ReadWriteHandler.class);
    private final ConnectionProperties connection;

    ReadWriteHandler(ConnectionProperties connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public void completed(Integer result, ReadWriteState readWriteState) {
        if (result == -1) {
            LOGGER.info("There were no bytes read/written. State: {}\tConnection: {}.", readWriteState, connection);
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.warning("Unable to close connection.", e);
            }
            return;
        }

        final ProxyConnectionState state = connection.getProxyConnectionState();
        switch (state) {
            case PROXY_NOT_STARTED:
                if (readWriteState.isReading()) {
                    final ByteBuffer readBuffer = readWriteState.getBuffer();
                    readBuffer.flip();
                    final int bytesToRead = readBuffer.limit();
                    if (bytesToRead > 0) {
                        final byte[] connectRequest = new byte[bytesToRead];
                        readBuffer.get(connectRequest, 0, bytesToRead);
                        readBuffer.compact();

                        final InetSocketAddress clientAddress = getClientAddress(connectRequest);

                        LOGGER.info("Connecting to client: {}", clientAddress);

                        connection.setProxyConnectionState(ProxyConnectionState.PROXY_INITIATED);
                        connection.getOutgoingSocket().connect(clientAddress, readWriteState,
                            new ServiceConnectCompletionHandler(connection, this));
                    } else {
                        throw LOGGER.logExceptionAsError(new IllegalStateException(
                            "There should have been bytes read from the buffer when starting proxy."));
                    }
                } else {
                    throw LOGGER.logExceptionAsError(new IllegalStateException(
                        "ReadWriteState should be isReading because proxy connection has not been initiated yet."));
                }
                break;

            case PROXY_CONNECTED:
                copyBytesBetweenClientAndService(readWriteState);
                break;

            default:
                break;
        }
    }

    @Override
    public void failed(Throwable exc, ReadWriteState readWriteState) {
        if (exc instanceof AsynchronousCloseException) {
            LOGGER.info("Client socket closed.");
        } else {
            LOGGER.error("Operation failed connection={}, readWriteState={}", connection, readWriteState, exc);
        }
    }

    private void copyBytesBetweenClientAndService(ReadWriteState readWriteState) {
        LOGGER.info("Copying bytes. State: {}", readWriteState);

        final ByteBuffer buffer = readWriteState.getBuffer();
        final Target writeTarget = readWriteState.getWriteTarget();
        if (readWriteState.isReading()) {
            final AsynchronousSocketChannel writeChannel = writeTarget == Target.CLIENT
                ? connection.getClientSocket()
                : connection.getOutgoingSocket();

            readWriteState.setIsReading(false);
            buffer.flip();
            writeChannel.write(buffer, readWriteState, this);
        } else {
            // if this is write_success_callback - issue a read on the opposite channel
            final AsynchronousSocketChannel readChannel = writeTarget == Target.CLIENT
                ? connection.getOutgoingSocket()
                : connection.getClientSocket();

            readWriteState.setIsReading(true);
            buffer.flip();
            buffer.clear();
            readChannel.read(buffer, readWriteState, this);
        }
    }

    private InetSocketAddress getClientAddress(final byte[] connectRequest) {
        final String request = new String(connectRequest, UTF_8);
        final Scanner requestScanner = new Scanner(request);
        final String firstLine = requestScanner.nextLine();

        final String[] split = firstLine
            .replace("CONNECT ", "")
            .replace(" HTTP/1.1", "")
            .split(":");

        if (split.length != 2) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Unable to parse CONNECT request: " + request));
        }

        final int port;
        try {
            port = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Unable to parse port number from CONNECT request: " + request));
        }

        return new InetSocketAddress(split[0], port);
    }

    private static class ServiceConnectCompletionHandler implements CompletionHandler<Void, ReadWriteState> {
        private static final ClientLogger LOGGER = new ClientLogger(ServiceConnectCompletionHandler.class);
        private final ConnectionProperties connection;
        private final ReadWriteHandler handler;

        ServiceConnectCompletionHandler(ConnectionProperties connection, ReadWriteHandler handler) {
            this.connection = Objects.requireNonNull(connection);
            this.handler = Objects.requireNonNull(handler);
        }

        @Override
        public void completed(Void result, ReadWriteState proxyInitRead) {
            // when proxy connection negotiation is complete
            // initiate 2 async - pipelines
            // 1) read from client buffer and write to service buffer
            // 2) read from service buffer and write to client buffer
            connection.setProxyConnectionState(ProxyConnectionState.PROXY_CONNECTED);
            LOGGER.warning("Connecting to service successful, {}, {}", connection.getProxyConnectionState(),
                proxyInitRead);

            final ByteBuffer buffer = proxyInitRead.getBuffer();
            final ReadWriteState clientWriterState = new ReadWriteState(Target.CLIENT, buffer, false);
            buffer.clear();
            buffer.put("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            buffer.limit(buffer.position());
            buffer.flip();

            LOGGER.info("Writing connection established to client.");

            final AsynchronousSocketChannel client = connection.getClientSocket();
            client.write(buffer, clientWriterState, handler);

            final ReadWriteState serviceWriterState = new ReadWriteState(Target.SERVICE,
                ByteBuffer.allocate(PROXY_BUFFER_SIZE), true);

            LOGGER.info("Reading connect to client.");

            client.read(serviceWriterState.getBuffer(), serviceWriterState, handler);
            LOGGER.info("Finished reading connect to client.");
        }

        @Override
        public void failed(Throwable exc, ReadWriteState state) {
            LOGGER.error("serviceConnectFailed, Connection={}, State={}", connection, state, exc);

            connection.setProxyConnectionState(ProxyConnectionState.PROXY_FAILED);

            final ByteBuffer buffer = state.getBuffer();
            state.setIsReading(false);
            buffer.clear();
            buffer.put(String.format("HTTP/1.1 502 %s\r\n\r\n", exc.getMessage()).getBytes());
            buffer.flip();
            connection.getClientSocket().write(buffer, state, handler);
        }
    }
}
