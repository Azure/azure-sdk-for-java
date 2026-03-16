// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.inference;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
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
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final String INFERENCE_USER_AGENT =
        "cosmos-inference-java/" + HttpConstants.Versions.getSdkVersion();
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    // System property / environment variable names
    private static final String MAX_CONNECTION_LIMIT_PROPERTY = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_SERVICE_MAX_CONNECTION_LIMIT";
    private static final String MAX_CONNECTION_LIMIT_VARIABLE = "AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_SERVICE_MAX_CONNECTION_LIMIT";

    // Option keys that callers can pass in the options map
    public static final String OPTION_TIMEOUT_SECONDS = "timeout_seconds";
    public static final String OPTION_RETURN_DOCUMENTS = "return_documents";
    public static final String OPTION_TOP_K = "top_k";
    public static final String OPTION_BATCH_SIZE = "batch_size";
    public static final String OPTION_SORT = "sort";
    public static final String OPTION_DOCUMENT_TYPE = "document_type";
    public static final String OPTION_TARGET_PATHS = "target_paths";

    /**
     * Default network-level (connection + read) timeout for inference service HTTP calls.
     * Set to match {@link #DEFAULT_REQUEST_TIMEOUT} because inference calls can take tens of
     * seconds for large document sets. If this were shorter than the operation timeout,
     * the socket would time out before the 120-second operation deadline could fire.
     */
    public static final Duration DEFAULT_NETWORK_REQUEST_TIMEOUT = Duration.ofSeconds(120);
    public static final Duration DEFAULT_IDLE_CONNECTION_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration DEFAULT_CONNECTION_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    /**
     * Default per-request timeout for semantic rerank calls (120 seconds).
     * Callers using the async API can override downstream with {@code .timeout(Duration)}.
     * Callers using the sync API can override by passing {@value #OPTION_TIMEOUT_SECONDS} in the options map.
     */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);
    /**
     * Default maximum number of pooled connections to the inference endpoint.
     * Matches .NET's {@code inferenceServiceDefaultMaxConnectionLimit = 50}.
     * Override via system property or environment variable
     * {@value #MAX_CONNECTION_LIMIT_PROPERTY}.
     */
    public static final int DEFAULT_MAX_CONNECTION_POOL_SIZE = 50;

    // Retry policy — matches Python: TOTAL_RETRIES=3, RETRY_BACKOFF_FACTOR=0.8s, RETRY_BACKOFF_MAX=120s
    static final int RETRY_MAX_ATTEMPTS = 3;
    static final Duration RETRY_INITIAL_BACKOFF = Duration.ofMillis(800);
    static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(120);
    // Status codes that are safe to retry (transient failures and rate limiting)
    private static final Set<Integer> RETRYABLE_STATUS_CODES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            HttpConstants.StatusCodes.TOO_MANY_REQUESTS,      // 429 — rate limited
            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,  // 500 — transient server error
            502,                                               // 502 — bad gateway (no constant in HttpConstants)
            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE     // 503 — transient unavailability
        ))
    );

    private final URI inferenceEndpoint;
    private final HttpClient httpClient;
    private SimpleTokenCache tokenCache = null;
    // Package-private so tests can inject a VirtualTimeScheduler to make backoff delays deterministic
    Scheduler retryScheduler = Schedulers.parallel();
    // Package-private so tests can disable jitter for deterministic virtual-time delays
    double retryJitter = 0.5;

    /**
     * Creates a new InferenceService instance.
     *
     * @param tokenCredential The Azure AD token credential.
     * @throws IllegalArgumentException if inference endpoint is not configured or token credential is null.
     */
    public InferenceService(TokenCredential tokenCredential) {
        checkNotNull(tokenCredential,
            "Semantic reranking requires AAD authentication. "
                + "Rebuild the CosmosClient using .credential(TokenCredential) — "
                + "key-based auth (master key or AzureKeyCredential) is not supported for this operation.");

        Configs configs = new Configs();
        URI inferenceBaseUrl = configs.getInferenceServiceEndpoint();

        if (inferenceBaseUrl == null || inferenceBaseUrl.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Inference endpoint property must be set to use semantic reranking");
        }

        this.inferenceEndpoint = URI.create(inferenceBaseUrl + BASE_PATH);
        HttpClientConfig httpClientConfig = new HttpClientConfig(configs)
            .withNetworkRequestTimeout(DEFAULT_NETWORK_REQUEST_TIMEOUT)
            .withConnectionAcquireTimeout(DEFAULT_CONNECTION_ACQUIRE_TIMEOUT)
            .withMaxIdleConnectionTimeout(DEFAULT_IDLE_CONNECTION_TIMEOUT)
            .withPoolSize(resolveMaxConnectionPoolSize());
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
     * Resolves the maximum connection pool size from system property, environment variable,
     * or the default ({@value #DEFAULT_MAX_CONNECTION_POOL_SIZE}).
     * Matches .NET's AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_SERVICE_MAX_CONNECTION_LIMIT override.
     */
    private static int resolveMaxConnectionPoolSize() {
        String fromProperty = System.getProperty(MAX_CONNECTION_LIMIT_PROPERTY);
        if (fromProperty != null && !fromProperty.isEmpty()) {
            try {
                return Integer.parseInt(fromProperty);
            } catch (NumberFormatException e) {
                logger.warn("Invalid value for system property {}: '{}'. Using default {}.",
                    MAX_CONNECTION_LIMIT_PROPERTY, fromProperty, DEFAULT_MAX_CONNECTION_POOL_SIZE);
            }
        }

        String fromEnv = System.getenv(MAX_CONNECTION_LIMIT_VARIABLE);
        if (fromEnv != null && !fromEnv.isEmpty()) {
            try {
                return Integer.parseInt(fromEnv);
            } catch (NumberFormatException e) {
                logger.warn("Invalid value for environment variable {}: '{}'. Using default {}.",
                    MAX_CONNECTION_LIMIT_VARIABLE, fromEnv, DEFAULT_MAX_CONNECTION_POOL_SIZE);
            }
        }

        return DEFAULT_MAX_CONNECTION_POOL_SIZE;
    }

    /**
     * Performs semantic reranking of documents.
     *
     * <p>The request timeout defaults to 120 seconds ({@link #DEFAULT_REQUEST_TIMEOUT}).
     * To override it, pass {@code "timeout_seconds"} (as a {@link Number}) in the {@code options} map.
     * Callers using the async {@link reactor.core.publisher.Mono} result can also apply
     * {@code .timeout(Duration)} downstream without needing to set the option.
     *
     * @param rerankContext The query or context string used to score documents.
     * @param documents The list of document strings to rerank.
     * @param options Optional reranking parameters. SDK-local keys ({@link #OPTION_TIMEOUT_SECONDS})
     *                are consumed locally and not forwarded to the inference endpoint.
     * @return A Mono emitting the semantic rerank result.
     */
    public Mono<SemanticRerankResult> semanticRerank(
        String rerankContext,
        List<String> documents,
        Map<String, Object> options) {

        // Resolve the per-request timeout: options > default
        final Duration requestTimeout = resolveRequestTimeout(options);

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
                            // timeout_seconds is a SDK-local option — do not forward to the endpoint
                            if (value != null && !OPTION_TIMEOUT_SECONDS.equals(key)) {
                                payload.set(key, OBJECT_MAPPER.valueToTree(value));
                            }
                        });
                    }

                    String requestBody = OBJECT_MAPPER.writeValueAsString(payload);

                    HttpRequest httpRequest = getHttpRequest(accessToken);
                    httpRequest.withBody(requestBody.getBytes(StandardCharsets.UTF_8));

                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending semantic rerank request to: {} (timeout: {})", inferenceEndpoint, requestTimeout);
                    }

                    return httpClient.send(httpRequest, requestTimeout)
                        .flatMap(response -> parseResponse(response));

                } catch (IOException e) {
                    logger.error("Failed to serialize request payload", e);
                    return Mono.error(BridgeInternal.createCosmosException(500, "Failed to serialize semantic rerank request"));
                }
            })
            .as(this::withRetry)
            .doOnError(error -> logger.error("Semantic rerank operation failed", error));
    }

    /**
     * Resolves the effective request timeout.
     * If the caller supplied {@value #OPTION_TIMEOUT_SECONDS} in the options map, that value is used;
     * otherwise {@link #DEFAULT_REQUEST_TIMEOUT} applies.
     */
    private static Duration resolveRequestTimeout(Map<String, Object> options) {
        if (options != null) {
            Object value = options.get(OPTION_TIMEOUT_SECONDS);
            if (value instanceof Number) {
                double seconds = ((Number) value).doubleValue();
                if (seconds > 0) {
                    return Duration.ofMillis((long) (seconds * 1000));
                } else {
                    logger.warn("Invalid '{}' value: {}. Must be > 0. Using default timeout {}.",
                        OPTION_TIMEOUT_SECONDS, seconds, DEFAULT_REQUEST_TIMEOUT);
                }
            }
        }
        return DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Wraps a {@link Mono} with retry logic for transient inference endpoint failures.
     *
     * <p>Retries up to {@value #RETRY_MAX_ATTEMPTS} times on retryable {@link com.azure.cosmos.CosmosException}
     * status codes (429, 500, 502, 503) using exponential backoff with jitter, starting at
     * {@link #RETRY_INITIAL_BACKOFF} and capped at {@link #RETRY_MAX_BACKOFF}.
     * All other exceptions (non-retryable status codes, serialisation errors, etc.) propagate immediately.
     *
     * <p>Matches the Python SDK's retry policy:
     * {@code TOTAL_RETRIES=3, RETRY_BACKOFF_FACTOR=0.8, RETRY_BACKOFF_MAX=120s}.
     */
    private <T> Mono<T> withRetry(Mono<T> source) {
        return source.retryWhen(
            Retry.backoff(RETRY_MAX_ATTEMPTS, RETRY_INITIAL_BACKOFF)
                .maxBackoff(RETRY_MAX_BACKOFF)
                .jitter(retryJitter)
                .scheduler(retryScheduler)
                .filter(error -> {
                    if (!(error instanceof CosmosException)) {
                        return false;
                    }
                    int statusCode = ((CosmosException) error).getStatusCode();
                    return RETRYABLE_STATUS_CODES.contains(statusCode);
                })
                .doBeforeRetry(signal -> {
                    Throwable failure = signal.failure();
                    int statusCode = failure instanceof CosmosException
                        ? ((CosmosException) failure).getStatusCode()
                        : -1;
                    logger.warn(
                        "Semantic rerank transient failure (status={}, attempt={}/{}), retrying after backoff...",
                        statusCode, signal.totalRetries() + 1, RETRY_MAX_ATTEMPTS);
                })
        );
    }

    private HttpRequest getHttpRequest(AccessToken accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpConstants.HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpConstants.HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getToken());
        headers.set(HttpConstants.HttpHeaders.USER_AGENT, INFERENCE_USER_AGENT);

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
                                JsonNode indexNode = scoreNode.get("index");
                                JsonNode scoreValNode = scoreNode.get("score");
                                if (indexNode != null) {
                                    ModelBridgeInternal.setSemanticRerankScoreIndex(score, indexNode.asInt());
                                }
                                if (scoreValNode != null) {
                                    ModelBridgeInternal.setSemanticRerankScoreScore(score, scoreValNode.asDouble());
                                }

                                if (scoreNode.has("document")) {
                                    ModelBridgeInternal.setSemanticRerankScoreDocument(score, scoreNode.get("document").asText());
                                }

                                scores.add(score);
                            }
                        }
                        ModelBridgeInternal.setSemanticRerankResultScores(result, scores);
                    }

                    // Parse latency
                    if (rootNode.has("latency")) {
                        Map<String, Object> latency = new HashMap<>();
                        rootNode.get("latency").fields().forEachRemaining(
                            entry -> latency.put(entry.getKey(), entry.getValue().asDouble()));
                        ModelBridgeInternal.setSemanticRerankResultLatency(result, latency);
                    }

                    // Parse token usage
                    if (rootNode.has("token_usage")) {
                        Map<String, Object> tokenUsage = new HashMap<>();
                        rootNode.get("token_usage").fields().forEachRemaining(
                            entry -> tokenUsage.put(entry.getKey(), entry.getValue().asInt()));
                        ModelBridgeInternal.setSemanticRerankResultTokenUsage(result, tokenUsage);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Successfully parsed semantic rerank response with {} scores",
                            result.getScores() != null ? result.getScores().size() : 0);
                    }

                    return Mono.just(result);

                } catch (Exception e) {
                    logger.error("Failed to parse semantic rerank response", e);
                    // Use BadRequestException (400) + CUSTOM_SERIALIZER_EXCEPTION sub-status —
                    // the same pattern CosmosItemSerializer uses for client-side deserialization
                    // failures. 400 is not in RETRYABLE_STATUS_CODES, so no retry will be attempted.
                    BadRequestException parseException = new BadRequestException(
                        "Failed to parse semantic rerank response: " + e.getMessage(), e);
                    BridgeInternal.setSubStatusCode(parseException,
                        HttpConstants.SubStatusCodes.CUSTOM_SERIALIZER_EXCEPTION);
                    return Mono.error(parseException);
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
