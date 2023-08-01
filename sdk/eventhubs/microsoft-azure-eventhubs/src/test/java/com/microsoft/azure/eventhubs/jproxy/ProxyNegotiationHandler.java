// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.jproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProxyNegotiationHandler {

    public enum ProxyConnectionState {
        PROXY_NOT_STARTED,
        PROXY_INITIATED,
        PROXY_CONNECTED,
        PROXY_FAILED,
        PROXY_CLOSED
    }

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ProxyNegotiationHandler.class);
    private static final int PROXY_BUFFER_SIZE = 1024;

    private final AsynchronousSocketChannel clientSocket;
    private final AsynchronousSocketChannel serviceSocket;

    private volatile ProxyConnectionState proxyConnectionState;

    public ProxyNegotiationHandler(final AsynchronousSocketChannel clientSocket) throws IOException {

        this.clientSocket = clientSocket;
        this.proxyConnectionState = ProxyConnectionState.PROXY_NOT_STARTED;
        this.serviceSocket = AsynchronousSocketChannel.open();

        final ReadWriteState proxyInitRead = new ReadWriteState(false);
        proxyInitRead.buffer = ByteBuffer.allocate(PROXY_BUFFER_SIZE);
        proxyInitRead.isRead = true;
        proxyInitRead.clientAddress = this.clientSocket.getRemoteAddress().toString();

        this.clientSocket.read(proxyInitRead.buffer, proxyInitRead, new ReadWriteHandler());
    }

    static class ReadWriteState {
        // flag used by PROXY_CONNECTED state to decide which socketchannel to write bytes to
        final Boolean isClientWriter;

        ReadWriteState(final Boolean isClientWriter) {
            this.isClientWriter = isClientWriter;
        }

        ByteBuffer buffer;
        Boolean isRead;

        String clientAddress;
        String serviceAddress;

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder()
                .append("ReadWriteState[")
                .append(isClientWriter ? "client" : "service")
                .append("Writer, ")
                .append("client=")
                .append(clientAddress)
                .append(", ")
                .append("svc=")
                .append(serviceAddress)
                .append(", ")
                .append(isRead ? "read" : "write")
                .append("]");

            return builder.toString();
        }
    }

    class ReadWriteHandler implements CompletionHandler<Integer, ReadWriteState> {

        @Override
        public void completed(Integer result, ReadWriteState readWriteState) {
            if (result == -1) {
                proxyConnectionState = ProxyConnectionState.PROXY_CLOSED;

                try {
                    clientSocket.close();
                } catch (IOException ignore) {
                }

                try {
                    serviceSocket.close();
                } catch (IOException ignore) {
                }

                return;
            }

            switch (proxyConnectionState) {
                case PROXY_NOT_STARTED:
                    if (readWriteState.isRead) {
                        ByteBuffer readBuffer = readWriteState.buffer;
                        readBuffer.flip();
                        int bytesToRead = readBuffer.limit();
                        if (bytesToRead > 0) {
                            final byte[] connectRequest = new byte[bytesToRead];
                            readBuffer.get(connectRequest, 0, bytesToRead);
                            readBuffer.compact();

                            final String[] hostNamePortParts = extractHostNamePort(connectRequest);
                            proxyConnectionState = ProxyConnectionState.PROXY_INITIATED;
                            final InetSocketAddress serviceAddress = new InetSocketAddress(
                                hostNamePortParts[0], Integer.parseInt(hostNamePortParts[1]));
                            readWriteState.serviceAddress = serviceAddress.toString();
                            serviceSocket.connect(
                                serviceAddress,
                                readWriteState,
                                new ServiceConnectCompletionHandler());
                        } else {
                            // unreachable code
                            throw new IllegalStateException();
                        }
                    } else {
                        // unreachable code
                        // we don't issue a write on any socket
                        // - without connecting to service
                        throw new IllegalStateException();
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
        public void failed(Throwable exc, ReadWriteState attachment) {
            if (TRACE_LOGGER.isWarnEnabled()) {
                TRACE_LOGGER.warn("readWriteHandlerFailed error="
                    + exc.getClass()
                    + ", " + proxyConnectionState
                    + ", " + attachment.toString());
            }
        }

        private void copyBytesBetweenClientAndService(final ReadWriteState readWriteState) {
            if (!readWriteState.isRead) {
                // if this is write_success_callback - issue a read on the opposite channel
                final AsynchronousSocketChannel readChannel = readWriteState.isClientWriter
                    ? serviceSocket
                    : clientSocket;

                readWriteState.isRead = true;
                readWriteState.buffer.flip();
                readWriteState.buffer.clear();
                readChannel.read(readWriteState.buffer, readWriteState, this);
            } else {
                final AsynchronousSocketChannel writeChannel = readWriteState.isClientWriter
                    ? clientSocket
                    : serviceSocket;
                readWriteState.isRead = false;
                readWriteState.buffer.flip();
                writeChannel.write(readWriteState.buffer, readWriteState, this);
            }
        }

        private String[] extractHostNamePort(final byte[] connectRequest) {
            final String request = new String(connectRequest, UTF_8);
            final Scanner requestScanner = new Scanner(request);
            final String firstLine = requestScanner.nextLine();

            return firstLine
                .replace("CONNECT ", "")
                .replace(" HTTP/1.1", "")
                .split(":");
        }

        private class ServiceConnectCompletionHandler implements CompletionHandler<Void, ReadWriteState> {
            @Override
            public void completed(Void result, ReadWriteState proxyInitRead) {

                // when proxy connection negotiation is complete
                // initiate 2 async - pipelines
                // 1) read from client buffer and write to service buffer
                // 2) read from service buffer and write to client buffer

                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn("serviceConnectSuccess, "
                        + proxyConnectionState + ", "
                        + proxyInitRead.toString());
                }

                proxyConnectionState = ProxyConnectionState.PROXY_CONNECTED;

                final ReadWriteState clientWriterState = new ReadWriteState(true);
                clientWriterState.isRead = false;
                clientWriterState.buffer = proxyInitRead.buffer;
                clientWriterState.buffer.clear();
                clientWriterState.buffer.put("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                clientWriterState.buffer.limit(proxyInitRead.buffer.position());
                clientWriterState.buffer.flip();

                clientSocket.write(
                    clientWriterState.buffer,
                    clientWriterState,
                    ReadWriteHandler.this);

                final ReadWriteState serviceWriterState = new ReadWriteState(false);
                serviceWriterState.isRead = true;
                serviceWriterState.buffer = ByteBuffer.allocate(PROXY_BUFFER_SIZE);

                clientSocket.read(
                    serviceWriterState.buffer,
                    serviceWriterState,
                    ReadWriteHandler.this);
            }

            @Override
            public void failed(Throwable exc, ReadWriteState attachment) {
                if (TRACE_LOGGER.isWarnEnabled()) {
                    TRACE_LOGGER.warn("serviceConnectFailed, error="
                        + exc.getClass() + ", "
                        + proxyConnectionState + ", "
                        + attachment.toString());
                }

                proxyConnectionState = ProxyConnectionState.PROXY_FAILED;

                attachment.isRead = false;
                attachment.buffer.clear();
                attachment.buffer.put(String.format("HTTP/1.1 502 %s\r\n\r\n", exc.getMessage()).getBytes());
                attachment.buffer.flip();
                clientSocket.write(attachment.buffer, attachment, ReadWriteHandler.this);
            }
        }
    }
}
