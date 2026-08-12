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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Wire level tests recording what each HTTP client implementation actually does with {@code Expect: 100-continue}.
 * <p>
 * The policy tests elsewhere in this package use a stubbed {@code HttpClient}, so they can only show that the header is
 * set under the right conditions. These tests run against a real socket and assert on what the transport put on the
 * wire and when, which is the only way to tell whether the handshake actually saves an upload.
 * <p>
 * Setting the header does not by itself make a client wait. Of the four transports, only OkHttp performs the handshake;
 * the others either send the body immediately anyway or drop the header. That is recorded in {@link ContinueSupport}
 * rather than hidden, so this test documents the real behavior and fails if any transport changes, in either direction.
 */
public class ExpectContinueTransportTests {
    private static final byte[] BODY = "the request body that must not be sent early".getBytes(StandardCharsets.UTF_8);

    /*
     * How long the server waits, after the headers arrive, before deciding that the client has not sent the body. Long
     * enough that a client which streams the body immediately will have lost the race.
     */
    private static final long BODY_SETTLE_MILLIS = 500;

    private ExpectContinueServer server;

    /**
     * What a transport is observed to do when the pipeline sets {@code Expect: 100-continue}.
     */
    private enum ContinueSupport {
        /**
         * Sends the headers, waits for {@code 100 Continue}, and only then sends the body. This is the behavior the
         * feature depends on, and the only case where a rejected upload costs nothing but headers.
         */
        DEFERS_BODY,

        /**
         * Puts the header on the wire but streams the body immediately without waiting, so the service cannot reject
         * the request before receiving it and no bandwidth is saved.
         */
        SENDS_HEADER_ONLY,

        /**
         * Drops the header entirely, so the handshake never happens. The JDK client treats {@code Expect} as a
         * restricted header and additionally reports {@code expectContinue() == false} to the underlying client.
         */
        DROPS_HEADER
    }

    static Stream<Arguments> transports() {
        return Stream.of(
            Arguments.of("netty", "com.azure.core.http.netty.NettyAsyncHttpClientProvider",
                ContinueSupport.SENDS_HEADER_ONLY),
            Arguments.of("okhttp", "com.azure.core.http.okhttp.OkHttpAsyncClientProvider", ContinueSupport.DEFERS_BODY),
            Arguments.of("jdk", "com.azure.core.http.jdk.httpclient.JdkHttpClientProvider",
                ContinueSupport.DROPS_HEADER),
            Arguments.of("vertx", "com.azure.core.http.vertx.VertxHttpClientProvider",
                ContinueSupport.SENDS_HEADER_ONLY));
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
    public void transportBehaviorOnTheWire(String name, String providerClassName, ContinueSupport expected)
        throws Exception {
        HttpClient httpClient = createHttpClient(providerClassName);
        assumeTrue(httpClient != null, name + " is not on the test classpath");

        HttpPipeline pipeline = pipeline(httpClient);
        HttpResponse response
            = pipeline.sendSync(new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY), Context.NONE);

        assertEquals(201, response.getStatusCode(), name + " did not complete the request");
        assertTrue(server.awaitRequest(), name + " never reached the server");

        // Whatever the transport does with the handshake, the service must end up with the whole body.
        assertEquals(BODY.length, server.totalBodyBytes(), name + " did not deliver the full body");

        switch (expected) {
            case DEFERS_BODY:
                assertEquals("100-continue", server.expectHeader, name + " did not put the header on the wire");
                assertEquals(0, server.bodyBytesBeforeContinue,
                    name + " streamed " + server.bodyBytesBeforeContinue + " body bytes before the server sent 100"
                        + " Continue. It is recorded as deferring the body, so this is a regression that makes the"
                        + " feature ineffective on this transport.");
                assertEquals(BODY.length, server.bodyBytesAfterContinue,
                    name + " did not deliver the body after 100 Continue");
                break;

            case SENDS_HEADER_ONLY:
                assertEquals("100-continue", server.expectHeader, name + " did not put the header on the wire");
                assertEquals(BODY.length, server.bodyBytesBeforeContinue,
                    name + " is recorded as sending the body without waiting. If it now waits for 100 Continue, the"
                        + " transport has gained real support and this table, plus the feature's documented"
                        + " limitations, should be updated.");
                break;

            case DROPS_HEADER:
                assertNull(server.expectHeader,
                    name + " is recorded as dropping the header. If it now reaches the wire, this table and the"
                        + " feature's documented limitations should be updated.");
                assertEquals(BODY.length, server.bodyBytesBeforeContinue,
                    name + " dropped the header but still withheld the body, which is not expected");
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

    /*
     * Resolved by name so that this test compiles without a direct dependency on every transport, and skips rather
     * than fails when one is absent from the classpath.
     */
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
     * A minimal HTTP/1.1 server that speaks the 100-continue handshake by hand, so that the timing of the body is
     * observable. It deliberately waits before answering, and records how many body bytes had arrived by then.
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

                // Give a client that streams the body immediately every chance to do so, then look before answering.
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

        /*
         * Reads exactly up to the end of the headers, one byte at a time, so that no part of the body is consumed.
         */
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
