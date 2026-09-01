// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the client honours {@code Expect: 100-continue} on the wire: the headers are sent, the body is withheld
 * until the service responds {@code 100 Continue}, and the body is delivered afterwards.
 */
public class JdkHttpClientExpectContinueTests {
    private static final byte[] BODY = new byte[1024];

    // How long the server waits after the headers arrive before answering. A client that does not defer the body will
    // have sent it well within this window.
    private static final long BODY_SETTLE_MILLIS = 500;

    @Test
    public void withholdsBodyUntilContinue() throws Exception {
        try (ExpectContinueServer server = new ExpectContinueServer()) {
            server.start();

            HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
            request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");

            HttpClient client = new JdkHttpClientProvider().createInstance();
            HttpResponse response = client.send(request).block();

            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
            assertTrue(server.awaitRequest(), "the request never reached the server");

            assertEquals("100-continue", server.expectHeader, "the Expect header did not reach the wire");
            assertEquals(0, server.bodyBytesBeforeContinue, "the body was sent before the service answered");
            assertEquals(BODY.length, server.bodyBytesAfterContinue, "the body was not delivered after 100 Continue");
        }
    }

    @Test
    public void sendsBodyImmediatelyWithoutTheHeader() throws Exception {
        try (ExpectContinueServer server = new ExpectContinueServer()) {
            server.start();

            HttpClient client = new JdkHttpClientProvider().createInstance();
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
        private final ServerSocket serverSocket;
        private final CountDownLatch requestHandled = new CountDownLatch(1);
        private volatile Thread thread;

        private volatile String expectHeader;
        private volatile int bodyBytesBeforeContinue = -1;
        private volatile int bodyBytesAfterContinue = -1;

        ExpectContinueServer() throws IOException {
            this.serverSocket = new ServerSocket(0);
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

                Thread.sleep(BODY_SETTLE_MILLIS);
                bodyBytesBeforeContinue = in.available();

                if (expectHeader != null) {
                    out.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                bodyBytesAfterContinue = readBody(in, contentLength - bodyBytesBeforeContinue);

                out.write("HTTP/1.1 201 Created\r\nContent-Length: 0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException | InterruptedException ex) {
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
