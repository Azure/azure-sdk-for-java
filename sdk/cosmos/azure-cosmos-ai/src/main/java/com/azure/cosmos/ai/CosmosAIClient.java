// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai;

import com.azure.cosmos.ai.implementation.InferenceService;
import com.azure.cosmos.ai.models.SemanticRerankResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Synchronous client for Azure Cosmos DB AI services.
 *
 * <p>Provides semantic reranking of documents using the Azure Cosmos DB inference service.
 * Use {@link CosmosAIClientBuilder} to create an instance.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * CosmosAIClient client = new CosmosAIClientBuilder()
 *     .endpoint("https://my-inference.dbinference.azure.com")
 *     .credential(new DefaultAzureCredentialBuilder().build())
 *     .buildClient();
 *
 * SemanticRerankResult result = client.semanticRerank("What is the capital of France?", documents, options);
 * result.getScores().forEach(s -&gt; System.out.println(s.getIndex() + ": " + s.getScore()));
 * </pre>
 */
public final class CosmosAIClient implements AutoCloseable {

    private final CosmosAIAsyncClient asyncClient;

    /**
     * Creates a new CosmosAIClient wrapping the given async client.
     *
     * @param asyncClient The async client to wrap.
     */
    CosmosAIClient(CosmosAIAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Performs semantic reranking of documents using the inference service.
     *
     * <p><strong>Timeout:</strong> This method blocks with a default timeout of 120 seconds.
     * To override, pass {@code "timeout_seconds"} (as a {@link Number}) in the {@code options} map,
     * for example: {@code options.put("timeout_seconds", 30)}.</p>
     *
     * @param rerankContext The query or context string used to score documents.
     * @param documents The list of document strings to rerank.
     * @param options Optional reranking parameters as a map. Supported keys include:
     *                {@code return_documents} (Boolean) - whether to return document text in the response,
     *                {@code top_k} (Integer) - maximum number of documents to return,
     *                {@code batch_size} (Integer) - number of documents per batch,
     *                {@code sort} (Boolean) - whether to sort results by relevance score,
     *                {@code timeout_seconds} (Number) - per-request timeout in seconds (default: 120).
     * @return The semantic rerank result.
     * @throws RuntimeException if the operation fails or times out.
     */
    public SemanticRerankResult semanticRerank(
        String rerankContext,
        List<String> documents,
        Map<String, Object> options) {

        Duration blockTimeout = resolveBlockTimeout(options);
        return asyncClient.semanticRerank(rerankContext, documents, options)
            .block(blockTimeout);
    }

    /**
     * Closes this client and releases resources.
     */
    @Override
    public void close() {
        asyncClient.close();
    }

    private static Duration resolveBlockTimeout(Map<String, Object> options) {
        if (options != null) {
            Object timeoutVal = options.get(InferenceService.OPTION_TIMEOUT_SECONDS);
            if (timeoutVal instanceof Number) {
                double seconds = ((Number) timeoutVal).doubleValue();
                if (seconds > 0) {
                    return Duration.ofMillis((long) (seconds * 1000));
                }
            }
        }
        return InferenceService.DEFAULT_REQUEST_TIMEOUT;
    }
}
