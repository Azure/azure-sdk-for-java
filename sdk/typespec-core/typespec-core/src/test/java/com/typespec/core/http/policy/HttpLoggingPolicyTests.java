// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpHeader;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.clients.NoOpHttpClient;
import com.typespec.core.implementation.AccessibleByteArrayOutputStream;
import com.typespec.core.implementation.util.EnvironmentConfiguration;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.logging.LogLevel;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.typespec.core.CoreTestUtils.assertArraysEqual;
import static com.typespec.core.CoreTestUtils.createUrl;
import static com.typespec.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains tests for {@link HttpLoggingPolicy}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
@ResourceLock(Resources.SYSTEM_OUT)
public class HttpLoggingPolicyTests {
    private static final String REDACTED = "REDACTED";
    private static final Context CONTEXT = new Context("caller-method", HttpLoggingPolicyTests.class.getName());
    private static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    private static String initialLogLevel;
    private static PrintStream originalSystemOut;

    private AccessibleByteArrayOutputStream logCaptureStream;

    @BeforeAll
    public static void captureInitialLogLevel() {
        initialLogLevel = EnvironmentConfiguration.getGlobalConfiguration().get(PROPERTY_AZURE_LOG_LEVEL);
        originalSystemOut = System.out;
    }

    @AfterAll
    public static void resetInitialLogLevel() {
        if (initialLogLevel == null) {
            EnvironmentConfiguration.getGlobalConfiguration().remove(PROPERTY_AZURE_LOG_LEVEL);
        } else {
            EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, initialLogLevel);
        }

