// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.ai.models.SemanticRerankResult;
import com.azure.cosmos.ai.models.SemanticRerankScore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample demonstrating semantic rerank functionality using Azure Cosmos DB AI SDK.
 * <p>
 * Prerequisites:
 * 1. Set environment variable {@code AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT} with
 *    the inference service endpoint, or pass it to the builder via {@code .endpoint(...)}.
 * 2. Use Azure AD authentication — provide a {@link TokenCredential} implementation.
 *    If you have {@code azure-identity} on your classpath, use:
 *    {@code new com.azure.identity.DefaultAzureCredentialBuilder().build()}
 * </p>
 */
public class SemanticRerankSample {

    /**
     * Main entry point.
     *
     * @param args command line arguments (not used).
     */
    public static void main(String[] args) {
        String inferenceEndpoint = System.getenv("AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT");
        if (inferenceEndpoint == null || inferenceEndpoint.isEmpty()) {
            System.err.println("Please set AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT environment variable");
            return;
        }

        // Provide an Azure AD TokenCredential. If azure-identity is on the classpath:
        //   TokenCredential credential = new com.azure.identity.DefaultAzureCredentialBuilder().build();
        // Key-based authentication is not supported for semantic reranking.
        throw new UnsupportedOperationException(
            "Supply a TokenCredential (e.g. DefaultAzureCredentialBuilder) and remove this line to run the sample.");
    }

    /**
     * Runs the semantic rerank demo.
     *
     * @param endpoint The inference service endpoint.
     * @param credential The Azure AD token credential.
     */
    static void runSemanticRerankDemo(String endpoint, TokenCredential credential) {
        System.out.println("Semantic Rerank Sample");
        System.out.println("======================\n");

        // BEGIN: readme-sample-createClient
        CosmosAIAsyncClient client = new CosmosAIClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();
        // END: readme-sample-createClient

        List<String> documents = Arrays.asList(
            "Berlin is the capital of Germany.",
            "Paris is the capital of France.",
            "Madrid is the capital of Spain.",
            "Rome is the capital of Italy.",
            "London is the capital of England."
        );

        String rerankContext = "What is the capital of France?";

        System.out.println("Query: " + rerankContext);
        System.out.println("\nDocuments to rerank:");
        for (int i = 0; i < documents.size(); i++) {
            System.out.println("  " + i + ": " + documents.get(i));
        }
        System.out.println();

        // Basic rerank with default options
        client.semanticRerank(rerankContext, documents, null)
            .subscribe(
                result -> printResults(result),
                error -> System.err.println("Error: " + error.getMessage())
            );

        // Rerank with custom options
        Map<String, Object> options = new HashMap<>();
        options.put("return_documents", true);
        options.put("top_k", 3);
        options.put("sort", true);

        client.semanticRerank(rerankContext, documents, options)
            .subscribe(
                result -> printResults(result),
                error -> System.err.println("Error: " + error.getMessage())
            );
    }

    private static void printResults(SemanticRerankResult result) {
        System.out.println("\nSemantic Rerank Results:");
        System.out.println("========================\n");

        List<SemanticRerankScore> scores = result.getScores();
        if (scores != null && !scores.isEmpty()) {
            System.out.println("Scores (ranked by relevance):");
            for (int i = 0; i < scores.size(); i++) {
                SemanticRerankScore score = scores.get(i);
                System.out.printf("  [%d] index=%d, score=%.7f%n", i, score.getIndex(), score.getScore());
                if (score.getDocument() != null) {
                    System.out.println("      document: " + score.getDocument());
                }
            }
        }

        Map<String, Object> latency = result.getLatency();
        if (latency != null) {
            System.out.println("\nLatency:");
            latency.forEach((key, value) -> System.out.printf("  %s: %s%n", key, value));
        }

        Map<String, Object> tokenUsage = result.getTokenUsage();
        if (tokenUsage != null) {
            System.out.println("\nToken Usage:");
            tokenUsage.forEach((key, value) -> System.out.printf("  %s: %s%n", key, value));
        }
    }
}
