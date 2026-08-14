// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.policy.ExpectContinueMode;
import com.azure.storage.common.policy.ExpectContinueOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies what each HTTP client puts on the wire for {@code Expect: 100-continue}, and whether it withholds the
 * request body until the service responds. Only OkHttp performs the handshake; the behavior of each client is recorded
 * in {@link ContinueSupport} so that these tests fail if any of them changes.
 */
public class ExpectContinueTransportTests {
    private static final byte[] BODY = "the request body that must not be sent early".getBytes(StandardCharsets.UTF_8);

    // How long the server waits after the headers arrive before deciding the client has not sent the body.
    private static final long BODY_SETTLE_MILLIS = 500;

    private ExpectContinueServer server;

    private enum ContinueSupport {
        /** Waits for {@code 100 Continue} before sending the body. */
        DEFERS_BODY,

        /** Sends the header but streams the body without waiting. */
        SENDS_HEADER_ONLY,

        /** Drops the header, as {@code Expect} is restricted by {@code java.net.http.HttpClient}. */
        DROPS_HEADER
    }

    static Stream<Arguments> transports() {
        Object[][] clients = {
            { "netty", "com.azure.core.http.netty.NettyAsyncHttpClientProvider", ContinueSupport.SENDS_HEADER_ONLY },
            { "okhttp", "com.azure.core.http.okhttp.OkHttpAsyncClientProvider", ContinueSupport.DEFERS_BODY },
            { "jdk", "com.azure.core.http.jdk.httpclient.JdkHttpClientProvider", ContinueSupport.DROPS_HEADER },
            { "vertx", "com.azure.core.http.vertx.VertxHttpClientProvider", ContinueSupport.SENDS_HEADER_ONLY } };

        return Stream.of(clients)
            .flatMap(client -> Stream.of(Arguments.of(client[0] + " sync", client[1], client[2], true),
                Arguments.of(client[0] + " async", client[1], client[2], false)));
    }

    @BeforeEach
    public void setup() throws IOException {
        server = new ExpectContinueServer();
        server.start();
    }

    @AfterEach
    public void teardown() throws IOException {
        if (server != null) {
            server.close();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    public void transportBehaviorOnTheWire(String name, String providerClassName, ContinueSupport expected,
        boolean sync) throws Exception {
        HttpClient httpClient = createHttpClient(providerClassName);
        assumeTrue(httpClient != null, name + " is not on the test classpath");

        HttpPipeline pipeline = pipeline(httpClient);
        HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
        HttpResponse response = sync ? pipeline.sendSync(request, Context.NONE) : pipeline.send(request).block();

        assertNotNull(response, name + " returned no response");
        assertEquals(201, response.getStatusCode(), name + " did not complete the request");
        assertTrue(server.awaitRequest(), name + " never reached the server");
        assertEquals(BODY.length, server.totalBodyBytes(), name + " did not deliver the full body");

        switch (expected) {
            case DEFERS_BODY:
                assertEquals("100-continue", server.expectHeader, name + " did not put the header on the wire");
                assertEquals(0, server.bodyBytesBeforeContinue, name + " sent the body before 100 Continue");
                assertEquals(BODY.length, server.bodyBytesAfterContinue,
                    name + " did not deliver the body after 100 Continue");
                break;

            case SENDS_HEADER_ONLY:
                assertEquals("100-continue", server.expectHeader, name + " did not put the header on the wire");
                assertEquals(BODY.length, server.bodyBytesBeforeContinue,
                    name + " is recorded as not deferring the body; update this table if that has changed");
                break;

            case DROPS_HEADER:
                assertNull(server.expectHeader,
                    name + " is recorded as dropping the header; update this table if that has changed");
                assertEquals(BODY.length, server.bodyBytesBeforeContinue, name + " unexpectedly withheld the body");
                break;

            default:
                throw new IllegalStateException("Unhandled expectation: " + expected);
        }
    }

    private static HttpPipeline pipeline(HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, new ExpectContinueOptions().setMode(ExpectContinueMode.On));
        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    // Resolved by name so this compiles without a direct dependency on every transport, and skips when one is absent.
    private static HttpClient createHttpClient(String providerClassName) {
        try {
            Class<?> providerClass = Class.forName(providerClassName);
            HttpClientProvider provider = (HttpClientProvider) providerClass.getDeclaredConstructor().newInstance();
            return provider.createInstance();
        } catch (ReflectiveOperationException | LinkageError ex) {
            return null;
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
            return new URL("http://localhost:" + serverSocket.getLocalPort() + "/container/blob");
        }

        void start() {
            thread = new Thread(this::serve, "expect-continue-server");
            thread.setDaemon(true);
            thread.start();
        }

        boolean awaitRequest() throws InterruptedException {
            return requestHandled.await(30, TimeUnit.SECONDS);
        }

        int totalBodyBytes() {
            return Math.max(bodyBytesBeforeContinue, 0) + Math.max(bodyBytesAfterContinue, 0);
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
