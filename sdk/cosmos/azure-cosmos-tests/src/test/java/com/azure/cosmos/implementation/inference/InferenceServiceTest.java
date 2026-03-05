// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.inference;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.SemanticRerankRequestOptions;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InferenceServiceTest {

    private static final String MOCK_TOKEN = "mock-access-token";
    private static final String TEST_ENDPOINT = "https://test-inference.westus.dbinference.azure.com";
    private static final String INFERENCE_ENDPOINT_PROPERTY = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT";

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

    @Test(groups = {"unit"})
    public void constructorShouldThrowWhenTokenCredentialIsNull() {
        assertThatThrownBy(() -> new InferenceService(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Token credential is required");
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
                assertThat(result.getLatency().getInferenceTime()).isEqualTo(0.5);
                assertThat(result.getTokenUsage()).isNotNull();
                assertThat(result.getTokenUsage().getTotalTokens()).isEqualTo(100);
            })
            .verifyComplete();

        // Verify the request was sent
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
        SemanticRerankRequestOptions options = new SemanticRerankRequestOptions()
            .setReturnDocuments(true)
            .setTopK(5)
            .setBatchSize(16)
            .setSort(true);

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

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleHttpError() {
        String errorBody = "{\"error\": \"Internal server error\"}";
        HttpResponse mockResponse = createMockResponse(500, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Semantic rerank request failed"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandle403Forbidden() {
        String errorBody = "{\"error\": \"Forbidden - insufficient permissions\"}";
        HttpResponse mockResponse = createMockResponse(403, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Semantic rerank request failed")
                && error.getMessage().contains("Forbidden"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleTokenAcquisitionFailure() {
        // Mock token credential to fail
        when(mockTokenCredential.getToken(any(TokenRequestContext.class)))
            .thenReturn(Mono.error(new RuntimeException("Token acquisition failed")));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error instanceof RuntimeException
                && error.getMessage().contains("Token acquisition failed"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleNetworkError() {
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.error(new RuntimeException("Network connection failed")));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error instanceof RuntimeException
                && error.getMessage().contains("Network connection failed"))
            .verify();
    }

    @Test(groups = {"unit"})
    public void semanticRerankShouldHandleMalformedResponse() {
        String malformedBody = "{ invalid json }";
        HttpResponse mockResponse = createMockResponse(200, malformedBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Failed to parse semantic rerank response"))
            .verify();
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

    @DataProvider(name = "httpStatusCodes")
    public Object[][] httpStatusCodeProvider() {
        return new Object[][] {
            {400, "Bad Request"},
            {401, "Unauthorized"},
            {403, "Forbidden"},
            {404, "Not Found"},
            {429, "Too Many Requests"},
            {500, "Internal Server Error"},
            {502, "Bad Gateway"},
            {503, "Service Unavailable"}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "httpStatusCodes")
    public void semanticRerankShouldHandleVariousHttpErrors(int statusCode, String errorMessage) {
        String errorBody = String.format("{\"error\": \"%s\"}", errorMessage);
        HttpResponse mockResponse = createMockResponse(statusCode, errorBody);
        when(mockHttpClient.send(any(HttpRequest.class), any(Duration.class)))
            .thenReturn(Mono.just(mockResponse));

        InferenceService service = new InferenceService(mockTokenCredential);

        StepVerifier.create(service.semanticRerank("query", Arrays.asList("doc1"), null))
            .expectErrorMatches(error -> error.getMessage().contains("Semantic rerank request failed"))
            .verify();
    }

    // Helper methods

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