        System.setOut(originalSystemOut);
    }

    @BeforeEach
    public void prepareForTest() {
        // Set the log level to information for the test.
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());

        /*
         * DefaultLogger uses System.out to log. Inject a custom PrintStream to log into for the duration of the test to
         * capture the log messages.
         */
        logCaptureStream = new AccessibleByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void cleanupAfterTest() {
        // Reset or clear the log level after the test completes.
        clearTestLogLevel();
    }

    /**
     * Tests that a query string will be properly redacted before it is logged.
     */
    @ParameterizedTest
    @MethodSource("redactQueryParametersSupplier")
    public void redactQueryParameters(String requestUrl, String expectedQueryString,
                                      Set<String> allowedQueryParameters) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BASIC)
                .setAllowedQueryParamNames(allowedQueryParameters)))
            .httpClient(new NoOpHttpClient())
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl), CONTEXT))
            .verifyComplete();

        assertTrue(convertOutputStreamToString(logCaptureStream).contains(expectedQueryString));
    }

    /**
     * Tests that a query string will be properly redacted before it is logged.
     */
    @ParameterizedTest
    @MethodSource("redactQueryParametersSupplier")
    public void redactQueryParametersSync(String requestUrl, String expectedQueryString,
                                          Set<String> allowedQueryParameters) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BASIC)
                .setAllowedQueryParamNames(allowedQueryParameters)))
            .httpClient(new NoOpHttpClient())
            .build();

        pipeline.sendSync(new HttpRequest(HttpMethod.POST, requestUrl), CONTEXT);

        assertTrue(convertOutputStreamToString(logCaptureStream).contains(expectedQueryString));
    }

    private static Stream<Arguments> redactQueryParametersSupplier() {
        String requestUrl = "https://localhost?sensitiveQueryParameter=sensitiveValue&queryParameter=value";

        String expectedFormat = "sensitiveQueryParameter=%s&queryParameter=%s";
        String fullyRedactedQueryString = String.format(expectedFormat, REDACTED, REDACTED);
        String sensitiveRedactionQueryString = String.format(expectedFormat, REDACTED, "value");
        String fullyAllowedQueryString = String.format(expectedFormat, "sensitiveValue", "value");

        Set<String> allQueryParameters = new HashSet<>();
        allQueryParameters.add("sensitiveQueryParameter");
        allQueryParameters.add("queryParameter");

        return Stream.of(
            // All query parameters should be redacted.
            Arguments.of(requestUrl, fullyRedactedQueryString, new HashSet<String>()),

            // Only the sensitive query parameter should be redacted.
            Arguments.of(requestUrl, sensitiveRedactionQueryString, Collections.singleton("queryParameter")),

            // No query parameters are redacted.
            Arguments.of(requestUrl, fullyAllowedQueryString, allQueryParameters)
        );
    }

    /**
     * Tests that logging the request body doesn't consume the stream before it is sent over the network.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeRequest(Flux<ByteBuffer> stream, byte[] data, int contentLength)
        throws MalformedURLException {
        String url = "https://test.com/validateLoggingDoesNotConsumeRequest";
        HttpHeaders requestHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArraysEqual(data, bytes))
                .then(Mono.empty()))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.POST, createUrl(url), requestHeaders, stream),
                CONTEXT))
            .verifyComplete();

        String logString = convertOutputStreamToString(logCaptureStream);
        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
        assertEquals(1, messages.size());

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, data);
        expectedRequest.assertEqual(messages.get(0), HttpLogDetailLevel.BODY, LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging the request body doesn't consume the stream before it is sent over the network.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplierSync")
    @Execution(ExecutionMode.SAME_THREAD)
    public void validateLoggingDoesNotConsumeRequestSync(BinaryData requestBody, byte[] data, int contentLength)
        throws MalformedURLException {
        String url = "https://test.com/validateLoggingDoesNotConsumeRequestSync";
        HttpHeaders requestHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArraysEqual(data, bytes))
                .then(Mono.empty()))
            .build();

        pipeline.sendSync(new HttpRequest(HttpMethod.POST, createUrl(url), requestHeaders, requestBody), CONTEXT);

        String logString = convertOutputStreamToString(logCaptureStream);
        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
        assertEquals(1, messages.size());

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, data);
        expectedRequest.assertEqual(messages.get(0), HttpLogDetailLevel.BODY, LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeResponse(Flux<ByteBuffer> stream, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com/validateLoggingDoesNotConsumeResponse");
        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> Mono.just(new MockHttpResponse(ignored, responseHeaders, stream)))
            .build();

        StepVerifier.create(pipeline.send(request, CONTEXT))
            .assertNext(response -> StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(response.getBody()))
                .assertNext(bytes -> assertArraysEqual(data, bytes))
                .verifyComplete())
            .verifyComplete();

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplierSync")
    public void validateLoggingDoesNotConsumeResponseSync(BinaryData responseBody, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test./validateLoggingDoesNotConsumeResponseSync");
        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> Mono.just(new MockHttpResponse(ignored, responseHeaders, responseBody)))
            .build();

        try (HttpResponse response = pipeline.sendSync(request, CONTEXT)) {
            assertArraysEqual(data, response.getBodyAsByteArray().block());
        }

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    private static Stream<Arguments> validateLoggingDoesNotConsumeSupplierSync() {
        byte[] data = "this is a test".getBytes(StandardCharsets.UTF_8);

        return Stream.of(
            Arguments.of(BinaryData.fromBytes(data), data, data.length),

            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(data), (long) data.length), data, data.length)
        );
    }

    private static Stream<Arguments> validateLoggingDoesNotConsumeSupplier() {
        byte[] data = "this is a test".getBytes(StandardCharsets.UTF_8);
        byte[] repeatingData = new byte[data.length * 3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(data, 0, repeatingData, i * data.length, data.length);
        }

        return Stream.of(
            // Single emission cold flux.
            Arguments.of(Flux.just(ByteBuffer.wrap(data)), data, data.length),

            // Single emission Stream based Flux.
            Arguments.of(Flux.fromStream(Stream.of(ByteBuffer.wrap(data))), data, data.length),

            // Single emission hot flux.
            Arguments.of(Flux.just(ByteBuffer.wrap(data)).publish().autoConnect(), data, data.length),

            // Multiple emission cold flux.
            Arguments.of(Flux.fromArray(new ByteBuffer[] {
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data)
            }), repeatingData, repeatingData.length),

            // Multiple emission Stream based flux.
            Arguments.of(Flux.fromStream(Stream.of(
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data)
            )), repeatingData, repeatingData.length),

            // Multiple emission hot flux.
            Arguments.of(Flux.just(
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data),
                ByteBuffer.wrap(data)
            ).publish().autoConnect(), repeatingData, repeatingData.length)
        );
    }

    private static class MockHttpResponse extends HttpResponse {
        private final HttpHeaders headers;
        private final Flux<ByteBuffer> body;
        private final BinaryData binaryDataBody;

        MockHttpResponse(HttpRequest request, HttpHeaders headers, Flux<ByteBuffer> body) {
            super(request);
            this.headers = headers;
            this.body = body;
            this.binaryDataBody = null;
        }

        MockHttpResponse(HttpRequest request, HttpHeaders headers, BinaryData body) {
            super(request);
            this.headers = headers;
            this.binaryDataBody = body;
            this.body = null;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return headers.getValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            if (body == null && binaryDataBody != null) {
                return binaryDataBody.toFluxByteBuffer();
            } else {
                return body;
            }
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(value = HttpLogDetailLevel.class, mode = EnumSource.Mode.INCLUDE,
        names = {"BASIC", "HEADERS", "BODY", "BODY_AND_HEADERS"})
    public void loggingIncludesRetryCount(HttpLogDetailLevel logLevel) {
        AtomicInteger requestCount = new AtomicInteger();
        String url = "https://test.com/loggingIncludesRetryCount/" + logLevel;
        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        byte[] responseBody = new byte[] {24, 42};
        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
            .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(), new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
            .httpClient(ignored -> (requestCount.getAndIncrement() == 0)
                ? Mono.error(new RuntimeException("Try again!"))
                : Mono.fromCallable(
                    () -> new com.typespec.core.http.MockHttpResponse(ignored, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRetry1 = HttpLogMessage.request(HttpMethod.GET, url, null)
            .setTryCount(1)
            .setHeaders(request.getHeaders());
        HttpLogMessage expectedRetry2 = HttpLogMessage.request(HttpMethod.GET, url, null)
            .setTryCount(2)
            .setHeaders(request.getHeaders());
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200)
            .setHeaders(responseHeaders);

        StepVerifier.create(pipeline.send(request, CONTEXT)
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody()))
                .doFinally(s -> {
                    String logString = convertOutputStreamToString(logCaptureStream);
                    List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
                    assertEquals(3, messages.size());

                    expectedRetry1.assertEqual(messages.get(0), logLevel, LogLevel.INFORMATIONAL);
                    expectedRetry2.assertEqual(messages.get(1), logLevel, LogLevel.INFORMATIONAL);
                    expectedResponse.assertEqual(messages.get(2), logLevel, LogLevel.INFORMATIONAL);
                }))
            .assertNext(body -> assertArraysEqual(responseBody, body))
            .verifyComplete();
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(value = HttpLogDetailLevel.class, mode = EnumSource.Mode.INCLUDE,
        names = {"BASIC", "HEADERS", "BODY", "BODY_AND_HEADERS"})
    public void loggingHeadersAndBodyVerbose(HttpLogDetailLevel logLevel) {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());
        byte[] requestBody = new byte[] {42};
        byte[] responseBody = new byte[] {24, 42};
        String url = "https://test.com/loggingHeadersAndBodyVerbose/" + logLevel;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url)
            .setBody(requestBody)
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
            .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(), new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
            .httpClient(r -> Mono.just(new com.typespec.core.http.MockHttpResponse(r, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, requestBody)
            .setHeaders(request.getHeaders())
            .setTryCount(1);
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200)
            .setHeaders(responseHeaders);

        StepVerifier.create(pipeline.send(request, CONTEXT)
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody()))
                .doFinally(s -> {
                    String logString = convertOutputStreamToString(logCaptureStream);

                    List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
                    assertEquals(2, messages.size());

                    expectedRequest.assertEqual(messages.get(0), logLevel, LogLevel.VERBOSE);
                    expectedResponse.assertEqual(messages.get(1), logLevel, LogLevel.VERBOSE);
                }))
            .assertNext(body -> assertArraysEqual(responseBody, body))
            .verifyComplete();
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(value = HttpLogDetailLevel.class, mode = EnumSource.Mode.INCLUDE,
        names = {"BASIC", "HEADERS", "BODY", "BODY_AND_HEADERS"})
    public void loggingIncludesRetryCountSync(HttpLogDetailLevel logLevel) {
        AtomicInteger requestCount = new AtomicInteger();
        String url = "https://test.com/loggingIncludesRetryCountSync/" + logLevel;
        HttpRequest request = new HttpRequest(HttpMethod.GET, url)
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        byte[] responseBody = new byte[] {24, 42};
        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
            .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(), new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
            .httpClient(ignored -> (requestCount.getAndIncrement() == 0)
                ? Mono.error(new RuntimeException("Try again!"))
                : Mono.just(new com.typespec.core.http.MockHttpResponse(ignored, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRetry1 = HttpLogMessage.request(HttpMethod.GET, url, null)
            .setTryCount(1)
            .setHeaders(request.getHeaders());
        HttpLogMessage expectedRetry2 = HttpLogMessage.request(HttpMethod.GET, url, null)
            .setTryCount(2)
            .setHeaders(request.getHeaders());
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200)
            .setHeaders(responseHeaders);

        try (HttpResponse response = pipeline.sendSync(request, CONTEXT)) {
            BinaryData content = response.getBodyAsBinaryData();
            assertEquals(2, requestCount.get());
            String logString = convertOutputStreamToString(logCaptureStream);

            // if HttpLoggingPolicy logger was created when verbose was enabled,
            // there is no way to change it.
            List<HttpLogMessage> messages = HttpLogMessage.fromString(logString).stream()
                .filter(m -> !m.getMessage().equals("Error resume.")).collect(Collectors.toList());

            assertEquals(3, messages.size(), logString);

            expectedRetry1.assertEqual(messages.get(0), logLevel, LogLevel.INFORMATIONAL);
            expectedRetry2.assertEqual(messages.get(1), logLevel, LogLevel.INFORMATIONAL);
            expectedResponse.assertEqual(messages.get(2), logLevel, LogLevel.INFORMATIONAL);

            assertArraysEqual(responseBody, content.toBytes());
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @EnumSource(value = HttpLogDetailLevel.class, mode = EnumSource.Mode.INCLUDE,
        names = {"BASIC", "HEADERS", "BODY", "BODY_AND_HEADERS"})
    public void loggingHeadersAndBodyVerboseSync(HttpLogDetailLevel logLevel) {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());
        byte[] requestBody = new byte[] {42};
        byte[] responseBody = new byte[] {24, 42};
        String url = "https://test.com/loggingHeadersAndBodyVerboseSync/" + logLevel;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url)
            .setBody(requestBody)
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        HttpHeaders responseHeaders = new HttpHeaders()
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
            .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(), new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
            .httpClient(r -> Mono.just(new com.typespec.core.http.MockHttpResponse(r, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, requestBody)
            .setHeaders(request.getHeaders())
            .setTryCount(1);
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200)
            .setHeaders(responseHeaders);

        try (HttpResponse response = pipeline.sendSync(request, CONTEXT)) {
            assertArraysEqual(responseBody, response.getBodyAsByteArray().block());

            String logString = convertOutputStreamToString(logCaptureStream);

            // if HttpLoggingPolicy logger was created when verbose was enabled,
            // there is no way to change it.
            List<HttpLogMessage> messages = HttpLogMessage.fromString(logString).stream()
                .filter(m -> !m.getMessage().equals("Error resume.")).collect(Collectors.toList());

            assertEquals(2, messages.size(), logString);

            expectedRequest.assertEqual(messages.get(0), logLevel, LogLevel.VERBOSE);
            expectedResponse.assertEqual(messages.get(1), logLevel, LogLevel.VERBOSE);
        }
    }

    private void setupLogLevel(int logLevelToSet) {
        EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(logLevelToSet));
    }

    private void clearTestLogLevel() {
        EnvironmentConfiguration.getGlobalConfiguration().remove(PROPERTY_AZURE_LOG_LEVEL);
    }

    private static String convertOutputStreamToString(AccessibleByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }

    public static class HttpLogMessage {
        private static final ObjectMapper SERIALIZER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        private static final Integer MAGIC_NUMBER = 42;

        @JsonProperty("az.sdk.message")
        private String message;

        @JsonProperty("method")
        private String method;

        @JsonProperty("url")
        private String url;

        @JsonProperty("contentLength")
        private Integer contentLength;

        @JsonProperty("body")
        private String body;

        @JsonProperty("tryCount")
        private Integer tryCount;

        @JsonProperty("statusCode")
        private Integer statusCode;

        @JsonProperty("durationMs")
        private Integer durationMs;

        private final Map<String, String> headers = new HashMap<>();

        public HttpLogMessage() {
        }

        private static HttpLogMessage request(HttpMethod method, String url, byte[] body) {
            return new HttpLogMessage()
                .setMessage("HTTP request")
                .setMethod(method.toString())
                .setUrl(url)
                .setBody(body != null ? new String(body, StandardCharsets.UTF_8) : null)
                .setContentLength(body == null ? 0 : body.length);
        }

        private static HttpLogMessage response(String url, byte[] body, Integer statusCode) {
            return new HttpLogMessage()
                .setMessage("HTTP response")
                .setUrl(url)
                .setStatusCode(statusCode)
                .setDurationMs(MAGIC_NUMBER)
                .setBody(body != null ? new String(body, StandardCharsets.UTF_8) : null)
                .setContentLength(body == null ? 0 : body.length);
        }


        public HttpLogMessage setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public HttpLogMessage setMethod(String method) {
            this.method = method;
            return this;
        }

        public String getMethod() {
            return method;
        }

        public HttpLogMessage setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public HttpLogMessage setContentLength(Integer contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public Integer getContentLength() {
            return contentLength;
        }

        public HttpLogMessage setTryCount(Integer tryCount) {
            this.tryCount = tryCount;
            return this;
        }

        public Integer getTryCount() {
            return tryCount;
        }

        public HttpLogMessage setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public HttpLogMessage setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Integer getDurationMs() {
            return durationMs;
        }

        public HttpLogMessage setBody(String body) {
            this.body = body;
            return this;
        }

        public String getBody() {
            return body;
        }

        @JsonAnyGetter
        public Map<String, String> getHeaders() {
            return headers;
        }

        @JsonAnySetter
        public HttpLogMessage addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public HttpLogMessage setHeaders(HttpHeaders headers) {
            for (HttpHeader h : headers) {
                this.headers.put(h.getName(), h.getValue());
            }

            return this;
        }

        public static List<HttpLogMessage> fromString(String logRecord) {

            List<HttpLogMessage> messages = new ArrayList<>();

            int start = logRecord.indexOf("{\"az.sdk.message\"");
            for (; start >= 0; start = logRecord.indexOf("{\"az.sdk.message\"", start + 1)) {
                String msg = logRecord.substring(start, logRecord.lastIndexOf("}") + 1);
                try {
                    messages.add(SERIALIZER.readValue(msg, HttpLogMessage.class));
                } catch (JsonMappingException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    fail(ex);
                }
            }

            return messages;
        }

        void assertEqual(HttpLogMessage other, HttpLogDetailLevel httpLevel, LogLevel logLevel) {

            assertEquals(this.message, other.message);
            assertEquals(this.method, other.method);
            assertEquals(this.url, other.url);
            assertEquals(this.contentLength, other.contentLength);
            assertEquals(this.tryCount, other.tryCount);
            assertEquals(this.statusCode, other.statusCode);
            if (this.durationMs != null) {
                assertNotNull(other.durationMs);
            }

            if (httpLevel.shouldLogBody()) {
                assertEquals(this.body, other.body);
            }

            if (httpLevel.shouldLogHeaders() && logLevel == LogLevel.VERBOSE) {
                assertEquals(this.headers.size(), other.headers.size());

                for (Map.Entry<String, String> kvp : this.headers.entrySet()) {
                    assertEquals(kvp.getValue(), other.headers.get(kvp.getKey()));
                }
            }
        }
    }
}
