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
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.models.CoreException;
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
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void testResponseShortBodyAsByteArray() {
        checkBodyReceived(SHORT_BODY, SHORT_BODY_PATH);
    }

    @Test
    public void testResponseLongBodyAsByteArray() {
        checkBodyReceived(LONG_BODY, LONG_BODY_PATH);
    }

    @Test
    public void testProgressReporterSync() {
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

        CoreException thrown = assertThrows(CoreException.class, () -> client.send(request).close());
        RuntimeException causal = assertInstanceOf(RuntimeException.class, thrown.getCause());
        assertEquals("boo", causal.getMessage());
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponseSyncInGetMethod() {
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
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        try (Response<BinaryData> response = sendRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            assertEquals("error", response.getValue().toString());
        }
    }

    @ParameterizedTest
    @MethodSource("requestHeaderSupplier")
    public void requestHeader(String headerValue, String expectedValue) {
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
    public void validateRequestHasOneUserAgentHeader() {
        HttpClient httpClient = new NettyHttpClientProvider().getSharedInstance();

        try (Response<BinaryData> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(uri(NO_DOUBLE_UA_PATH))
            .setHeaders(new HttpHeaders().set(HttpHeaderName.USER_AGENT, EXPECTED_HEADER))
            .setBody(BinaryData.empty()))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void validateHeadersReturnAsIs() {
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

    /**
     * This test validates that the eager retrying of Proxy Authentication (407) responses doesn't return to the
     * HttpPipeline before connecting.
     */
    @Test
    public void proxyAuthenticationErrorEagerlyRetries() throws IOException {
        // Create a Netty HttpClient to share backing resources that are warmed up before making a time based call.
        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
            AtomicInteger responseHandleCount = new AtomicInteger();
            HttpRetryPolicy retryPolicy = new HttpRetryPolicy(new HttpRetryOptions(3, Duration.ofSeconds(1)));
            ProxyOptions proxyOptions
                = new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("1", "1");

            // Create an HttpPipeline where any exception has a retry delay of 10 seconds.
            HttpPipeline httpPipeline = new HttpPipelineBuilder().addPolicy(retryPolicy).addPolicy((context, next) -> {
                Response<BinaryData> response = next.process();
                responseHandleCount.incrementAndGet();
                return response;
            }).httpClient(new NettyHttpClientBuilder().proxy(proxyOptions).build()).build();

            try (Response<BinaryData> response
                = httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(PROXY_TO_ADDRESS)))) {
                assertEquals(418, response.getStatusCode());
            }

            assertEquals(1, responseHandleCount.get());
        }
    }

    /**
     * This test validates that if the eager retrying of Proxy Authentication (407) responses uses all retries returns
     * the correct error.
     */
    @Test
    public void failedProxyAuthenticationReturnsCorrectError() {
        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
            HttpClient httpClient = new NettyHttpClientBuilder()
                .proxy(
                    new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("2", "2"))
                .build();

            CoreException coreException = assertThrows(CoreException.class,
                () -> httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(PROXY_TO_ADDRESS))));

            Throwable exception = coreException.getCause();
            assertInstanceOf(ProxyConnectException.class, exception);

            assertTrue(coreException.getCause().getMessage().contains("Proxy Authentication Required"),
                () -> "Expected exception message to contain \"Proxy Authentication Required\", it was: "
                    + coreException.getCause().getMessage());
        }
    }

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
        return client.send(request);
    }

    private static URI uri(String path) {
        return URI.create(SERVER_HTTP_URI + path);
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) {
        HttpClient client = new NettyHttpClientProvider().getSharedInstance();
        try (Response<BinaryData> response = sendRequest(client, path)) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            WritableByteChannel body = Channels.newChannel(outStream);
            response.getValue().writeTo(body);
            Assertions.assertArrayEquals(expectedBody, outStream.toByteArray());
        }
    }

    private static Response<BinaryData> sendRequest(HttpClient client, String path) {
        return client.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(path)));
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
