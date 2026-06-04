// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.cosmos.ai.models.InferenceResponseParser;
import com.azure.cosmos.ai.models.SemanticRerankResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Internal service for semantic reranking operations using azure-core HTTP pipeline.
 * <p>
 * While this class is public, it is not part of our published public APIs.
 * This is meant to be internally used only by our SDK.
 */
public class InferenceService implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InferenceService.class);
    private static final String BASE_PATH = "/inference/semanticReranking";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Option keys that callers can pass in the options map
    /** Timeout option key. */
    public static final String OPTION_TIMEOUT_SECONDS = "timeout_seconds";
    /** Return documents option key. */
    public static final String OPTION_RETURN_DOCUMENTS = "return_documents";
    /** Top K option key. */
    public static final String OPTION_TOP_K = "top_k";
    /** Batch size option key. */
    public static final String OPTION_BATCH_SIZE = "batch_size";
    /** Sort option key. */
    public static final String OPTION_SORT = "sort";
    /** Document type option key. */
    public static final String OPTION_DOCUMENT_TYPE = "document_type";
    /** Target paths option key. */
    public static final String OPTION_TARGET_PATHS = "target_paths";

    /**
     * Default per-request timeout for semantic rerank calls (120 seconds).
     */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);

    // Retry policy — matches Python: TOTAL_RETRIES=3, RETRY_BACKOFF_FACTOR=0.8s, RETRY_BACKOFF_MAX=120s
    static final int RETRY_MAX_ATTEMPTS = 3;
    static final Duration RETRY_INITIAL_BACKOFF = Duration.ofMillis(800);
    static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(120);

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(429, 500, 502, 503))
    );

    private static final HttpHeaderName RETRY_AFTER_HEADER = HttpHeaderName.fromString("Retry-After");

    private final URI inferenceEndpoint;
    private final HttpPipeline httpPipeline;

    // Package-private so tests can inject a VirtualTimeScheduler to make backoff delays deterministic
    Scheduler retryScheduler = Schedulers.parallel();
    // Package-private so tests can disable jitter for deterministic virtual-time delays
    double retryJitter = 0.5;

    /**
     * Creates a new InferenceService instance.
     *
     * @param endpoint The inference service base endpoint.
     * @param httpPipeline The HTTP pipeline (with auth, retry, logging policies).
     * @throws NullPointerException if endpoint or httpPipeline is null.
     */
    public InferenceService(URI endpoint, HttpPipeline httpPipeline) {
        Objects.requireNonNull(endpoint, "'endpoint' must not be null.");
        Objects.requireNonNull(httpPipeline, "'httpPipeline' must not be null.");

        this.inferenceEndpoint = URI.create(endpoint.toString() + BASE_PATH);
        this.httpPipeline = httpPipeline;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("InferenceService initialized with endpoint: {}", this.inferenceEndpoint);
        }
    }

    /**
     * Closes the InferenceService.
     */
    @Override
    public void close() {
        LOGGER.info("Shutting down InferenceService...");
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

        Objects.requireNonNull(rerankContext, "Rerank context cannot be null");
        Objects.requireNonNull(documents, "Documents list cannot be null");

        if (rerankContext.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Rerank context cannot be empty"));
        }
        if (documents.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Documents list cannot be empty"));
        }

        final Duration requestTimeout = resolveRequestTimeout(options);

        try {
            String requestBody = buildRequestPayload(rerankContext, documents, options);

            HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, inferenceEndpoint.toURL())
                .setBody(requestBody.getBytes(StandardCharsets.UTF_8))
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Sending semantic rerank request to: {} (timeout: {})", inferenceEndpoint, requestTimeout);
            }

            return httpPipeline.send(httpRequest)
                .timeout(requestTimeout)
                .flatMap(this::parseResponse)
                .as(this::withRetry)
                .doOnError(error -> LOGGER.error("Semantic rerank operation failed", error));

        } catch (Exception e) {
            LOGGER.error("Failed to create request", e);
            return Mono.error(new IllegalArgumentException(
                "Failed to create semantic rerank request: " + e.getMessage(), e));
        }
    }

    private String buildRequestPayload(String rerankContext, List<String> documents,
                                       Map<String, Object> options) throws IOException {
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

        return OBJECT_MAPPER.writeValueAsString(payload);
    }

    /**
     * Resolves the effective request timeout.
     */
    private static Duration resolveRequestTimeout(Map<String, Object> options) {
        if (options != null) {
            Object value = options.get(OPTION_TIMEOUT_SECONDS);
            if (value instanceof Number) {
                double seconds = ((Number) value).doubleValue();
                if (seconds > 0) {
                    return Duration.ofMillis((long) (seconds * 1000));
                } else {
                    LOGGER.warn("Invalid '{}' value: {}. Must be > 0. Using default timeout {}.",
                        OPTION_TIMEOUT_SECONDS, seconds, DEFAULT_REQUEST_TIMEOUT);
                }
            }
        }
        return DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Wraps a Mono with retry logic for transient inference endpoint failures.
     */
    private <T> Mono<T> withRetry(Mono<T> source) {
        return source.retryWhen(
            Retry.from(retrySignals -> retrySignals.concatMap(signal -> {
                Throwable error = signal.failure();
                if (!(error instanceof HttpResponseException)) {
                    return Mono.error(error);
                }
                HttpResponseException httpEx = (HttpResponseException) error;
                com.azure.core.http.HttpResponse response = httpEx.getResponse();
                int statusCode = response != null ? response.getStatusCode() : 0;
                if (!RETRYABLE_STATUS_CODES.contains(statusCode)) {
                    return Mono.error(error);
                }
                long attempt = signal.totalRetries();
                if (attempt >= RETRY_MAX_ATTEMPTS) {
                    return Mono.error(error);
                }

                Duration delay = computeRetryDelay(response, statusCode, attempt);
                LOGGER.warn(
                    "Semantic rerank transient failure (status={}, attempt={}/{}), retrying after {} ms...",
                    statusCode, attempt + 1, RETRY_MAX_ATTEMPTS, delay.toMillis());
                return Mono.delay(delay, retryScheduler);
            }))
        );
    }

    private Duration computeRetryDelay(HttpResponse response, int statusCode, long attempt) {
        if (statusCode == 429 && response != null) {
            Duration serverDelay = parseRetryAfterSeconds(response.getHeaders());
            if (serverDelay != null) {
                return serverDelay.compareTo(RETRY_MAX_BACKOFF) > 0 ? RETRY_MAX_BACKOFF : serverDelay;
            }
        }
        return exponentialBackoffWithJitter(attempt);
    }

    private static Duration parseRetryAfterSeconds(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String value = headers.getValue(RETRY_AFTER_HEADER);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long seconds = Long.parseLong(value.trim());
            if (seconds < 0) {
                return null;
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Duration exponentialBackoffWithJitter(long attempt) {
        long baseMillis = RETRY_INITIAL_BACKOFF.toMillis() << Math.min(attempt, 30);
        long cappedMillis = Math.min(baseMillis, RETRY_MAX_BACKOFF.toMillis());
        long jitterRange = (long) (cappedMillis * retryJitter);
        long jitter = jitterRange > 0
            ? ThreadLocalRandom.current().nextLong(-jitterRange, jitterRange + 1)
            : 0L;
        return Duration.ofMillis(Math.max(0L, cappedMillis + jitter));
    }

    private Mono<SemanticRerankResult> parseResponse(HttpResponse response) {
        int statusCode = response.getStatusCode();

        return response.getBodyAsString()
            .flatMap(bodyString -> {
                if (statusCode >= 400) {
                    LOGGER.error("Semantic rerank request failed with status {}: {}", statusCode, bodyString);
                    return Mono.error(new HttpResponseException(
                        String.format("Semantic rerank request failed with status %d: %s", statusCode, bodyString),
                        response));
                }

                try {
                    SemanticRerankResult result = InferenceResponseParser.parseRerankResponse(bodyString);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Successfully parsed semantic rerank response with {} scores",
                            result.getScores() != null ? result.getScores().size() : 0);
                    }

                    return Mono.just(result);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse semantic rerank response", e);
                    return Mono.error(new IllegalStateException(
                        "Failed to parse semantic rerank response: " + e.getMessage(), e));
                }
            });
    }
}
