// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link ExpectContinuePolicy} and {@link ExpectContinueOnThrottlePolicy}.
 */
public class ExpectContinueTests {
    private static final String CONTINUE = "100-continue";
    private static final String ENDPOINT = "https://account.blob.core.windows.net/container/blob";
    private static final byte[] BODY = new byte[1024];

    // How long the wire level server waits after the headers arrive before deciding the body was not sent.
    private static final long BODY_SETTLE_MILLIS = 500;

    // Negligible backoff so the retry tests stay fast.
    private static final RequestRetryOptions FAST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 4, (Integer) null, 1L, 5L, null);

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void policyAddsHeaderOnContentBody(boolean hasBody) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ON), client);

        HttpRequest request = hasBody
            ? new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT)).setBody("foo")
            : new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT));
        pipeline.sendSync(request, Context.NONE);

        assertEquals(hasBody ? CONTINUE : null, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @CsvSource({ "1024, 2048, true", "2048, 1024, false", "1024, 1024, true" })
    public void policyRespectsThreshold(int threshold, int bodyLength, boolean expectHeader)
        throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.ON).setContentLengthThreshold((long) threshold), client);

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);

        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @ValueSource(ints = { 429, 500, 503 })
    public void throttlePolicyAddsHeaderOnlyAfterError(int errorStatusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(202, errorStatusCode, 202);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(1));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertEquals(CONTINUE, client.expectHeaders.get(2));
    }

    @ParameterizedTest
    @CsvSource({ "1024, 2048, true", "2048, 1024, false", "1024, 1024, true" })
    public void throttlePolicyRespectsThreshold(int threshold, int bodyLength, boolean expectHeader)
        throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(429, 202);
        HttpPipeline pipeline = pipeline(
            options(ExpectContinueMode.APPLY_ON_THROTTLE).setContentLengthThreshold((long) threshold), client);

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);
        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(1));
    }

    @Test
    public void throttlePolicyRevertsAfterBackoff() throws Exception {
        Duration backoff = Duration.ofMillis(50);
        RecordingHttpClient client = new RecordingHttpClient(429, 202, 202);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE).setThrottleInterval(backoff), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertEquals(CONTINUE, client.expectHeaders.get(1));

        Thread.sleep(500);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(2));
    }

    @SyncAsyncTest
    public void appliesHeaderOnRetryAfterThrottling() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(2, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @SyncAsyncTest
    public void keepsApplyingHeaderAcrossMultipleRetries() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 503, 429, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(4, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
        assertEquals(CONTINUE, client.expectHeaders.get(2));
        assertEquals(CONTINUE, client.expectHeaders.get(3));
    }

    @SyncAsyncTest
    public void onModeAppliesHeaderOnEveryAttempt() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.ON), client);

        send(pipeline, requestWithBody());

        assertEquals(2, client.expectHeaders.size());
        assertEquals(CONTINUE, client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @SyncAsyncTest
    public void offModeDoesNotApplyHeader() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.OFF), client);

        send(pipeline, requestWithBody());

        assertNull(client.expectHeaders.get(0));
    }

    @Test
    public void offModeAddsNoPolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options(ExpectContinueMode.OFF));

        assertTrue(policies.isEmpty());
    }

    @Test
    public void defaultOptionsApplyOnThrottle() {
        ExpectContinueOptions options = new ExpectContinueOptions();

        assertEquals(ExpectContinueMode.APPLY_ON_THROTTLE, options.getMode());
        assertEquals(Duration.ofMinutes(1), options.getThrottleInterval());
        assertNull(options.getContentLengthThreshold());
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, -1024, Long.MIN_VALUE })
    public void negativeContentLengthThresholdIsRejected(long threshold) {
        ExpectContinueOptions options = new ExpectContinueOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setContentLengthThreshold(threshold));
    }

    @Test
    public void zeroAndNullContentLengthThresholdAreAccepted() {
        assertEquals(0L, new ExpectContinueOptions().setContentLengthThreshold(0L).getContentLengthThreshold());
        assertNull(new ExpectContinueOptions().setContentLengthThreshold(null).getContentLengthThreshold());
    }

    @Test
    public void negativeThrottleIntervalIsRejected() {
        ExpectContinueOptions options = new ExpectContinueOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setThrottleInterval(Duration.ofSeconds(-1)));
    }

    @Test
    public void zeroThrottleIntervalIsAccepted() {
        assertEquals(Duration.ZERO,
            new ExpectContinueOptions().setThrottleInterval(Duration.ZERO).getThrottleInterval());
    }

    @Test
    public void nullOptionsAddNoPolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, null);

        assertTrue(policies.isEmpty());
    }

    @Test
    public void explicitApplyOnThrottleOptionsAddThePolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options(ExpectContinueMode.APPLY_ON_THROTTLE));

        assertEquals(1, policies.size());
        assertTrue(policies.get(0) instanceof ExpectContinueOnThrottlePolicy);
    }

    // The observation holder key must match the one the Netty client reads
    // (AzureNettyHttpClientContext.EXPECT_CONTINUE_RECEIVED_KEY). This guards against the two drifting apart.
    @Test
    public void observationKeyMatchesTransportContract() {
        assertEquals("azure-http-client-expect-continue-received",
            ExpectContinuePolicyHelper.EXPECT_CONTINUE_RECEIVED_KEY);
    }

    // When the header is applied, the policy installs an observation holder that a supporting client sets, and the
    // result is readable through the helper. This is the storage half of the bridge; the Netty half is covered by
    // NettyExpectContinueObservationTests.
    @SyncAsyncTest
    public void observationHolderIsSetWhenServiceSendsContinue() throws MalformedURLException {
        ObservingHttpClient client = new ObservingHttpClient(true, 200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ON), client);

        send(pipeline, requestWithBody());

        assertTrue(client.holderInstalled, "policy did not install the observation holder");
        assertEquals(Boolean.TRUE, client.observedValue, "a received 100 Continue was not surfaced to the holder");
    }

    @SyncAsyncTest
    public void observationHolderStaysFalseWithoutContinue() throws MalformedURLException {
        ObservingHttpClient client = new ObservingHttpClient(false, 200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ON), client);

        send(pipeline, requestWithBody());

        assertTrue(client.holderInstalled, "policy did not install the observation holder");
        assertEquals(Boolean.FALSE, client.observedValue, "holder should be false when no 100 Continue is observed");
    }

    @Test
    public void noObservationHolderWhenHeaderNotApplied() throws MalformedURLException {
        // In APPLY_ON_THROTTLE mode with no prior throttling the header is not applied, so no holder is installed.
        ObservingHttpClient client = new ObservingHttpClient(false, 200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertFalse(client.holderInstalled, "no observation holder should be installed when the header is not applied");
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 304, 404, 412, 501 })
    public void applyOnThrottleIgnoresNonThrottlingResponses(int statusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(statusCode, 200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.APPLY_ON_THROTTLE), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @Test
    public void unknownContentLengthIsAlwaysEligible() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.ON).setContentLengthThreshold(Long.MAX_VALUE), client);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT));
        request.setBody(BinaryData.fromStream(new ByteArrayInputStream(BODY)));
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        pipeline.sendSync(request, Context.NONE);

        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "TRUE" })
    public void configurationOptOutSuppressesHeader(String value) throws MalformedURLException {
        Configuration configuration = environmentConfiguration(
            new TestConfigurationSource().put(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, value));
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = buildPipeline(Arrays.asList(new ExpectContinuePolicy(null, configuration),
            new ExpectContinueOnThrottlePolicy(Duration.ofMinutes(1), null, configuration)), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @Test
    public void configurationOptOutDefaultsToEnabled() throws MalformedURLException {
        Configuration configuration = environmentConfiguration(new TestConfigurationSource());
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = buildPipeline(Arrays.asList(new ExpectContinuePolicy(null, configuration)), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    // The opt out is read with Configuration.get(String, T), which resolves against the environment configuration.
    private static Configuration environmentConfiguration(TestConfigurationSource environment) {
        return new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), environment)
            .build();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    public void transportBehaviorOnTheWire(String name, String providerClassName, ContinueSupport expected,
        boolean sync) throws Exception {
        HttpClient httpClient = createHttpClient(providerClassName);
        assumeTrue(httpClient != null, name + " is not on the test classpath");

        try (ExpectContinueServer server = new ExpectContinueServer()) {
            server.start();

            HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ON), httpClient);
            HttpRequest request = new HttpRequest(HttpMethod.PUT, server.url()).setBody(BODY);
            HttpResponse response = sync ? pipeline.sendSync(request, Context.NONE) : pipeline.send(request).block();

            assertNotNull(response, name + " returned no response");
            assertEquals(201, response.getStatusCode(), name + " did not complete the request");
            assertTrue(server.awaitRequest(), name + " never reached the server");
            assertEquals(BODY.length, server.totalBodyBytes(), name + " did not deliver the full body");

            switch (expected) {
                case DEFERS_BODY:
                    assertEquals(CONTINUE, server.expectHeader, name + " did not put the header on the wire");
                    assertEquals(0, server.bodyBytesBeforeContinue, name + " sent the body before 100 Continue");
                    assertEquals(BODY.length, server.bodyBytesAfterContinue,
                        name + " did not deliver the body after 100 Continue");
                    break;

                case SENDS_HEADER_ONLY:
                    assertEquals(CONTINUE, server.expectHeader, name + " did not put the header on the wire");
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
    }

    static Stream<Arguments> transports() {
        Object[][] clients = {
            { "netty", "com.azure.core.http.netty.NettyAsyncHttpClientProvider", ContinueSupport.SENDS_HEADER_ONLY },
            { "okhttp", "com.azure.core.http.okhttp.OkHttpAsyncClientProvider", ContinueSupport.DEFERS_BODY },
            { "jdk", "com.azure.core.http.jdk.httpclient.JdkHttpClientProvider", ContinueSupport.DEFERS_BODY },
            { "vertx", "com.azure.core.http.vertx.VertxHttpClientProvider", ContinueSupport.DEFERS_BODY } };

        return Stream.of(clients)
            .flatMap(client -> Stream.of(Arguments.of(client[0] + " sync", client[1], client[2], true),
                Arguments.of(client[0] + " async", client[1], client[2], false)));
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

    private static ExpectContinueOptions options(ExpectContinueMode mode) {
        return new ExpectContinueOptions().setMode(mode);
    }

    private static HttpRequest requestWithBody() throws MalformedURLException {
        return requestWithBody(BODY.length);
    }

    private static HttpRequest requestWithBody(int bodyLength) throws MalformedURLException {
        return new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT)).setBody(new byte[bodyLength]);
    }

    private static HttpPipeline pipeline(ExpectContinueOptions options, HttpClient client) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options);
        return buildPipeline(policies, client);
    }

    private static HttpPipeline retryPipeline(ExpectContinueOptions options, HttpClient client) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestRetryPolicy(FAST_RETRY_OPTIONS));
        BuilderUtils.addExpectContinuePolicy(policies, options);
        return buildPipeline(policies, client);
    }

    private static HttpPipeline buildPipeline(List<HttpPipelinePolicy> policies, HttpClient client) {
        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(client)
            .build();
    }

    private static HttpResponse send(HttpPipeline pipeline, HttpRequest request) {
        return SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));
    }

    /**
     * Records the {@code Expect} header of every request it sees and replies with the given status codes in order,
     * repeating the last one once they are exhausted.
     */
    private static final class RecordingHttpClient extends NoOpHttpClient {
        private final List<String> expectHeaders = new CopyOnWriteArrayList<>();
        private final int[] statusCodes;
        private final AtomicInteger attempt = new AtomicInteger();

        RecordingHttpClient(int... statusCodes) {
            this.statusCodes = statusCodes;
        }

        private HttpResponse handle(HttpRequest request) {
            expectHeaders.add(request.getHeaders().getValue(HttpHeaderName.EXPECT));
            int index = Math.min(attempt.getAndIncrement(), statusCodes.length - 1);
            return new MockHttpResponse(request, statusCodes[index]);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromCallable(() -> handle(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            return handle(request);
        }
    }

    /**
     * A client that reads the observation holder the policy installs and, when the request carries the expect header,
     * optionally sets it, mimicking a transport that observed a {@code 100 Continue}. Records what it saw so the test
     * can assert the round trip.
     */
    private static final class ObservingHttpClient extends NoOpHttpClient {
        private final boolean simulateContinue;
        private final int status;
        private volatile boolean holderInstalled;
        private volatile Boolean observedValue;

        ObservingHttpClient(boolean simulateContinue, int status) {
            this.simulateContinue = simulateContinue;
            this.status = status;
        }

        private HttpResponse handle(HttpRequest request, Context context) {
            Object holder = context.getData(ExpectContinuePolicyHelper.EXPECT_CONTINUE_RECEIVED_KEY).orElse(null);
            if (holder instanceof AtomicBoolean) {
                holderInstalled = true;
                boolean hasExpect = CONTINUE.equals(request.getHeaders().getValue(HttpHeaderName.EXPECT));
                if (simulateContinue && hasExpect) {
                    ((AtomicBoolean) holder).set(true);
                }
                observedValue = ((AtomicBoolean) holder).get();
            } else {
                holderInstalled = false;
            }
            return new MockHttpResponse(request, status);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return Mono.fromCallable(() -> handle(request, context));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            return handle(request, context);
        }
    }

    private enum ContinueSupport {
        /** Waits for {@code 100 Continue} before sending the body. */
        DEFERS_BODY,

        /** Sends the header but streams the body without waiting. */
        SENDS_HEADER_ONLY,

        /** Drops the header, as {@code Expect} is restricted by {@code java.net.http.HttpClient}. */
        DROPS_HEADER
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
