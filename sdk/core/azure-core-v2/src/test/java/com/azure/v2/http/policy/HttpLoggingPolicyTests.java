// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import io.clientcore.core.http.ContentType;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.clients.NoOpHttpClient;
import com.azure.core.v2.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.v2.implementation.accesshelpers.ClientLoggerAccessHelper;
import com.azure.core.v2.implementation.logging.DefaultLogger;
import com.azure.core.v2.util.BinaryData;
import io.clientcore.core.util.Context;
import com.azure.core.v2.util.FluxUtil;
import com.azure.core.v2.util.logging.LogLevel;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.createUrl;
import static com.azure.core.http.HttpHeaderName.X_MS_REQUEST_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains tests for {@link HttpLoggingPolicy}.
 */
public class HttpLoggingPolicyTests {
    private static final String REDACTED = "REDACTED";

    private final AccessibleByteArrayOutputStream logCaptureStream = new AccessibleByteArrayOutputStream();

    /**
     * Tests that a query string will be properly redacted before it is logged.
     */
    @ParameterizedTest
    @MethodSource("redactQueryParametersSupplier")
    public void redactQueryParameters(String requestUrl, String expectedQueryString,
        Set<String> allowedQueryParameters) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC)
                .setAllowedQueryParamNames(allowedQueryParameters)))
            .httpClient(new NoOpHttpClient())
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl),
            getCallerMethodContext("redactQueryParameters", LogLevel.INFORMATIONAL))).verifyComplete();

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
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC)
                .setAllowedQueryParamNames(allowedQueryParameters)))
            .httpClient(new NoOpHttpClient())
            .build();

        pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl),
            getCallerMethodContext("redactQueryParametersSync", LogLevel.INFORMATIONAL));

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
            Arguments.of(requestUrl, fullyAllowedQueryString, allQueryParameters));
    }

    /**
     * Tests that logging the request body doesn't consume the stream before it is sent over the network.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeRequest(Flux<ByteBuffer> stream, byte[] data, int contentLength)
        throws MalformedURLException {
        String url = "https://test.com/validateLoggingDoesNotConsumeRequest";
        HttpHeaders requestHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY);
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpLoggingPolicy(logOptions))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArraysEqual(data, bytes))
                .then(Mono.empty()))
            .build();

        StepVerifier
            .create(pipeline.send(new HttpRequest(HttpMethod.POST, createUrl(url), requestHeaders, stream),
                getCallerMethodContext("validateLoggingDoesNotConsumeRequest", LogLevel.INFORMATIONAL)))
            .verifyComplete();

        String logString = convertOutputStreamToString(logCaptureStream);
        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
        assertEquals(1, messages.size());

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, data);
        expectedRequest.assertEqual(messages.get(0), logOptions, LogLevel.INFORMATIONAL);
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
        HttpHeaders requestHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY);
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpLoggingPolicy(logOptions))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArraysEqual(data, bytes))
                .then(Mono.empty()))
            .build();

        pipeline.send(new HttpRequest(HttpMethod.POST, createUrl(url), requestHeaders, requestBody),
            getCallerMethodContext("validateLoggingDoesNotConsumeRequestSync", LogLevel.INFORMATIONAL));

        String logString = convertOutputStreamToString(logCaptureStream);
        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
        assertEquals(1, messages.size());

        HttpLogMessage expectedRequest = HttpLogMessage.request(HttpMethod.POST, url, data);
        expectedRequest.assertEqual(messages.get(0), logOptions, LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeResponse(Flux<ByteBuffer> stream, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com/validateLoggingDoesNotConsumeResponse");
        HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> new MockHttpResponse(ignored, responseHeaders, stream)))
            .build();

        StepVerifier
            .create(pipeline.send(request,
                getCallerMethodContext("validateLoggingDoesNotConsumeResponse", LogLevel.INFORMATIONAL)))
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
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://test./validateLoggingDoesNotConsumeResponseSync");
        HttpHeaders responseHeaders = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON)
            .set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> new MockHttpResponse(ignored, responseHeaders, responseBody)))
            .build();

        try (Response<?> response = pipeline.send(request,
            getCallerMethodContext("validateLoggingDoesNotConsumeResponseSync", LogLevel.INFORMATIONAL))) {
            assertArraysEqual(data, response.getBodyAsBinaryData().toBytes());
        }

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    private static Stream<Arguments> validateLoggingDoesNotConsumeSupplierSync() {
        byte[] data = "this is a test".getBytes(StandardCharsets.UTF_8);

        return Stream.of(Arguments.of(BinaryData.fromBytes(data), data, data.length),

            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(data), (long) data.length), data, data.length));
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
            Arguments.of(
                Flux.fromArray(
                    new ByteBuffer[] { ByteBuffer.wrap(data), ByteBuffer.wrap(data), ByteBuffer.wrap(data) }),
                repeatingData, repeatingData.length),

            // Multiple emission Stream based flux.
            Arguments.of(
                Flux.fromStream(Stream.of(ByteBuffer.wrap(data), ByteBuffer.wrap(data), ByteBuffer.wrap(data))),
                repeatingData, repeatingData.length),

            // Multiple emission hot flux.
            Arguments.of(
                Flux.just(ByteBuffer.wrap(data), ByteBuffer.wrap(data), ByteBuffer.wrap(data)).publish().autoConnect(),
                repeatingData, repeatingData.length));
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
        public byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body);
        }

        @Override
        public String> getBodyAsString() {
            return getBodyAsString(StandardCharsets.UTF_8);
        }

        @Override
        public String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("logOptionsSupplier")
    public void loggingIncludesRetryCount(HttpLogOptions logOptions) {
        AtomicInteger requestCount = new AtomicInteger();
        String url = "https://test.com/loggingIncludesRetryCount/" + logOptions.getLogLevel();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url).setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID,
            "client-request-id");

        byte[] responseBody = new byte[] { 24, 42 };
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
                .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(), new HttpLoggingPolicy(logOptions))
            .httpClient(ignored -> (requestCount.getAndIncrement() == 0)
                ? Mono.error(new RuntimeException("Try again!"))
                : new com.azure.core.http.MockHttpResponse(ignored, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRetry1
            = HttpLogMessage.request(HttpMethod.GET, url, null).setTryCount(1).setHeaders(request.getHeaders());
        HttpLogMessage expectedRetry2
            = HttpLogMessage.request(HttpMethod.GET, url, null).setTryCount(2).setHeaders(request.getHeaders());
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200).setHeaders(responseHeaders);

        StepVerifier
            .create(pipeline.send(request, getCallerMethodContext("loggingIncludesRetryCount", LogLevel.INFORMATIONAL))
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody())))
            .assertNext(body -> assertArraysEqual(responseBody, body))
            .verifyComplete();

        String logString = convertOutputStreamToString(logCaptureStream);

        // if HttpLoggingPolicy logger was created when verbose was enabled,
        // there is no way to change it.
        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString)
            .stream()
            .filter(m -> !m.getMessage().equals("Error resume."))
            .collect(Collectors.toList());

        expectedRetry1.assertEqual(messages.get(0), logOptions, LogLevel.INFORMATIONAL);
        assertEquals("HTTP FAILED", messages.get(1).getMessage());
        expectedRetry2.assertEqual(messages.get(2), logOptions, LogLevel.INFORMATIONAL);
        expectedResponse.assertEqual(messages.get(3), logOptions, LogLevel.INFORMATIONAL);

        assertEquals(4, messages.size());
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("logOptionsSupplier")
    public void loggingHeadersAndBodyVerbose(HttpLogOptions logOptions) {
        byte[] requestBody = new byte[] { 42 };
        byte[] responseBody = new byte[] { 24, 42 };
        String url = "https://test.com/loggingHeadersAndBodyVerbose/" + logOptions.getLogLevel();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url).setBody(requestBody)
            .setHeader(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
                .set(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
                .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(), new HttpLoggingPolicy(logOptions))
            .httpClient(r -> new com.azure.core.http.MockHttpResponse(r, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRequest
            = HttpLogMessage.request(HttpMethod.POST, url, requestBody).setHeaders(request.getHeaders()).setTryCount(1);
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200).setHeaders(responseHeaders);

        StepVerifier
            .create(pipeline.send(request, getCallerMethodContext("loggingHeadersAndBodyVerbose", LogLevel.VERBOSE))
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody())))
            .assertNext(body -> assertArraysEqual(responseBody, body))
            .verifyComplete();

        String logString = convertOutputStreamToString(logCaptureStream);

        List<HttpLogMessage> messages = HttpLogMessage.fromString(logString);
        assertEquals(2, messages.size());

        expectedRequest.assertEqual(messages.get(0), logOptions, LogLevel.VERBOSE);
        expectedResponse.assertEqual(messages.get(1), logOptions, LogLevel.VERBOSE);
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("logOptionsSupplier")
    public void loggingIncludesRetryCountSync(HttpLogOptions logOptions) {
        AtomicInteger requestCount = new AtomicInteger();
        String url = "https://test.com/loggingIncludesRetryCountSync/" + logOptions.getLogLevel();
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, url).setHeader(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
                .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        byte[] responseBody = new byte[] { 24, 42 };
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
                .set(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
                .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(), new HttpLoggingPolicy(logOptions))
            .httpClient(ignored -> (requestCount.getAndIncrement() == 0)
                ? Mono.error(new RuntimeException("Try again!"))
                : new com.azure.core.http.MockHttpResponse(ignored, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRetry1
            = HttpLogMessage.request(HttpMethod.GET, url, null).setTryCount(1).setHeaders(request.getHeaders());
        HttpLogMessage expectedRetry2
            = HttpLogMessage.request(HttpMethod.GET, url, null).setTryCount(2).setHeaders(request.getHeaders());
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200).setHeaders(responseHeaders);

        try (Response<?> response
            = pipeline.send(request, getCallerMethodContext("loggingIncludesRetryCountSync", LogLevel.INFORMATIONAL))) {
            BinaryData content = response.getBodyAsBinaryData();
            assertEquals(2, requestCount.get());
            String logString = convertOutputStreamToString(logCaptureStream);

            // if HttpLoggingPolicy logger was created when verbose was enabled,
            // there is no way to change it.
            List<HttpLogMessage> messages = HttpLogMessage.fromString(logString)
                .stream()
                .filter(m -> !m.getMessage().equals("Error resume."))
                .collect(Collectors.toList());

            assertEquals(4, messages.size(), logString);

            expectedRetry1.assertEqual(messages.get(0), logOptions, LogLevel.INFORMATIONAL);

            assertEquals("HTTP FAILED", messages.get(1).getMessage());
            assertEquals("client-request-id", messages.get(1).getHeaders().get("x-ms-client-request-id"));
            assertEquals("Try again!", messages.get(1).getHeaders().get("exception"));

            expectedRetry2.assertEqual(messages.get(2), logOptions, LogLevel.INFORMATIONAL);
            expectedResponse.assertEqual(messages.get(3), logOptions, LogLevel.INFORMATIONAL);

            assertArraysEqual(responseBody, content.toBytes());
        }
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("logOptionsSupplier")
    public void loggingHeadersAndBodyVerboseSync(HttpLogOptions logOptions) {
        byte[] requestBody = new byte[] { 42 };
        byte[] responseBody = new byte[] { 24, 42 };
        String url = "https://test.com/loggingHeadersAndBodyVerboseSync/" + logOptions.getLogLevel();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url).setBody(requestBody)
            .setHeader(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
            .setHeader(HttpHeaderName.X_MS_CLIENT_REQUEST_ID, "client-request-id");

        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, Integer.toString(responseBody.length))
                .set(HttpHeaderName.AUTHORIZATION, "not-allowed-value")
                .set(X_MS_REQUEST_ID, "server-request-id");

        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy(), new HttpLoggingPolicy(logOptions))
            .httpClient(r -> new com.azure.core.http.MockHttpResponse(r, 200, responseHeaders, responseBody)))
            .build();

        HttpLogMessage expectedRequest
            = HttpLogMessage.request(HttpMethod.POST, url, requestBody).setHeaders(request.getHeaders()).setTryCount(1);
        HttpLogMessage expectedResponse = HttpLogMessage.response(url, responseBody, 200).setHeaders(responseHeaders);

        try (Response<?> response
            = pipeline.send(request, getCallerMethodContext("loggingHeadersAndBodyVerboseSync", LogLevel.VERBOSE))) {
            assertArraysEqual(responseBody, response.getBodyAsBinaryData().toBytes());

            String logString = convertOutputStreamToString(logCaptureStream);

            // if HttpLoggingPolicy logger was created when verbose was enabled,
            // there is no way to change it.
            List<HttpLogMessage> messages = HttpLogMessage.fromString(logString)
                .stream()
                .filter(m -> !m.getMessage().equals("Error resume."))
                .collect(Collectors.toList());

            assertEquals(2, messages.size(), logString);

            expectedRequest.assertEqual(messages.get(0), logOptions, LogLevel.VERBOSE);
            expectedResponse.assertEqual(messages.get(1), logOptions, LogLevel.VERBOSE);
        }
    }

    private static Stream<HttpLogOptions> logOptionsSupplier() {
        return Stream.of(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC),
            new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS),
            new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS).disableRedactedHeaderLogging(true),
            new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
            new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS).disableRedactedHeaderLogging(true));
    }

    private Context getCallerMethodContext(String testMethodName, LogLevel logLevel) {
        Logger logger = new DefaultLogger(HttpLoggingPolicyTests.class.getName() + "." + testMethodName,
            new PrintStream(logCaptureStream), logLevel);
        return new Context("caller-method-logger", ClientLoggerAccessHelper.createClientLogger(logger, null));
    }

    private static String convertOutputStreamToString(AccessibleByteArrayOutputStream stream) {
        return stream.toString(StandardCharsets.UTF_8);
    }

    public static class HttpLogMessage {
        private static final ObjectMapper SERIALIZER
            = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        private static final Integer MAGIC_NUMBER = 42;

        @JsonProperty("az.sdk.message")
        private String message;

        @JsonProperty("method")
        private String method;

        @JsonProperty("url")
        private String url;

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
            return new HttpLogMessage().setMessage("HTTP request")
                .setMethod(method.toString())
                .setUrl(url)
                .setBody(body != null ? new String(body, StandardCharsets.UTF_8) : null);
        }

        private static HttpLogMessage response(String url, byte[] body, Integer statusCode) {
            return new HttpLogMessage().setMessage("HTTP response")
                .setUrl(url)
                .setStatusCode(statusCode)
                .setDurationMs(MAGIC_NUMBER)
                .setBody(body != null ? new String(body, StandardCharsets.UTF_8) : null);
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

        void assertEqual(HttpLogMessage other, HttpLogOptions logOptions, LogLevel logLevel) {
            assertEquals(this.message, other.message);
            assertEquals(this.method, other.method);
            assertEquals(this.url, other.url);
            assertEquals(this.tryCount, other.tryCount);
            assertEquals(this.statusCode, other.statusCode);
            if (this.durationMs != null) {
                assertNotNull(other.durationMs);
            }

            if (logOptions.getLogLevel().shouldLogBody()) {
                assertEquals(this.body, other.body);
            }

            if (logOptions.getLogLevel().shouldLogHeaders() && LogLevel.INFORMATIONAL.compareTo(logLevel) >= 0) {
                int expectedHeaders = 0;
                boolean expectRedactedHeaders = false;
                for (Map.Entry<String, String> kvp : this.headers.entrySet()) {
                    boolean isAllowed = logOptions.getAllowedHeaderNames().contains(kvp.getKey());
                    if (isAllowed) {
                        expectedHeaders++;
                        assertEquals(kvp.getValue(), other.headers.get(kvp.getKey().toLowerCase(Locale.ROOT)));
                    } else if (!logOptions.isRedactedHeaderLoggingDisabled()) {
                        expectRedactedHeaders = true;
                        assertTrue(other.headers.get("redactedHeaders").contains(kvp.getKey()));
                    }
                }

                assertEquals(expectedHeaders + (expectRedactedHeaders ? 1 : 0), other.headers.size());
            }
        }
    }
}
