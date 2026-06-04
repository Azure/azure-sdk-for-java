// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InferenceServiceTest {

    private static final String TEST_ENDPOINT = "https://test-inference.westus.dbinference.azure.com";
    // Total calls expected for a fully-retried retryable failure: 1 initial + RETRY_MAX_ATTEMPTS retries
    private static final int TOTAL_CALLS_AFTER_EXHAUSTION = 1 + InferenceService.RETRY_MAX_ATTEMPTS;

    private com.azure.core.http.HttpClient mockAzureCoreHttpClient;
    private HttpPipeline httpPipeline;

    @BeforeMethod(groups = {"unit"})
    public void setUp() {
        mockAzureCoreHttpClient = mock(com.azure.core.http.HttpClient.class);
        httpPipeline = new HttpPipelineBuilder()
            .httpClient(mockAzureCoreHttpClient)
            .build();
    }

    // -------------------------------------------------------------------------
    // Constructor / validation tests
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void constructorShouldThrowWhenEndpointIsNull() {
        assertThatThrownBy(() -> new InferenceService(null, httpPipeline))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("endpoint");
    }

    @Test(groups = {"unit"})
    public void constructorShouldThrowWhenPipelineIsNull() {
        assertThatThrownBy(() -> new InferenceService(URI.create(TEST_ENDPOINT), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("httpPipeline");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenRerankContextIsNull() {
        InferenceService service = createService();

        assertThatThrownBy(() -> service.semanticRerank(null, Arrays.asList("doc1", "doc2"), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Rerank context cannot be null");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenDocumentsIsNull() {
        InferenceService service = createService();

        assertThatThrownBy(() -> service.semanticRerank("query", null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Documents list cannot be null");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenRerankContextIsEmpty() {
        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("   ", Arrays.asList("doc1", "doc2"), null))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && error.getMessage().contains("Rerank context cannot be empty"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenDocumentsListIsEmpty() {
        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("query", Collections.emptyList(), null))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && error.getMessage().contains("Documents list cannot be empty"))
            .verify();
    }

    // -------------------------------------------------------------------------
    // Happy-path tests
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void semanticRerankShouldSucceedWithValidResponse() {
        String responseBody = createSuccessResponseBody();
        MockHttpResponse mockResponse = new MockHttpResponse(null, 200, responseBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        List<String> documents = Arrays.asList(
            "This is document 1",
            "This is document 2",
            "This is document 3"
        );

        StepVerifier.create(service.semanticRerank("search query", documents, null))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.getScores()).isNotNull();
                assertThat(result.getScores()).hasSize(3);
                assertThat(result.getScores().get(0).getIndex()).isEqualTo(0);
                assertThat(result.getScores().get(0).getScore()).isEqualTo(0.95);
                assertThat(result.getScores().get(0).getDocument()).isEqualTo("This is document 1");
                assertThat(result.getLatency()).isNotNull();
                assertThat(result.getLatency().get("inference_time")).isEqualTo(0.5);
                assertThat(result.getTokenUsage()).isNotNull();
                assertThat(result.getTokenUsage().get("total_tokens")).isEqualTo(100);
            })
            .verifyComplete();

        verify(mockAzureCoreHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldIncludeOptionsInRequest() {
        String responseBody = createSuccessResponseBody();
        MockHttpResponse mockResponse = new MockHttpResponse(null, 200, responseBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockAzureCoreHttpClient.send(requestCaptor.capture(), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        List<String> documents = Arrays.asList("doc1", "doc2");
        Map<String, Object> options = new HashMap<>();
        options.put("return_documents", true);
        options.put("top_k", 5);
        options.put("batch_size", 16);
        options.put("sort", true);

        StepVerifier.create(service.semanticRerank("query", documents, options))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();

        String requestBody = capturedRequest.getBodyAsBinaryData().toString();
        assertThat(requestBody).contains("\"return_documents\":true");
        assertThat(requestBody).contains("\"top_k\":5");
        assertThat(requestBody).contains("\"batch_size\":16");
        assertThat(requestBody).contains("\"sort\":true");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldNotForwardTimeoutSecondsToPayload() {
        String responseBody = createSuccessResponseBody();
        MockHttpResponse mockResponse = new MockHttpResponse(null, 200, responseBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockAzureCoreHttpClient.send(requestCaptor.capture(), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        Map<String, Object> options = new HashMap<>();
        options.put("top_k", 3);
        options.put(InferenceService.OPTION_TIMEOUT_SECONDS, 30);

        StepVerifier.create(service.semanticRerank("query", Collections.singletonList("doc1"), options))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        HttpRequest capturedRequest = requestCaptor.getValue();
        String requestBody = capturedRequest.getBodyAsBinaryData().toString();
        assertThat(requestBody).doesNotContain(InferenceService.OPTION_TIMEOUT_SECONDS);
    }

    // -------------------------------------------------------------------------
    // Non-retryable error tests — expect exactly 1 HTTP call, immediate failure
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandle403Forbidden() {
        String errorBody = "{\"error\": \"Forbidden - insufficient permissions\"}";
        MockHttpResponse mockResponse = new MockHttpResponse(null, 403, errorBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error instanceof HttpResponseException
                && error.getMessage().contains("Semantic rerank request failed")
                && error.getMessage().contains("Forbidden"))
            .verify();

        verify(mockAzureCoreHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleMalformedResponse() {
        String malformedBody = "{ invalid json }";
        MockHttpResponse mockResponse = new MockHttpResponse(null, 200, malformedBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error ->
                error instanceof IllegalStateException
                    && error.getMessage().contains("Failed to parse semantic rerank response"))
            .verify();

        verify(mockAzureCoreHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleEmptyScoresResponse() {
        String responseBody = "{\"Scores\": [], \"latency\": {}, \"token_usage\": {}}";
        MockHttpResponse mockResponse = new MockHttpResponse(null, 200, responseBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.getScores()).isEmpty();
            })
            .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Retry behaviour tests — use virtual time to skip backoff delays
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void semanticRerankShouldRetryOn429AndSucceedOnSecondAttempt() {
        MockHttpResponse throttledResponse = new MockHttpResponse(null, 429, "{\"error\": \"Too Many Requests\"}");
        MockHttpResponse successResponse = new MockHttpResponse(null, 200, createSuccessResponseBody());

        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(throttledResponse))
            .thenReturn(Mono.just(successResponse));

        final InferenceService service = createService();

        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        verify(mockAzureCoreHttpClient, times(2)).send(any(HttpRequest.class), any());
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldRetryOn500AndSucceedOnThirdAttempt() {
        MockHttpResponse errorResponse = new MockHttpResponse(null, 500, "{\"error\": \"Internal Server Error\"}");
        MockHttpResponse successResponse = new MockHttpResponse(null, 200, createSuccessResponseBody());

        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(errorResponse))
            .thenReturn(Mono.just(errorResponse))
            .thenReturn(Mono.just(successResponse));

        final InferenceService service = createService();
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        verify(mockAzureCoreHttpClient, times(3)).send(any(HttpRequest.class), any());
    }

    @DataProvider(name = "nonRetryableStatusCodes")
    public Object[][] nonRetryableStatusCodeProvider() {
        return new Object[][] {
            {400, "Bad Request"},
            {401, "Unauthorized"},
            {403, "Forbidden"},
            {404, "Not Found"},
        };
    }

    @Test(groups = {"unit"}, dataProvider = "nonRetryableStatusCodes")
    public void semanticRerankShouldNotRetryNonRetryableStatusCodes(int statusCode, String errorMessage) {
        String errorBody = String.format("{\"error\": \"%s\"}", errorMessage);
        MockHttpResponse mockResponse = new MockHttpResponse(null, statusCode, errorBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = createService();

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Semantic rerank request failed"))
            .verify();

        verify(mockAzureCoreHttpClient, times(1)).send(any(HttpRequest.class), any());
    }

    @DataProvider(name = "retryableStatusCodes")
    public Object[][] retryableStatusCodeProvider() {
        return new Object[][] {
            {429, "Too Many Requests"},
            {500, "Internal Server Error"},
            {502, "Bad Gateway"},
            {503, "Service Unavailable"},
        };
    }

    @Test(groups = {"unit"}, dataProvider = "retryableStatusCodes")
    public void semanticRerankShouldRetryRetryableStatusCodesAndExhaust(int statusCode, String errorMessage) {
        String errorBody = String.format("{\"error\": \"%s\"}", errorMessage);
        MockHttpResponse mockResponse = new MockHttpResponse(null, statusCode, errorBody);
        when(mockAzureCoreHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn(Mono.just(mockResponse));

        final InferenceService service = createService();
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        verify(mockAzureCoreHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any());
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private InferenceService createService() {
        return new InferenceService(URI.create(TEST_ENDPOINT), httpPipeline);
    }

    private boolean isRetryExhaustedWithMessage(Throwable error, String messageSubstring) {
        if (!Exceptions.isRetryExhausted(error)) {
            return error.getMessage() != null && error.getMessage().contains(messageSubstring);
        }
        Throwable cause = error.getCause();
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains(messageSubstring)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String createSuccessResponseBody() {
        return "{"
            + "\"Scores\": ["
            + "  {\"index\": 0, \"score\": 0.95, \"document\": \"This is document 1\"},"
            + "  {\"index\": 1, \"score\": 0.85, \"document\": \"This is document 2\"},"
            + "  {\"index\": 2, \"score\": 0.75, \"document\": \"This is document 3\"}"
            + "],"
            + "\"latency\": {"
            + "  \"data_preprocess_time\": 0.1,"
            + "  \"inference_time\": 0.5,"
            + "  \"postprocess_time\": 0.05"
            + "},"
            + "\"token_usage\": {"
            + "  \"total_tokens\": 100"
            + "}"
            + "}";
    }

    /**
     * Simple mock implementation of azure-core HttpResponse for testing.
     */
    private static class MockHttpResponse extends HttpResponse {
        private final int statusCode;
        private final String body;
        private final HttpHeaders headers;

        MockHttpResponse(HttpRequest request, int statusCode, String body) {
            super(request);
            this.statusCode = statusCode;
            this.body = body;
            this.headers = new HttpHeaders();
        }

        MockHttpResponse(HttpRequest request, int statusCode, String body, HttpHeaders headers) {
            super(request);
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return headers.getValue(HttpHeaderName.fromString(name));
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
        public reactor.core.publisher.Flux<ByteBuffer> getBody() {
            return reactor.core.publisher.Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(body);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(body.getBytes(StandardCharsets.UTF_8), charset));
        }
    }
}
