// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contains tests for {@link HttpLoggingPolicy}.
 */
public class HttpLoggingPolicyTests {
    private static final String REDACTED = "REDACTED";

    // Context used to tell the HttpLoggingPolicy to use a ClientLogger named HttpLoggingPolicyTests.
    private Context context = new Context("caller-method", "HttpLoggingPolicyTests");

    private HttpMethod httpMethod = HttpMethod.PUT;
    private URL url = new URL("https://aurl.com");

    private String requestBodyAsString = "Request body";
    private byte[] requestBodyAsByteArray = requestBodyAsString.getBytes(StandardCharsets.UTF_8);
    private Flux<ByteBuffer> requestBody = Flux.just(ByteBuffer.wrap(requestBodyAsByteArray));

    private HttpHeader allowedRequestHeader = new HttpHeader("User-Agent", "anAllowedHeader");
    private HttpHeader disallowedHeader = new HttpHeader("anotherHeader", "aDisallowedHeader");
    private String wildCardHeaderBase = "x-ms-meta-";
    private HttpHeader wildCardHeader1 = new HttpHeader(wildCardHeaderBase + "metadata1", "metadataValue1");
    private HttpHeader wildCardHeader2 = new HttpHeader(wildCardHeaderBase + "metadata2", "metadataValue2");
    private HttpHeaders requestHeaders = new HttpHeaders(Arrays.asList(allowedRequestHeader, disallowedHeader,
        wildCardHeader1, wildCardHeader2,
        new HttpHeader("Content-Length", Integer.toString(requestBodyAsByteArray.length))));

    private String responseBodyAsString = "Response body";
    private byte[] responseBody = responseBodyAsString.getBytes(StandardCharsets.UTF_8);

    private HttpHeader allowedResponseHeader = new HttpHeader("Content-Type", "text");
    private HttpHeaders responseHeaders = new HttpHeaders(Arrays.asList(allowedResponseHeader,
        new HttpHeader("Content-Length", Integer.toString(responseBody.length))));

    private PrintStream originalSystemErr;
    private ByteArrayOutputStream logCaptureStream;
    private String originalLogLevel;

    public HttpLoggingPolicyTests() throws MalformedURLException {
    }

    @BeforeEach
    public void prepareForTest() {
        /*
         * The default configuration for SLF4J's SimpleLogger uses System.err to log. Inject a custom PrintStream to
         * log into for the duration of the test to capture the log messages.
         */
        originalSystemErr = System.err;
        logCaptureStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(logCaptureStream));

