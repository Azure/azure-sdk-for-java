// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the Netty client surfaces the observation of an interim {@code 100 Continue} response through an
 * {@link AtomicBoolean} holder the caller places in the request {@link Context}. The holder is keyed by an opt-in
 * string contract any caller may use; the Netty client sets it to {@code true} when the service sends the interim
 * response.
 */
public class NettyExpectContinueObservationTests {
    private static final byte[] BODY = new byte[1024];

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void interimContinueIsSurfacedThroughContextHolder(boolean sync) throws Exception {
        try (ContinueTestServer server = new ContinueTestServer(true)) {
            server.start();
            AtomicBoolean holder = sendWithHolder(server, sync);
            assertTrue(server.awaitRequest(), "request never reached the server");
            assertTrue(holder.get(), "100 Continue was not surfaced through the context holder");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void holderStaysFalseWhenServerSendsNoContinue(boolean sync) throws Exception {
        try (ContinueTestServer server = new ContinueTestServer(false)) {
            server.start();
            AtomicBoolean holder = sendWithHolder(server, sync);
            assertTrue(server.awaitRequest(), "request never reached the server");
            assertFalse(holder.get(), "holder should stay false when no 100 Continue is sent");
        }
    }

    // Must match the private key NettyAsyncHttpClient reads for the opt-in 100-continue observation contract.
    private static final String EXPECT_CONTINUE_RECEIVED_KEY = "azure-http-client-expect-continue-received";

    private static AtomicBoolean sendWithHolder(ContinueTestServer server, boolean sync) throws IOException {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        AtomicBoolean holder = new AtomicBoolean(false);
        Context context = new Context(EXPECT_CONTINUE_RECEIVED_KEY, holder);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
        request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");

        HttpResponse response = sync ? client.sendSync(request, context) : client.send(request, context).block();
        assertEquals(201, response.getStatusCode());
        response.close();
        return holder;
    }

    /**
     * Minimal HTTP/1.1 server that optionally answers the 100-continue handshake, then reads the body and returns 201.
     */
    private static final class ContinueTestServer implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final boolean sendContinue;
        private final CountDownLatch requestHandled = new CountDownLatch(1);
        private volatile Thread thread;

        ContinueTestServer(boolean sendContinue) throws IOException {
            this.serverSocket = new ServerSocket(0);
            this.sendContinue = sendContinue;
        }

        URL url() throws IOException {
            return URI.create("http://localhost:" + serverSocket.getLocalPort() + "/resource").toURL();
        }

        void start() {
            thread = new Thread(this::serve, "continue-test-server");
            thread.setDaemon(true);
            thread.start();
        }

        boolean awaitRequest() throws InterruptedException {
            return requestHandled.await(30, TimeUnit.SECONDS);
        }

        private void serve() {
            try (Socket socket = serverSocket.accept()) {
                socket.setTcpNoDelay(true);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                Parsed parsed = readHeaders(in);

                if (sendContinue && parsed.expectHeader != null) {
                    out.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                readBody(in, parsed.contentLength);

                out.write("HTTP/1.1 201 Created\r\nContent-Length: 0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException ex) {
                // The test assertions report the failure.
            } finally {
                requestHandled.countDown();
            }
        }

        private static Parsed readHeaders(InputStream in) throws IOException {
            ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
            int consecutiveNewlines = 0;
            int b;
            while (consecutiveNewlines < 2 && (b = in.read()) != -1) {
                headerBytes.write(b);
                if (b == '\n') {
                    consecutiveNewlines++;
                } else if (b != '\r') {
                    consecutiveNewlines = 0;
                }
            }

            Parsed parsed = new Parsed();
            for (String line : new String(headerBytes.toByteArray(), StandardCharsets.UTF_8).split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();
                if ("expect".equals(name)) {
                    parsed.expectHeader = value.toLowerCase(Locale.ROOT);
                } else if ("content-length".equals(name)) {
                    parsed.contentLength = Integer.parseInt(value);
                }
            }
            return parsed;
        }

        private static void readBody(InputStream in, int remaining) throws IOException {
            byte[] buffer = new byte[Math.max(remaining, 1)];
            int read = 0;
            while (read < remaining) {
                int count = in.read(buffer, 0, Math.min(buffer.length, remaining - read));
                if (count < 0) {
                    break;
                }
                read += count;
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            if (thread != null) {
                thread.interrupt();
            }
        }

        private static final class Parsed {
            private String expectHeader;
            private int contentLength;
        }
    }
}
