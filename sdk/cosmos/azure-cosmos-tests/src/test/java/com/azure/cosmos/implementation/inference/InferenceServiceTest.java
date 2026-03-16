// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.inference;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InferenceServiceTest {

    private static final String MOCK_TOKEN = "mock-access-token";
    private static final String TEST_ENDPOINT = "https://test-inference.westus.dbinference.azure.com";
    private static final String INFERENCE_ENDPOINT_PROPERTY = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT";

    // Total calls expected for a fully-retried retryable failure: 1 initial + RETRY_MAX_ATTEMPTS retries
    private static final int TOTAL_CALLS_AFTER_EXHAUSTION = 1 + InferenceService.RETRY_MAX_ATTEMPTS;

    private TokenCredential mockTokenCredential;
    private HttpClient mockHttpClient;
    private MockedStatic<HttpClient> httpClientStaticMock;

    @BeforeMethod
    public void setUp() {
        // Set the inference endpoint system property required by InferenceService constructor
        System.setProperty(INFERENCE_ENDPOINT_PROPERTY, TEST_ENDPOINT);

        mockTokenCredential = mock(TokenCredential.class);
        mockHttpClient = mock(HttpClient.class);

        // Mock token credential to return a valid token
        AccessToken accessToken = new AccessToken(MOCK_TOKEN, OffsetDateTime.now().plusHours(1));
        when(mockTokenCredential.getToken(any(TokenRequestContext.class)))
            .thenReturn(Mono.just(accessToken));

        // Mock the static HttpClient.createFixed method
        httpClientStaticMock = Mockito.mockStatic(HttpClient.class);
        httpClientStaticMock.when(() -> HttpClient.createFixed(any(HttpClientConfig.class)))
            .thenReturn(mockHttpClient);
    }

    @AfterMethod
    public void tearDown() {
        System.clearProperty(INFERENCE_ENDPOINT_PROPERTY);
        if (httpClientStaticMock != null) {
            httpClientStaticMock.close();
        }
    }

    // -------------------------------------------------------------------------
    // Constructor / validation tests (unchanged by retry logic)
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void constructorShouldThrowWhenTokenCredentialIsNull() {
        assertThatThrownBy(() -> new InferenceService(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Semantic reranking requires AAD authentication")
            .hasMessageContaining("key-based auth");
    }

    @Test(groups = {"unit"})
    public void constructorShouldThrowWhenEndpointNotConfigured() {
        System.clearProperty(INFERENCE_ENDPOINT_PROPERTY);

        assertThatThrownBy(() -> new InferenceService(mockTokenCredential))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must be set to use semantic reranking");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenRerankContextIsNull() {
        InferenceService service = new InferenceService(mockTokenCredential);

        assertThatThrownBy(() -> service.semanticRerank(null, Arrays.asList("doc1", "doc2"), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Rerank context cannot be null");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenDocumentsIsNull() {
        InferenceService service = new InferenceService(mockTokenCredential);

        assertThatThrownBy(() -> service.semanticRerank("query", null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Documents list cannot be null");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenRerankContextIsEmpty() {
        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("   ", Arrays.asList("doc1", "doc2"), null))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && error.getMessage().contains("Rerank context cannot be empty"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldThrowWhenDocumentsListIsEmpty() {
        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Collections.emptyList(), null))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && error.getMessage().contains("Documents list cannot be empty"))
            .verify();
    }

    // -------------------------------------------------------------------------
    // Happy-path tests (unchanged by retry logic)
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void semanticRerankShouldSucceedWithValidResponse() {
        // Create a mock response
        String responseBody = createSuccessResponseBody();
        HttpResponse mockResponse = createMockResponse(200, responseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

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

        // Success on first attempt — exactly 1 call
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldIncludeOptionsInRequest() {
        String responseBody = createSuccessResponseBody();
        HttpResponse mockResponse = createMockResponse(200, responseBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(requestCaptor.capture(), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        List<String> documents = Arrays.asList("doc1", "doc2");
        Map<String, Object> options = new HashMap<>();
        options.put("return_documents", true);
        options.put("top_k", 5);
        options.put("batch_size", 16);
        options.put("sort", true);

        StepVerifier.create(service.semanticRerank("query", documents, options))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        // Verify request body contains options
        HttpRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest).isNotNull();

        // Collect the Flux<byte[]> body into a single string
        String requestBody = capturedRequest.body()
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            .collectList()
            .map(list -> String.join("", list))
            .block();

        assertThat(requestBody).contains("\"return_documents\":true");
        assertThat(requestBody).contains("\"top_k\":5");
        assertThat(requestBody).contains("\"batch_size\":16");
        assertThat(requestBody).contains("\"sort\":true");
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldNotForwardTimeoutSecondsToPayload() {
        String responseBody = createSuccessResponseBody();
        HttpResponse mockResponse = createMockResponse(200, responseBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(requestCaptor.capture(), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        Map<String, Object> options = new HashMap<>();
        options.put("top_k", 3);
        options.put(InferenceService.OPTION_TIMEOUT_SECONDS, 30); // SDK-local, must NOT reach the endpoint

        StepVerifier.create(service.semanticRerank("query", Collections.singletonList("doc1"), options))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        // Verify that the request body does not contain the timeout seconds
        HttpRequest capturedRequest = requestCaptor.getValue();
        String requestBody = capturedRequest.body()
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            .collectList()
            .map(list -> String.join("", list))
            .block();

        assertThat(requestBody).doesNotContain(InferenceService.OPTION_TIMEOUT_SECONDS);
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldIncludeAuthorizationHeader() {
        String responseBody = createSuccessResponseBody();
        HttpResponse mockResponse = createMockResponse(200, responseBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(requestCaptor.capture(), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.headers().value("Authorization")).isEqualTo("Bearer " + MOCK_TOKEN);
    }

    // -------------------------------------------------------------------------
    // Non-retryable error tests — expect exactly 1 HTTP call, immediate failure
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandle403Forbidden() {
        // 403 is non-retryable — should fail immediately with 1 call
        String errorBody = "{\"error\": \"Forbidden - insufficient permissions\"}";
        HttpResponse mockResponse = createMockResponse(403, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Semantic rerank request failed")
                && error.getMessage().contains("Forbidden"))
            .verify();

        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleTokenAcquisitionFailure() {
        // Token errors are not CosmosException — should not be retried
        when(mockTokenCredential.getToken(any(TokenRequestContext.class)))
            .thenReturn(Mono.error(new RuntimeException("Token acquisition failed")));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error instanceof RuntimeException
                && error.getMessage().contains("Token acquisition failed"))
            .verify();

        // No HTTP call should be made if token acquisition fails
        verify(mockHttpClient, never()).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldNotRetryNetworkErrors() {
        // Raw RuntimeException (not CosmosException) — not retryable
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.error(new RuntimeException("Network connection failed")));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error instanceof RuntimeException
                && error.getMessage().contains("Network connection failed"))
            .verify();

        // Non-CosmosException errors bypass the retry filter — exactly 1 call
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleMalformedResponse() {
        // Parse failure uses BadRequestException (400) + CUSTOM_SERIALIZER_EXCEPTION sub-status,
        // matching CosmosItemSerializer's pattern — not retryable, fails immediately with 1 call.
        String malformedBody = "{ invalid json }";
        HttpResponse mockResponse = createMockResponse(200, malformedBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error ->
                error instanceof BadRequestException
                    && error.getMessage().contains("Failed to parse semantic rerank response")
                    && ((BadRequestException) error).getStatusCode() == HttpConstants.StatusCodes.BADREQUEST
                    && ((BadRequestException) error).getSubStatusCode()
                        == HttpConstants.SubStatusCodes.CUSTOM_SERIALIZER_EXCEPTION)
            .verify();

        // Not retryable — exactly 1 call
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleEmptyScoresResponse() {
        String responseBody = "{\"Scores\": [], \"latency\": {}, \"token_usage\": {}}";
        HttpResponse mockResponse = createMockResponse(200, responseBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

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
        // First call returns 429, second returns 200
        HttpResponse throttledResponse = createMockResponse(429, "{\"error\": \"Too Many Requests\"}");
        HttpResponse successResponse = createMockResponse(200, createSuccessResponseBody());

        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(throttledResponse))
            .thenReturn(Mono.just(successResponse));

        // Inject VirtualTimeScheduler so Retry.backoff delays run under virtual time.
        // The Mono is returned inside the supplier so it is assembled after the
        // virtual scheduler is installed.
        final InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        // 1 initial + 1 retry = 2 total calls
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldRetryOn500AndSucceedOnThirdAttempt() {
        // First two calls return 500, third returns 200
        HttpResponse errorResponse = createMockResponse(500, "{\"error\": \"Internal Server Error\"}");
        HttpResponse successResponse = createMockResponse(200, createSuccessResponseBody());

        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(errorResponse))
            .thenReturn(Mono.just(errorResponse))
            .thenReturn(Mono.just(successResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .assertNext(result -> assertThat(result).isNotNull())
            .verifyComplete();

        // 1 initial + 2 retries = 3 total calls
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldExhaustRetriesOn429AndFail() {
        HttpResponse throttledResponse = createMockResponse(429, "{\"error\": \"Too Many Requests\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(throttledResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        verify(mockHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldExhaustRetriesOn500AndFail() {
        HttpResponse errorResponse = createMockResponse(500, "{\"error\": \"Internal Server Error\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(errorResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        verify(mockHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldExhaustRetriesOn502AndFail() {
        HttpResponse errorResponse = createMockResponse(502, "{\"error\": \"Bad Gateway\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(errorResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        verify(mockHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any(Duration.class));
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldExhaustRetriesOn503AndFail() {
        HttpResponse errorResponse = createMockResponse(503, "{\"error\": \"Service Unavailable\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(errorResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        verify(mockHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any(Duration.class));
    }

    // -------------------------------------------------------------------------
    // DataProvider — non-retryable codes fail immediately (1 call),
    //                retryable codes exhaust retries (TOTAL_CALLS_AFTER_EXHAUSTION calls)
    // -------------------------------------------------------------------------

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
        HttpResponse mockResponse = createMockResponse(statusCode, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> containsMessage(error, "Semantic rerank request failed"))
            .verify();

        // Non-retryable — exactly 1 call regardless of status code
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(Duration.class));
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
        HttpResponse mockResponse = createMockResponse(statusCode, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        final InferenceService service = new InferenceService(mockTokenCredential);
        StepVerifier.withVirtualTime(() -> {
                service.retryScheduler = reactor.test.scheduler.VirtualTimeScheduler.get();
                service.retryJitter = 0.0;
                return service.semanticRerank("query", Arrays.asList("doc1"), null);
            })
            .thenAwait(InferenceService.RETRY_MAX_BACKOFF.multipliedBy(TOTAL_CALLS_AFTER_EXHAUSTION))
            .expectErrorMatches(error -> isRetryExhaustedWithMessage(error, "Semantic rerank request failed"))
            .verify();

        // 1 initial attempt + RETRY_MAX_ATTEMPTS retries
        verify(mockHttpClient, times(TOTAL_CALLS_AFTER_EXHAUSTION))
            .send(any(HttpRequest.class), any(Duration.class));
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Checks that an error represents exhausted retries on a retryable inference failure.
     *
     * When {@code Retry.backoff} exhausts all attempts it wraps the last error in a
     * {@code RetryExhaustedException}. {@link com.azure.cosmos.CosmosException#getMessage()}
     * returns a JSON diagnostic blob, not the plain constructor message, so we use
     * {@link com.azure.cosmos.CosmosException#getShortMessage()} for plain-string matching.
     * As a fallback we also walk the cause chain.
     */
    private boolean isRetryExhaustedWithMessage(Throwable error, String messageSubstring) {
        if (!Exceptions.isRetryExhausted(error)) {
            // Not wrapped — check the error itself
            return containsMessage(error, messageSubstring);
        }
        // Walk the cause chain from the RetryExhaustedException
        Throwable cause = error.getCause();
        while (cause != null) {
            if (containsMessage(cause, messageSubstring)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean containsMessage(Throwable t, String substring) {
        if (t instanceof com.azure.cosmos.CosmosException) {
            // getShortMessage() returns the plain constructor message without JSON wrapping
            String short_ = ((com.azure.cosmos.CosmosException) t).getShortMessage();
            if (short_ != null && short_.contains(substring)) {
                return true;
            }
        }
        return t.getMessage() != null && t.getMessage().contains(substring);
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

    private HttpResponse createMockResponse(int statusCode, String body) {
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(statusCode);
        when(mockResponse.bodyAsString()).thenReturn(Mono.just(body));
        when(mockResponse.headers()).thenReturn(new com.azure.cosmos.implementation.http.HttpHeaders());
        return mockResponse;
    }
}
