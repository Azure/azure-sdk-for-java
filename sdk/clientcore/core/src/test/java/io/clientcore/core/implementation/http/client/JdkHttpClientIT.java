// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.http.client.JdkHttpClientLocalTestServer.LONG_BODY;
import static io.clientcore.core.implementation.http.client.JdkHttpClientLocalTestServer.RETURN_HEADERS_AS_IS_PATH;
import static io.clientcore.core.implementation.http.client.JdkHttpClientLocalTestServer.SHORT_BODY;
import static io.clientcore.core.implementation.http.client.JdkHttpClientLocalTestServer.TIMEOUT;
import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link JdkHttpClient}.
 * <p>
 * Now that the default HttpClient, and related code, are using multi-release JARs this must be an integration test as
 * the full JAR must be available to use the multi-release code.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientIT {
    private static LocalTestServer server;

    @BeforeAll
    public static void startTestServer() {
        server = JdkHttpClientLocalTestServer.getServer();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = doRequest(client, "/error")) {
            assertEquals(500, response.getStatusCode());

            String responseBodyAsString = response.getValue().toString();

            assertTrue(responseBodyAsString.contains("error"));
        }

    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientBuilder().build();

        ForkJoinPool pool = new ForkJoinPool((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0));
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (Response<BinaryData> response = doRequest(client, "/long")) {
                    byte[] body = response.getValue().toBytes();
                    assertArraysEqual(LONG_BODY, body);
                    return null;
                }
            });
        }

        pool.invokeAll(requests);
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new JdkHttpClientBuilder().build();

        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        HttpHeaders headers = new HttpHeaders().set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        try (Response<?> response = client.send(new HttpRequest().setMethod(HttpMethod.GET)
            .setUri(uri(server, RETURN_HEADERS_AS_IS_PATH))
            .setHeaders(headers))) {
            assertEquals(200, response.getStatusCode());

            HttpHeaders responseHeaders = response.getHeaders();
            HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);

            assertEquals(singleValueHeaderName.getCaseInsensitiveName(), singleValueHeader.getName().toString());
            assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

            HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);

            assertEquals(multiValueHeaderName.getCaseInsensitiveName(), multiValueHeader.getName().toString());
            assertEquals(multiValueHeaderValue.size(), multiValueHeader.getValues().size());
            assertTrue(multiValueHeaderValue.containsAll(multiValueHeader.getValues()));
        }
    }

    @Test
    public void testBufferedResponse() {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = getResponse(client, "/short", RequestContext.none())) {
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    @Test
    public void testEmptyBufferResponse() {
        HttpClient client = new JdkHttpClientBuilder().build();

        try (Response<BinaryData> response = getResponse(client, "/empty", RequestContext.none())) {
            assertEquals(0L, response.getValue().toBytes().length);
        }
    }

    @Test
    public void testRequestBodyPost() {
        HttpClient client = new JdkHttpClientBuilder().build();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri(server, "/shortPost"));
        request.getHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)));
        request.setBody(BinaryData.fromString(contentChunk));

        try (Response<BinaryData> response = client.send(request)) {
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    @Test
    @Disabled("Need to add ProgressReporter support.")
    public void testProgressReporter() {
        HttpClient httpClient = new JdkHttpClientBuilder().build();

        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(uri(server, "/shortPost"))
            .setBody(BinaryData.fromStream(
                new SequenceInputStream(new ByteArrayInputStream(LONG_BODY), new ByteArrayInputStream(SHORT_BODY))));
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(SHORT_BODY.length + LONG_BODY.length));

        //        Contexts contexts = Contexts.with(Context.NONE)
        //            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::add));

        try (Response<BinaryData> response = httpClient.send(request)) {
            assertEquals(200, response.getStatusCode());
            List<Long> progressList = progress.stream().collect(Collectors.toList());
            assertEquals(LONG_BODY.length, progressList.get(0));
            assertEquals(SHORT_BODY.length + LONG_BODY.length, progressList.get(1));
        }
    }

    @Test
    public void noResponseTimesOut() {
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1)).build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<BinaryData> response = doRequest(client, "/noResponse")) {
                assertNotNull(response);
            }
        }));

        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    public void slowStreamReadingTimesOut() {
        // Set both the response timeout and read timeout to make sure we aren't getting a response timeout when the
        // response body is slow to be sent.
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(1))
            .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<BinaryData> response = doRequest(client, "/slowResponse")) {
                assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
            }
        }));

        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    public void slowEagerReadingTimesOut() {
        // Set both the response timeout and read timeout to make sure we aren't getting a response timeout when the
        // response body is slow to be sent.
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(1))
            .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> assertTimeout(Duration.ofSeconds(5), () -> {
            try (Response<BinaryData> response = doRequest(client, "/slowResponse")) {
                assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
            }
        }));

        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    @Disabled("Need to implement per-call timeouts.")
    public void perCallTimeout() {
        HttpClient client = new JdkHttpClientBuilder().responseTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, TIMEOUT));

        // Verify a smaller timeout sent through Context times out the request.
        //        RuntimeException ex = assertThrows(RuntimeException.class,
        //            () -> client.sendSync(request, new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofSeconds(1))));
        //        assertInstanceOf(HttpTimeoutException.class, ex.getCause());

        // Then verify not setting a timeout through Context does not time out the request.
        try (Response<BinaryData> response = client.send(request)) {
            assertEquals(200, response.getStatusCode());
            assertArraysEqual(SHORT_BODY, response.getValue().toBytes());
        }
    }

    private static Response<BinaryData> getResponse(HttpClient client, String path, RequestContext context) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, path)).setContext(context);

        return client.send(request);
    }

    static URI uri(LocalTestServer server, String path) {
        return URI.create(server.getUri() + path);
    }

    static URI httpsUri(LocalTestServer server) {
        return URI.create(server.getHttpsUri() + "/short");
    }

    private static Response<BinaryData> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri(server, path));

        return client.send(request);
    }
}
