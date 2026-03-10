// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.inference;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.SemanticRerankResult;
import com.azure.cosmos.models.SemanticRerankScore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Internal service for semantic reranking operations.
 * <p>
 * While this class is public, it is not part of our published public APIs.
 * This is meant to be internally used only by our SDK.
 */
public class InferenceService implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(InferenceService.class);
    private static final String INFERENCE_SCOPE = "https://dbinference.azure.com/.default";
    private static final String BASE_PATH = "/inference/semanticReranking";
    private static final String INFERENCE_USER_AGENT = "cosmos-inference-java";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);
    private static final String INFERENCE_ENDPOINT_PROPERTY = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT";
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    public static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration DEFAULT_CONNECTION_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    public static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 5;

    private final URI inferenceEndpoint;
    private final HttpClient httpClient;
    private SimpleTokenCache tokenCache = null;

    /**
     * Creates a new InferenceService instance.
     *
     * @param tokenCredential The Azure AD token credential.
     * @throws IllegalArgumentException if inference endpoint is not configured or token credential is null.
     */
    public InferenceService(TokenCredential tokenCredential) {
        checkNotNull(tokenCredential, "Token credential is required for semantic reranking");

        URI inferenceBaseUrl = new Configs().getInferenceServiceEndpoint();

        if (inferenceBaseUrl == null || inferenceBaseUrl.toString().trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("System property '%s' must be set to use semantic reranking",
                    INFERENCE_ENDPOINT_PROPERTY));
        }

        this.inferenceEndpoint = URI.create(inferenceBaseUrl + BASE_PATH);
        HttpClientConfig httpClientConfig =  new HttpClientConfig(Configs.getDefaultInferenceServiceConfig())
            .withNetworkRequestTimeout(DEFAULT_NETWORK_REQUEST_TIMEOUT)
            .withConnectionAcquireTimeout(DEFAULT_CONNECTION_ACQUIRE_TIMEOUT)
            .withMaxIdleConnectionTimeout(DEFAULT_IDLE_CONNECTION_TIMEOUT);
        this.httpClient = HttpClient.createFixed(httpClientConfig);

        // Create token cache for inference service scope
        this.tokenCache = new SimpleTokenCache(() -> {
            TokenRequestContext context = new TokenRequestContext().addScopes(INFERENCE_SCOPE);
            return tokenCredential.getToken(context)
                .doOnNext(token -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Acquired AAD token for inference service scope: {}", INFERENCE_SCOPE);
                    }
                });
        });

        if (logger.isInfoEnabled()) {
            logger.info("InferenceService initialized with endpoint: {}", inferenceEndpoint);
        }
    }

    /**
     * Closes the InferenceService and releases the underlying HTTP client connection pool.
     */
    @Override
    public void close() {
        logger.info("Shutting down InferenceService httpClient...");
        LifeCycleUtils.closeQuietly(this.httpClient);
    }

    /**
     * Performs semantic reranking of documents.
     *
     * @param rerankContext The query or context string used to score documents.
     * @param documents The list of document strings to rerank.
     * @param options Optional reranking parameters.
     * @return A Mono emitting the semantic rerank result.
     */
    public Mono<SemanticRerankResult> semanticRerank(
        String rerankContext,
        List<String> documents,
        Map<String, Object> options) {

        checkNotNull(rerankContext, "Rerank context cannot be null");
        checkNotNull(documents, "Documents list cannot be null");

        if (rerankContext.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Rerank context cannot be empty"));
        }

        if (documents.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Documents list cannot be empty"));
        }

        return this.tokenCache.getToken()
            .flatMap(accessToken -> {
                try {
                    // Build request payload
                    ObjectNode payload = OBJECT_MAPPER.createObjectNode();
                    payload.put("query", rerankContext);

                    ArrayNode documentsArray = payload.putArray("documents");
                    for (String doc : documents) {
                        documentsArray.add(doc);
                    }

                    if (options != null) {
                        options.forEach((key, value) -> {
                            if (value instanceof Boolean) {
                                payload.put(key, (Boolean) value);
                            } else if (value instanceof Integer) {
                                payload.put(key, (Integer) value);
                            }
                        });
                    }

                    String requestBody = OBJECT_MAPPER.writeValueAsString(payload);

                    // Build HTTP request
                    HttpRequest httpRequest = getHttpRequest(accessToken);

                    httpRequest.withBody(requestBody.getBytes(StandardCharsets.UTF_8));

                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending semantic rerank request to: {}", inferenceEndpoint);
                    }

                    return httpClient.send(httpRequest, DEFAULT_TIMEOUT)
                        .flatMap(response -> parseResponse(response));

                } catch (IOException e) {
                    logger.error("Failed to serialize request payload", e);
                    return Mono.error(BridgeInternal.createCosmosException(500, "Failed to serialize semantic rerank request"));
                }
            })
            .doOnError(error -> {
                logger.error("Semantic rerank operation failed", error);
            });
    }

    private HttpRequest getHttpRequest(AccessToken accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpConstants.HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpConstants.HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken());
        headers.set(HttpConstants.HttpHeaders.USER_AGENT, INFERENCE_USER_AGENT);
        headers.set(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);

        return new HttpRequest(
            HttpMethod.POST,
            inferenceEndpoint,
            inferenceEndpoint.getPort(),
            headers);
    }

    /**
     * Parses the HTTP response into a SemanticRerankResult.
     *
     * @param response The HTTP response.
     * @return A Mono emitting the parsed result.
     */
    private Mono<SemanticRerankResult> parseResponse(HttpResponse response) {
        int statusCode = response.statusCode();

        return response.bodyAsString()
            .flatMap(bodyString -> {
                if (statusCode >= 400) {
                    logger.error("Semantic rerank request failed with status {}: {}", statusCode, bodyString);
                    Map<String, String> headersMap = convertHeaders(response.headers());
                    return Mono.error(BridgeInternal.createCosmosException(
                        String.format("Semantic rerank request failed: %s", bodyString),
                        null,
                        headersMap,
                        statusCode,
                        null));
                }

                try {
                    JsonNode rootNode = OBJECT_MAPPER.readTree(bodyString);
                    SemanticRerankResult result = new SemanticRerankResult();

                    // Parse scores
                    if (rootNode.has("Scores")) {
                        JsonNode scoresNode = rootNode.get("Scores");
                        List<SemanticRerankScore> scores = new ArrayList<>();

                        if (scoresNode.isArray()) {
                            for (JsonNode scoreNode : scoresNode) {
                                SemanticRerankScore score = new SemanticRerankScore();
                                score.setIndex(scoreNode.get("index").asInt());
                                score.setScore(scoreNode.get("score").asDouble());

                                if (scoreNode.has("document")) {
                                    score.setDocument(scoreNode.get("document").asText());
                                }

                                scores.add(score);
                            }
                        }
                        result.setScores(scores);
                    }

                    // Parse latency
                    if (rootNode.has("latency")) {
                        Map<String, Object> latency = new HashMap<>();
                        rootNode.get("latency").fields().forEachRemaining(
                            entry -> latency.put(entry.getKey(), entry.getValue().asDouble()));
                        result.setLatency(latency);
                    }

                    // Parse token usage
                    if (rootNode.has("token_usage")) {
                        Map<String, Object> tokenUsage = new HashMap<>();
                        rootNode.get("token_usage").fields().forEachRemaining(
                            entry -> tokenUsage.put(entry.getKey(), entry.getValue().asInt()));
                        result.setTokenUsage(tokenUsage);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully parsed semantic rerank response with {} scores",
                            result.getScores() != null ? result.getScores().size() : 0);
                    }

                    return Mono.just(result);

                } catch (IOException e) {
                    logger.error("Failed to parse semantic rerank response", e);
                    Map<String, String> headersMap = convertHeaders(response.headers());
                    return Mono.error(BridgeInternal.createCosmosException(
                        "Failed to parse semantic rerank response: " + e.getMessage(),
                        e,
                        headersMap,
                        500,
                        null));
                }
            });
    }

    /**
     * Converts HttpHeaders to a Map.
     *
     * @param headers The HTTP headers.
     * @return A map of header names to values.
     */
    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            headers.toMap().forEach((key, value) -> {
                if (value != null) {
                    headersMap.put(key, value);
                }
            });
        }
        return headersMap;
    }
}
