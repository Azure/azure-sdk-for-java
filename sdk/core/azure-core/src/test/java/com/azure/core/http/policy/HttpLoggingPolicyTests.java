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
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * This class contains tests for {@link HttpLoggingPolicy}.
 */
public class HttpLoggingPolicyTests {
    private static final String REDACTED = "REDACTED";
    private static final Context CONTEXT = new Context("caller-method", "HttpLoggingPolicyTests");

    private String originalLogLevel;
    private PrintStream originalErr;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void prepareForTest() {
        // Set the log level to information for the test.
        originalLogLevel = System.getProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, "2");

        /*
         * Indicate to SLF4J to enable trace level logging for a logger named
         * com.azure.core.util.logging.ClientLoggerTests. Trace is the maximum level of logging supported by the
         * ClientLogger.
         */
        System.setProperty("org.slf4j.simpleLogger.log.com.azure.core.util.logging.HttpLoggingPolicyTests", "trace");

        // Override System.err as that is where SLF4J will log by default.
        originalErr = System.err;
        logCaptureStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void cleanupAfterTest() {
        // Reset or clear the log level after the test completes.
        if (CoreUtils.isNullOrEmpty(originalLogLevel)) {
            System.clearProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        } else {
            System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);
        }

        System.clearProperty("org.slf4j.simpleLogger.log.com.azure.core.util.logging.HttpLoggingPolicyTests");

        // Reset System.err to the original PrintStream.
        System.setErr(originalErr);
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

        String logString = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        Assertions.assertTrue(logString.contains(expectedQueryString));
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
        URL requestUrl = new URL("https://test.com");
        HttpHeaders requestHeaders = new HttpHeaders()
            .put("Content-Type", ContentType.APPLICATION_JSON)
            .put("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(request -> FluxUtil.collectBytesInByteBufferStream(request.getBody())
                .doOnSuccess(bytes -> assertArrayEquals(data, bytes))
                .then(Mono.empty()))
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl, requestHeaders, stream),
            CONTEXT))
            .verifyComplete();

        String logString = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(logString);
        Assertions.assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
    }

    /**
     * Tests that logging the response body doesn't consume the stream before it is returned from the service call.
     */
    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateLoggingDoesNotConsumeSupplier")
    public void validateLoggingDoesNotConsumeResponse(Flux<ByteBuffer> stream, byte[] data, int contentLength) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.com");
        HttpHeaders responseHeaders = new HttpHeaders()
            .put("Content-Type", ContentType.APPLICATION_JSON)
            .put("Content-Length", Integer.toString(contentLength));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY)))
            .httpClient(ignored -> Mono.just(new MockHttpResponse(ignored, responseHeaders, stream)))
            .build();

        StepVerifier.create(pipeline.send(request, CONTEXT))
            .assertNext(response -> StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(response.getBody()))
                .assertNext(bytes -> assertArrayEquals(data, bytes))
                .verifyComplete())
            .verifyComplete();

        String logString = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(logString);
        Assertions.assertTrue(logString.contains(new String(data, StandardCharsets.UTF_8)));
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
            Arguments.of(Flux.fromArray(new ByteBuffer[]{
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

        MockHttpResponse(HttpRequest request, HttpHeaders headers, Flux<ByteBuffer> body) {
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
            return body;
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return FluxUtil.collectBytesInByteBufferStream(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsByteArray().map(String::new);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return getBodyAsByteArray().map(bytes -> new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
