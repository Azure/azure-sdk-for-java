// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the client honours {@code Expect: 100-continue} on the wire: the headers are sent, the body is withheld
 * until the service responds {@code 100 Continue}, and the body is delivered afterwards.
 */
public class VertxHttpClientExpectContinueTests {
    private static final byte[] BODY = new byte[1024];

    @ParameterizedTest
    @ValueSource(strings = { "100-continue", "100-Continue", " 100-continue ", "100-continue, foo" })
    public void withholdsBodyUntilContinue(String expectHeaderValue) throws Exception {
        try (ExpectContinueServer server = new ExpectContinueServer()) {
            server.start();

            HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
            request.getHeaders().set(HttpHeaderName.EXPECT, expectHeaderValue);

            HttpClient client = new VertxHttpClientProvider().createInstance();
            HttpResponse response = client.send(request).block();

            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
            assertTrue(server.awaitRequest(), "the request never reached the server");

            assertNotNull(server.expectHeader, "the Expect header did not reach the wire");
            assertEquals(0, server.bodyBytesBeforeContinue, "the body was sent before the service answered");
            assertEquals(BODY.length, server.bodyBytesAfterContinue, "the body was not delivered after 100 Continue");
        }
    }

    @Test
    public void sendsBodyWhenTheServiceIgnoresTheExpectation() throws Exception {
        // A service that never answers the expectation must not stall the request. The body is sent anyway once the
        // fallback elapses.
        try (ExpectContinueServer server = new ExpectContinueServer(false)) {
            server.start();

            HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
            request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");

            HttpClient client = new VertxHttpClientProvider().createInstance();
            HttpResponse response = client.send(request).block();

            assertNotNull(response, "the request did not complete");
            assertEquals(201, response.getStatusCode());
            assertTrue(server.awaitRequest(), "the request never reached the server");
            assertEquals(BODY.length, server.bodyBytesBeforeContinue + server.bodyBytesAfterContinue,
                "the body was never delivered");
        }
    }

    @Test
    public void doesNotSendBodyWhenTheServiceRejectsTheHeaders() throws Exception {
        // The point of the expectation: a rejected request must not upload its body. The fallback must not fire
        // afterwards and write onto a finished exchange.
        try (ExpectContinueServer server = new ExpectContinueServer(false, 401)) {
            server.start();

            HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
            request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");

            HttpClient client = new VertxHttpClientProvider().createInstance();
            HttpResponse response = client.send(request).block();

            assertNotNull(response, "the request did not complete");
            assertEquals(401, response.getStatusCode());
            assertTrue(server.awaitRequest(), "the request never reached the server");

            // Outlast the fallback, then confirm it did not wake up and send the body.
            Thread.sleep(1500);
            assertEquals(0, server.bodyBytesBeforeContinue + server.bodyBytesAfterContinue,
                "the body was sent even though the service rejected the request");
        }
    }

    @Test
    public void sendsBodyImmediatelyWithoutTheHeader() throws Exception {
        try (ExpectContinueServer server = new ExpectContinueServer()) {
            server.start();

            HttpClient client = new VertxHttpClientProvider().createInstance();
            HttpResponse response = client.send(new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY)).block();

            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
            assertTrue(server.awaitRequest(), "the request never reached the server");

            assertNull(server.expectHeader, "the Expect header should not have been set");
            assertEquals(BODY.length, server.bodyBytesBeforeContinue,
                "without the header the body should be sent immediately");
        }
    }

    /**
     * A minimal HTTP/1.1 server that answers the 100-continue handshake by hand, recording how many body bytes had
     * arrived before it responded.
     */
    private static final class ExpectContinueServer implements AutoCloseable {
        // How long the server keeps reading before deciding the client is not going to send the body unprompted.
        private static final long BODY_SETTLE_MILLIS = 500;

        // Short enough that the settle window is made up of many read attempts rather than one long block.
        private static final int POLL_TIMEOUT_MILLIS = 50;

        private final ServerSocket serverSocket;
        private final CountDownLatch requestHandled = new CountDownLatch(1);
        private volatile Thread thread;

        private volatile String expectHeader;
        private volatile int bodyBytesBeforeContinue = -1;
        private volatile int bodyBytesAfterContinue = -1;

        private final boolean sendContinue;
        private final int rejectWithStatus;

        ExpectContinueServer() throws IOException {
            this(true, 0);
        }

        ExpectContinueServer(boolean sendContinue) throws IOException {
            this(sendContinue, 0);
        }

        ExpectContinueServer(boolean sendContinue, int rejectWithStatus) throws IOException {
            this.serverSocket = new ServerSocket(0);
            this.sendContinue = sendContinue;
            this.rejectWithStatus = rejectWithStatus;
        }

        URL url() throws IOException {
            return URI.create("http://localhost:" + serverSocket.getLocalPort() + "/expect-continue").toURL();
        }

        void start() {
            thread = new Thread(this::serve, "expect-continue-server");
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

                int contentLength = readHeaders(in);

                if (rejectWithStatus != 0) {
                    // Answer the headers with a final status and never read the body.
                    bodyBytesBeforeContinue = 0;
                    bodyBytesAfterContinue = 0;
                    out.write(("HTTP/1.1 " + rejectWithStatus + " Unauthorized\r\nContent-Length: 0\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    return;
                }

                // Actively read for the whole settle window rather than sampling available(), so a body that is on
                // its way is counted rather than missed.
                socket.setSoTimeout(POLL_TIMEOUT_MILLIS);
                bodyBytesBeforeContinue = readFor(in, contentLength, BODY_SETTLE_MILLIS);

                if (expectHeader != null && sendContinue) {
                    out.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
                bodyBytesAfterContinue = readBody(in, contentLength - bodyBytesBeforeContinue);

                out.write("HTTP/1.1 201 Created\r\nContent-Length: 0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException ex) {
                // Leave the recorded values as they are; the test assertions report the failure.
            } finally {
                requestHandled.countDown();
            }
        }

        // Reads one byte at a time up to the end of the headers, so that no part of the body is consumed.
        private int readHeaders(InputStream in) throws IOException {
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

            int contentLength = 0;
            for (String line : new String(headerBytes.toByteArray(), StandardCharsets.UTF_8).split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String name = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();
                if ("expect".equals(name)) {
                    expectHeader = value.toLowerCase(Locale.ROOT);
                } else if ("content-length".equals(name)) {
                    contentLength = Integer.parseInt(value);
                }
            }

            return contentLength;
        }

        /*
         * Keeps attempting reads until the window elapses, so the count reflects what actually arrived rather than
         * what happened to be buffered at one instant.
         */
        private static int readFor(InputStream in, int max, long windowMillis) throws IOException {
            byte[] buffer = new byte[8192];
            int read = 0;
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(windowMillis);
            while (System.nanoTime() < deadline && read < max) {
                try {
                    int count = in.read(buffer, 0, Math.min(buffer.length, max - read));
                    if (count < 0) {
                        break;
                    }
                    read += count;
                } catch (SocketTimeoutException ex) {
                    // Nothing arrived in this slice; keep waiting until the window closes.
                }
            }
            return read;
        }

        private static int readBody(InputStream in, int remaining) throws IOException {
            if (remaining <= 0) {
                return 0;
            }

            int read = 0;
            byte[] buffer = new byte[remaining];
            while (read < remaining) {
                int count = in.read(buffer, read, remaining - read);
                if (count < 0) {
                    break;
                }
                read += count;
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
