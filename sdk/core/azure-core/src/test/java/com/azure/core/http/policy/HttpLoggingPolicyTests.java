// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Contains tests for {@link HttpLoggingPolicy}.
 */
public class HttpLoggingPolicyTests {
    // Context used to tell the HttpLoggingPolicy to use a ClientLogger named HttpLoggingPolicyTests.
    private Context context = new Context("caller-method", "HttpLoggingPolicyTests");
    private String testUrl = "https://aurl.com";

    private PrintStream originalSystemErr;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void prepareLogLocation() {
        /*
         * The default configuration for SLF4J's SimpleLogger uses System.err to log. Inject a custom PrintStream to
         * log into for the duration of the test to capture the log messages.
         */
        originalSystemErr = System.err;
        logCaptureStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void revertLogLocation() {
        System.setErr(originalSystemErr);
    }

    /**
     * Tests that the logging policy performs a no-op if no {@link HttpLogOptions} were set.
     */
    @Test
    public void noHttpLogOptions() {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, testUrl);
        HttpResponse httpResponse = new MockHttpResponse(httpRequest, 200);

        HttpPipeline testPipeline = prepareTestPipeline(null, httpResponse, false);

        StepVerifier.create(testPipeline.send(httpRequest, context))
            .assertNext(response -> {
                assertEquals(httpResponse.getStatusCode(), response.getStatusCode());
                assertFalse(convertLogStream().contains("HttpLoggingPolicyTests"));
            })
            .verifyComplete();
    }

    /**
     * Tests that the logging policy doesn't log when using {@link HttpLogDetailLevel#NONE}.
     */
    @Test
    public void httpLogDetailLevelNone() {

    }

    /**
     * Tests that the logging policy only logs the URL, HTTP method, query parameters, and body size when using {@link
     * HttpLogDetailLevel#BASIC}.
     */
    @Test
    public void httpLogDetailLevelBasic() {

    }

    /**
     * Tests that the logging policy logs basic information plus {@link HttpHeaders} when using {@link
     * HttpLogDetailLevel#HEADERS}.
     */
    @Test
    public void httpLogDetailLevelHeaders() {

    }

    /**
     * Tests that the logging policy logs basic information plus the request and response bodies when using {@link
     * HttpLogDetailLevel#BODY}.
     */
    @Test
    public void httpLogDetailLevelBody() {

    }

    /**
     * Tests that the logging policy logs basic information plus headers and request and response bodies when using
     * {@link HttpLogDetailLevel#BODY_AND_HEADERS}.
     */
    @Test
    public void httpLogDetailLevelBodyAndHeaders() {

    }

    /**
     * Tests that the logging policy is able to log all headers that match a wild card.
     */
    @Test
    public void logWildCardHeaders() {

    }

    private HttpPipeline prepareTestPipeline(HttpLogOptions logOptions, HttpResponse response, boolean treatAsError) {
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
            return treatAsError ? Mono.error(new RuntimeException("Just an error")) : Mono.just(response);
        }
    }

    private String convertLogStream() {
        return new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
