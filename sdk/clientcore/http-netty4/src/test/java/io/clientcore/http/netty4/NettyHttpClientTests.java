// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.http.netty4.implementation.MockProxyServer;
import io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer;
import io.netty.handler.proxy.ProxyConnectException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.clientcore.http.netty4.TestUtils.assertArraysEqual;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.EXPECTED_HEADER;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.HTTP_HEADERS_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.LONG_BODY;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.LONG_BODY_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.NO_DOUBLE_UA_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.NULL_REPLACEMENT;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.PROXY_TO_ADDRESS;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.RETURN_HEADERS_AS_IS_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.SHORT_BODY;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.SHORT_BODY_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.SHORT_POST_BODY_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.SHORT_POST_BODY_WITH_VALIDATION_PATH;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.TEST_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class NettyHttpClientTests {
    private static final String SERVER_HTTP_URI = NettyHttpClientLocalTestServer.getServer().getHttpUri();

    @Test
    public void testConcurrentRequestsSync() throws InterruptedException, ExecutionException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();

        ForkJoinPool pool = new ForkJoinPool();
        try {
            List<Future<byte[]>> requests
                = pool.invokeAll(IntStream.range(0, numRequests).mapToObj(ignored -> (Callable<byte[]>) () -> {
                    try (Response<BinaryData> response = sendRequest(client, "/long")) {
                        return response.getValue().toBytes();
                    }
                }).collect(Collectors.toList()), 60, TimeUnit.SECONDS);

            for (Future<byte[]> request : requests) {
                assertArraysEqual(LONG_BODY, request.get());
            }
        } finally {
            pool.shutdown();
        }
    }

    @Test
    public void testResponseShortBodyAsByteArray() throws IOException {
        checkBodyReceived(SHORT_BODY, SHORT_BODY_PATH);
    }

    @Test
    public void testResponseLongBodyAsByteArray() throws IOException {
        checkBodyReceived(LONG_BODY, LONG_BODY_PATH);
    }

    @Test
    public void testProgressReporterSync() throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();

        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri(SHORT_POST_BODY_PATH))
            .setHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(SHORT_BODY.length)))
            .setBody(BinaryData.fromBytes(SHORT_BODY))
            .setContext(RequestContext.builder()
                .putMetadata("progressReporter", ProgressReporter.withProgressListener(progress::add))
                .build());

        try (Response<BinaryData> response = client.send(request)) {
            assertEquals(200, response.getStatusCode());
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
            List<Long> progressList = progress.stream().collect(Collectors.toList());
            assertEquals(SHORT_BODY.length, progressList.get(0));
        }
    }

    @Test
    public void testFileUpload() throws IOException {
        Path tempFile = writeToTempFile();
        tempFile.toFile().deleteOnExit();
        BinaryData body = BinaryData.fromFile(tempFile, 1L, 42L);

        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri(SHORT_POST_BODY_WITH_VALIDATION_PATH))
            .setBody(body);

        try (Response<BinaryData> response = client.send(request)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri(SHORT_POST_BODY_PATH))
            .setHeaders(new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "132"))
            .setBody(BinaryData.fromStream(new InputStream() {
                @Override
                public int read() {
                    throw new RuntimeException("boo");
                }
            }));

        IOException thrown = assertThrows(IOException.class, () -> client.send(request).close());
        RuntimeException causal = assertInstanceOf(RuntimeException.class, thrown.getCause());
        assertEquals("boo", causal.getMessage());
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponseSyncInGetMethod() throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();

        try (Response<BinaryData> response
            = client.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(LONG_BODY_PATH)))) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertArraysEqual(LONG_BODY, response.getValue().toBytes());
        }
    }

    @Test
    @Timeout(20)
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        try (Response<BinaryData> response = sendRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            assertEquals("error", response.getValue().toString());
        }
    }

    @ParameterizedTest
    @MethodSource("requestHeaderSupplier")
    public void requestHeader(String headerValue, String expectedValue) throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri(HTTP_HEADERS_PATH))
            .setHeaders(new HttpHeaders().set(TEST_HEADER, headerValue))
            .setBody(BinaryData.empty());

        try (Response<BinaryData> response = client.send(request)) {
            assertEquals(expectedValue, response.getHeaders().getValue(TEST_HEADER));
        }
    }

    @Test
    public void validateRequestHasOneUserAgentHeader() throws IOException {
        HttpClient httpClient = new NettyHttpClientProvider().getSharedInstance();

        try (Response<BinaryData> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(uri(NO_DOUBLE_UA_PATH))
            .setHeaders(new HttpHeaders().set(HttpHeaderName.USER_AGENT, EXPECTED_HEADER))
            .setBody(BinaryData.empty()))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void validateHeadersReturnAsIs() throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();

        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        HttpHeaders headers = new HttpHeaders().set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        try (Response<BinaryData> response = client.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(uri(RETURN_HEADERS_AS_IS_PATH))
            .setHeaders(headers)
            .setBody(BinaryData.empty()))) {
            assertEquals(200, response.getStatusCode());

            HttpHeaders responseHeaders = response.getHeaders();
            HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);
            assertEquals(singleValueHeaderName.getCaseSensitiveName(),
                singleValueHeader.getName().getCaseSensitiveName());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);
            assertEquals(multiValueHeaderName.getCaseSensitiveName(),
                multiValueHeader.getName().getCaseSensitiveName());
            assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValues());
        }
    }

    // TODO (alzimmer): Uncomment once proper pipelining for proxy authentication is added again.
    //    /**
    //     * This test validates that the eager retrying of Proxy Authentication (407) responses doesn't return to the
    //     * HttpPipeline before connecting.
    //     */
    //    @Test
    //    public void proxyAuthenticationErrorEagerlyRetries() {
    //        // Create a Netty HttpClient to share backing resources that are warmed up before making a time based call.
    //        reactor.netty.http.client.HttpClient warmedUpClient = reactor.netty.http.client.HttpClient.create();
    //        StepVerifier
    //            .create(new NettyAsyncHttpClientBuilder(warmedUpClient).build()
    //                .send(new HttpRequest(HttpMethod.GET, uri(SHORT_BODY_PATH))))
    //            .assertNext(response -> assertEquals(200, response.getStatusCode()))
    //            .verifyComplete();
    //
    //        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
    //            AtomicInteger responseHandleCount = new AtomicInteger();
    //            RetryPolicy retryPolicy = new RetryPolicy(new FixedDelay(3, Duration.ofSeconds(1)));
    //            ProxyOptions proxyOptions
    //                = new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("1", "1");
    //
    //            // Create an HttpPipeline where any exception has a retry delay of 10 seconds.
    //            HttpPipeline httpPipeline = new HttpPipelineBuilder()
    //                .policies(retryPolicy,
    //                    (context, next) -> next.process().doOnNext(ignored -> responseHandleCount.incrementAndGet()))
    //                .httpClient(new NettyAsyncHttpClientBuilder(warmedUpClient).proxy(proxyOptions).build())
    //                .build();
    //
    //            StepVerifier
    //                .create(httpPipeline.send(new HttpRequest(HttpMethod.GET, uri(PROXY_TO_ADDRESS)),
    //                    new Context("azure-eagerly-read-response", true)))
    //                .assertNext(response -> assertEquals(418, response.getStatusCode()))
    //                .expectComplete()
    //                .verify();
    //
    //            assertEquals(1, responseHandleCount.get());
    //        }
    //    }

    /**
     * This test validates that if the eager retrying of Proxy Authentication (407) responses uses all retries returns
     * the correct error.
     */
    // TODO (alzimmer): Reenable test when proxy support work is done.
    //    @Test
    public void failedProxyAuthenticationReturnsCorrectError() {
        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
            HttpClient httpClient = new NettyHttpClientBuilder()
                .proxy(
                    new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("2", "2"))
                .build();

            ProxyConnectException exception = assertThrows(ProxyConnectException.class,
                () -> httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(PROXY_TO_ADDRESS))));
            assertTrue(exception.getMessage().contains("Failed to connect to proxy. Status: "),
                () -> "Expected exception message to contain \"Failed to connect to proxy. Status: \", it was: "
                    + exception.getMessage());
        }
    }

    // TODO (alzimmer): Think about how this test can be reimplemented with just Netty and not Reactor Netty.
    //    @Test
    //    public void sslExceptionWrappedProxyConnectExceptionDoesNotRetryInfinitely() {
    //        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
    //            HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NettyAsyncHttpClientBuilder(
    //                reactor.netty.http.client.HttpClient.create().doOnRequest((req, conn) -> {
    //                    conn.addHandlerLast("sslException", new ChannelOutboundHandlerAdapter() {
    //                        @Override
    //                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    //                            promise.setFailure(new SSLException(new ProxyConnectException("Simulated SSLException")));
    //                        }
    //                    });
    //                })).proxy(
    //                    new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("1", "1"))
    //                    .build())
    //                .build();
    //
    //            StepVerifier.create(httpPipeline.send(new HttpRequest(HttpMethod.GET, uri(PROXY_TO_ADDRESS))))
    //                .verifyErrorMatches(exception -> exception instanceof SSLException
    //                    && exception.getCause() instanceof ProxyConnectException);
    //        }
    //    }

    // TODO (alzimmer): Add this back once/if per-call timeout is supported.
    //    @Test
    //    public void perCallTimeout() {
    //        HttpClient client = new NettyHttpClientBuilder().responseTimeout(Duration.ofSeconds(10)).build();
    //
    //        HttpRequest request = new HttpRequest(HttpMethod.GET, uri(TIMEOUT));
    //
    //        // Verify a smaller timeout sent through Context times out the request.
    //        RuntimeException ex = assertThrows(RuntimeException.class,
    //            () -> client.send(request, new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofSeconds(1))));
    //        assertInstanceOf(TimeoutException.class, ex.getCause());
    //
    //        // Then verify not setting a timeout through Context does not time out the request.
    //        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
    //            assertArraysEqual(SHORT_BODY, response.getBodyAsBinaryData().toBytes());
    //            assertEquals(200, response.getStatusCode());
    //        }
    //    }

    private static Stream<Arguments> requestHeaderSupplier() {
        return Stream.of(Arguments.of(null, NULL_REPLACEMENT), Arguments.of("", ""), Arguments.of("aValue", "aValue"));
    }

    private static Response<BinaryData> getResponse(String path) {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        return getResponse(client, path);
    }

    private static Response<BinaryData> getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(path));
        try {
            return client.send(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static URI uri(String path) {
        return URI.create(SERVER_HTTP_URI + path);
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) throws IOException {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        try (Response<BinaryData> response = sendRequest(client, path)) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            WritableByteChannel body = Channels.newChannel(outStream);
            response.getValue().writeTo(body);
            Assertions.assertArrayEquals(expectedBody, outStream.toByteArray());
        }
    }

    private static Response<BinaryData> sendRequest(HttpClient client, String path) {
        try {
            return client.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(path)));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Path writeToTempFile() throws IOException {
        Path tempFile = Files.createTempFile("data", null);
        tempFile.toFile().deleteOnExit();
        String tempFilePath = tempFile.toString();
        FileOutputStream outputStream = new FileOutputStream(tempFilePath);
        outputStream.write(NettyHttpClientLocalTestServer.LONG_BODY);
        outputStream.close();
        return tempFile;
    }
}
