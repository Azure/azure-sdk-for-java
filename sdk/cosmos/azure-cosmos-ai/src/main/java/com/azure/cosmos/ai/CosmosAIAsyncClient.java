// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai;

import com.azure.cosmos.ai.implementation.InferenceService;
import com.azure.cosmos.ai.models.SemanticRerankResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Asynchronous client for Azure Cosmos DB AI services.
 *
 * <p>Provides semantic reranking of documents using the Azure Cosmos DB inference service.
 * Use {@link CosmosAIClientBuilder} to create an instance.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * CosmosAIAsyncClient client = new CosmosAIClientBuilder()
 *     .endpoint("https://my-inference.dbinference.azure.com")
 *     .credential(new DefaultAzureCredentialBuilder().build())
 *     .buildAsyncClient();
 *
 * client.semanticRerank("What is the capital of France?", documents, options)
 *     .subscribe(result -&gt; {
 *         result.getScores().forEach(s -&gt; System.out.println(s.getIndex() + ": " + s.getScore()));
 *     });
 * </pre>
 */
public final class CosmosAIAsyncClient implements AutoCloseable {

    private final InferenceService inferenceService;

    /**
     * Creates a new CosmosAIAsyncClient.
     *
     * @param inferenceService The inference service instance.
     */
    CosmosAIAsyncClient(InferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

    /**
     * Performs semantic reranking of documents using the inference service.
     *
     * @param rerankContext The query or context string used to score documents.
     * @param documents The list of document strings to rerank (must be non-null and non-empty).
     * @param options Optional reranking parameters as a {@code Map<String, Object>}.
     *                Supported keys:
     *                <ul>
     *                  <li>{@code "return_documents"} (Boolean) — include document text in the response</li>
     *                  <li>{@code "top_k"} (Integer) — maximum number of results to return</li>
     *                  <li>{@code "batch_size"} (Integer) — documents per inference batch</li>
     *                  <li>{@code "sort"} (Boolean) — sort results by relevance score</li>
     *                  <li>{@code "document_type"} (String) — {@code "string"} or {@code "json"}</li>
     *                  <li>{@code "target_paths"} (String) — JSON paths for extraction when
     *                      {@code document_type} is {@code "json"}</li>
     *                  <li>{@code "timeout_seconds"} (Number) — per-request timeout override</li>
     *                </ul>
     * @return A {@link Mono} emitting the {@link SemanticRerankResult}.
     */
    public Mono<SemanticRerankResult> semanticRerank(
        String rerankContext,
        List<String> documents,
        Map<String, Object> options) {

        return inferenceService.semanticRerank(rerankContext, documents, options);
    }

    /**
     * Closes this client and releases resources.
     */
    @Override
    public void close() {
        inferenceService.close();
    }
}
