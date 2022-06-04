// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.AfterEach;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains tests for {@link HttpLoggingPolicy}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
@ResourceLock(Resources.SYSTEM_OUT)
public class HttpLoggingPolicySyncTests {
    private static final String REDACTED = "REDACTED";
    private static final Context CONTEXT = new Context("caller-method", HttpLoggingPolicySyncTests.class.getName());

    private String originalLogLevel;
    private PrintStream originalSystemOut;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void prepareForTest() {
        // Set the log level to information for the test.
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());

        /*
         * DefaultLogger uses System.out to log. Inject a custom PrintStream to log into for the duration of the test to
         * capture the log messages.
         */
        originalSystemOut = System.out;
        logCaptureStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void cleanupAfterTest() {
        // Reset or clear the log level after the test completes.
        setPropertyToOriginalOrClear(originalLogLevel);

        // Reset System.err to the original PrintStream.
        System.setOut(originalSystemOut);
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
    public void validateLoggingDoesNotConsumeRequest(BinaryData requestContent, byte[] data, int contentLength)
        throws MalformedURLException {
        URL requestUrl = new URL("https://test.com");
        HttpHeaders requestHeaders = new HttpHeaders()
            .set("Content-Type", ContentType.APPLICATION_JSON)
            .set("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArrayEquals(data, bytes))
                .then(Mono.empty()))
            .build();

        pipeline.sendSync(new HttpRequest(HttpMethod.POST, requestUrl, requestHeaders, requestContent),
                CONTEXT);

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeResponse(BinaryData responseContent, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com");
        HttpHeaders responseHeaders = new HttpHeaders()
            .set("Content-Type", ContentType.APPLICATION_JSON)
            .set("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> Mono.just(new MockHttpResponse(ignored, responseHeaders, responseContent)))
            .build();

        HttpResponse response = pipeline.sendSync(request, CONTEXT);

        assertArrayEquals(data, response.getBodyAsBinaryData().toBytes());
        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    private static Stream<Arguments> validateLoggingDoesNotConsumeSupplier() {
        byte[] data = "this is a test".getBytes(StandardCharsets.UTF_8);

        return Stream.of(
            Arguments.of(BinaryData.fromBytes(data), data, data.length),

            Arguments.of(BinaryData.fromStream(new ByteArrayInputStream(data)), data, data.length)
        );
    }

    private static class MockHttpResponse extends HttpResponse {
        private final HttpHeaders headers;
        private final BinaryData body;

        MockHttpResponse(HttpRequest request, HttpHeaders headers, BinaryData body) {
            super(request);
            this.headers = headers;
            this.body = body;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return body.toFluxByteBuffer();
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            return body;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body.toFluxByteBuffer());
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
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy(), new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(logLevel)))
            .httpClient(ignored -> (requestCount.getAndIncrement() == 0)
                ? Mono.error(new RuntimeException("Try again!"))
                : Mono.just(new com.azure.core.http.MockHttpResponse(ignored, 200)))
            .build();

        HttpResponse response = pipeline.sendSync(request, CONTEXT);

        assertEquals(200, response.getStatusCode());

        String logString = convertOutputStreamToString(logCaptureStream);
        assertTrue(logString.contains("Try count: 1"));
        assertTrue(logString.contains("Try count: 2"));
    }

    private void setupLogLevel(int logLevelToSet) {
        originalLogLevel = Configuration.getGlobalConfiguration().get(PROPERTY_AZURE_LOG_LEVEL);
        EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(logLevelToSet));
    }

    private void setPropertyToOriginalOrClear(String originalValue) {
        if (CoreUtils.isNullOrEmpty(originalValue)) {
            EnvironmentConfiguration.getGlobalConfiguration().remove(PROPERTY_AZURE_LOG_LEVEL);
        } else {
            EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, originalValue);
        }
    }

    private static String convertOutputStreamToString(ByteArrayOutputStream stream) {
        try {
            return stream.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