        /*
         * Change the logging level to verbose to ensure all messages are logged for testing purposes.
         */
        originalLogLevel = System.getProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, "1");

        /*
         * Indicate to SLF4J to enable trace level logging for a logger named HttpLoggingPolicyTests.
         * Trace is the maximum level of logging supported by the ClientLogger.
         */
        System.setProperty("org.slf4j.simpleLogger.log.HttpLoggingPolicyTests", "trace");
    }

    @AfterEach
    public void cleanupAfterTest() {
        System.setErr(originalSystemErr);
        System.clearProperty("org.slf4j.simpleLogger.log.HttpLoggingPolicyTests");

        // Reset or clear the log level after the test completes.
        if (CoreUtils.isNullOrEmpty(originalLogLevel)) {
            System.clearProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL);
        } else {
            System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);
        }
    }

    /**
     * Tests that the policy doesn't log anything when {@link HttpLogOptions} is {@code null} or when using {@link
     * HttpLogDetailLevel#NONE}.
     *
     * @param logOptions Logging options.
     */
    @ParameterizedTest
    @MethodSource("noLoggingArguments")
    public void noLogging(HttpLogOptions logOptions) {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        StepVerifier.create(createPipeline(logOptions, httpResponse, false).send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                String logOutput = convertLogStream();
                assertFalse(logOutput.contains(url.toString()));
                assertFalse(logOutput.contains(httpMethod.toString()));
                assertFalse(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertFalse(logOutput.contains(requestBodyAsString));
                assertFalse(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
    }

    /**
     * Tests that the policy doesn't log anything when {@link HttpLogOptions} is {@code null} or when using {@link
     * HttpLogDetailLevel#NONE} and an exception occurs.
     *
     * @param logOptions Logging options.
     */
    @ParameterizedTest
    @MethodSource("noLoggingArguments")
    public void noLoggingWhenException(HttpLogOptions logOptions) {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 400, responseHeaders, responseBody);

        StepVerifier.create(createPipeline(logOptions, httpResponse, true).send(httpRequest, context))
            .verifyErrorSatisfies(exception -> {
                assertTrue(exception instanceof HttpResponseException);
                assertEquals(httpResponse.getStatusCode(),
                    ((HttpResponseException) exception).getResponse().getStatusCode());
                String logOutput = convertLogStream();
                assertFalse(logOutput.contains(url.toString()));
                assertFalse(logOutput.contains(httpMethod.toString()));
                assertFalse(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertFalse(logOutput.contains(requestBodyAsString));
                assertFalse(logOutput.contains(responseBodyAsString));
            });
    }

    private static Stream<Arguments> noLoggingArguments() {
        return Stream.of(
            Arguments.of((HttpLogOptions) null),
            Arguments.of(new HttpLogOptions())
        );
    }

    /**
     * Tests that the logging policy only logs the URL, HTTP method, query parameters, and body size when using {@link
     * HttpLogDetailLevel#BASIC}.
     */
    @Test
    public void httpLogDetailLevelBasic() {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                String logOutput = convertLogStream();
                assertTrue(logOutput.contains(url.toString()));
                assertTrue(logOutput.contains(httpMethod.toString()));
                assertFalse(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertFalse(logOutput.contains(requestBodyAsString));
                assertFalse(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
    }

    /**
     * Tests that the logging policy logs basic information plus {@link HttpHeaders} when using {@link
     * HttpLogDetailLevel#HEADERS}.
     */
    @Test
    public void httpLogDetailLevelHeaders() {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                String logOutput = convertLogStream();
                assertTrue(logOutput.contains(url.toString()));
                assertTrue(logOutput.contains(httpMethod.toString()));
                assertTrue(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertFalse(logOutput.contains(requestBodyAsString));
                assertFalse(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
    }

    /**
     * Tests that the logging policy logs basic information plus the request and response bodies when using {@link
     * HttpLogDetailLevel#BODY}.
     */
    @Test
    public void httpLogDetailLevelBody() {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                String logOutput = convertLogStream();
                assertTrue(logOutput.contains(url.toString()));
                assertTrue(logOutput.contains(httpMethod.toString()));
                assertFalse(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertTrue(logOutput.contains(requestBodyAsString));
                assertTrue(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
    }

    /**
     * Tests that the logging policy logs basic information plus headers and request and response bodies when using
     * {@link HttpLogDetailLevel#BODY_AND_HEADERS}.
     */
    @Test
    public void httpLogDetailLevelBodyAndHeaders() {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                String logOutput = convertLogStream();
                assertTrue(logOutput.contains(url.toString()));
                assertTrue(logOutput.contains(httpMethod.toString()));
                assertTrue(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertTrue(logOutput.contains(requestBodyAsString));
                assertTrue(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
    }

    /**
     * Tests that the logging policy is able to log all headers that match a wild card.
     */
    @Test
    public void logWildCardHeaders() {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders, requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200, responseHeaders, responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.HEADERS)
                .addAllowedHeaderPattern(wildCardHeaderBase + "*")
                .addAllowedHeaderPattern("anotherPattern+"),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                String logOutput = convertLogStream();
                assertTrue(logOutput.contains(allowedRequestHeader.toString()));
                assertFalse(logOutput.contains(disallowedHeader.toString()));
                assertTrue(logOutput.contains(wildCardHeader1.toString()));
                assertTrue(logOutput.contains(wildCardHeader2.toString()));
            })
            .verifyComplete();
    }

    /**
     * Tests that when the request or response body is empty or considered large they don't get logger.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 16 * 1024 + 1, 32 * 1024})
    public void bodyDoesNotGetLogged(int bodySize) {
        HttpRequest httpRequest = new HttpRequest(httpMethod, url, requestHeaders
            .put("Content-Length", Integer.toString(bodySize)), requestBody);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200,
            requestHeaders.put("Content-Length", Integer.toString(bodySize)), responseBody);

        HttpPipeline testPipeline = createPipeline(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY),
            httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                String logOutput = convertLogStream();
                assertFalse(logOutput.contains(requestBodyAsString));
                assertFalse(logOutput.contains(responseBodyAsString));
            })
            .verifyComplete();
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

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.POST, requestUrl)))
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

    private HttpPipeline createPipeline(HttpLogOptions logOptions, HttpResponse response, boolean treatAsError) {
        return new HttpPipelineBuilder()
            .policies(new HttpLoggingPolicy(logOptions))
            .httpClient(new MockableHttpClient(response, treatAsError))
            .build();
    }

    private static class MockableHttpClient implements HttpClient {
        private final HttpResponse response;
        private final boolean treatAsError;

        MockableHttpClient(HttpResponse response, boolean treatAsError) {
            this.response = response;
            this.treatAsError = treatAsError;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return treatAsError
                ? Mono.error(new HttpResponseException("Just an error", response))
                : Mono.just(response);
        }
    }

    private String convertLogStream() {
        return new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
