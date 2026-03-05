// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.semanticrerank;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.SemanticRerankLatency;
import com.azure.cosmos.models.SemanticRerankRequestOptions;
import com.azure.cosmos.models.SemanticRerankResult;
import com.azure.cosmos.models.SemanticRerankScore;
import com.azure.cosmos.models.SemanticRerankTokenUsage;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating semantic rerank functionality in Azure Cosmos DB.
 * <p>
 * Prerequisites:
 * 1. Set environment variable AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT
 * 2. Use Azure AD authentication (not master key)
 * 3. Ensure inference service is enabled for your Cosmos DB account
 * </p>
 */
public class SemanticRerankSample {

    public static void main(String[] args) {
        String endpoint = System.getenv("COSMOS_ENDPOINT");
        
        if (endpoint == null || endpoint.isEmpty()) {
            System.err.println("Please set COSMOS_ENDPOINT environment variable");
            return;
        }
        
        String inferenceEndpoint = System.getenv("AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT");
        if (inferenceEndpoint == null || inferenceEndpoint.isEmpty()) {
            System.err.println("Please set AZURE_COSMOS_SEMANTIC_RERANKER_INFERENCE_ENDPOINT environment variable");
            return;
        }

        // Create client with Azure AD authentication (required for semantic reranking)
        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        try {
            runSemanticRerankDemo(client);
        } finally {
            client.close();
        }
    }

    private static void runSemanticRerankDemo(CosmosAsyncClient client) {
        System.out.println("Semantic Rerank Sample");
        System.out.println("======================\n");

        // Example documents to rerank
        List<String> documents = Arrays.asList(
            "Berlin is the capital of Germany.",
            "Paris is the capital of France.",
            "Madrid is the capital of Spain.",
            "Rome is the capital of Italy.",
            "London is the capital of England."
        );

        // Query context for reranking
        String rerankContext = "What is the capital of France?";

        System.out.println("Query: " + rerankContext);
        System.out.println("\nDocuments to rerank:");
        for (int i = 0; i < documents.size(); i++) {
            System.out.println("  " + i + ": " + documents.get(i));
        }
        System.out.println();

        // NOTE: This sample demonstrates the API structure.
        // Full implementation requires semantic rerank method to be added to CosmosAsyncContainer
        /*
        CosmosAsyncContainer container = client.getDatabase("mydb").getContainer("mycontainer");
        
        // Basic usage with default options
        container.semanticRerank(rerankContext, documents)
            .subscribe(
                result -> printResults(result),
                error -> System.err.println("Error: " + error.getMessage())
            );

        // Usage with custom options
        SemanticRerankRequestOptions options = new SemanticRerankRequestOptions()
            .setReturnDocuments(true)
            .setTopK(3)
            .setSort(true);

        container.semanticRerank(rerankContext, documents, options)
            .subscribe(
                result -> printResults(result),
                error -> System.err.println("Error: " + error.getMessage())
            );
        */

        // Example of what the result structure looks like
        demonstrateResultStructure();
    }

    private static void demonstrateResultStructure() {
        System.out.println("Expected Result Structure:");
        System.out.println("-------------------------");
        System.out.println("Scores:");
        System.out.println("  [0] index=1, document='Paris is the capital of France.', score=0.9921875");
        System.out.println("  [1] index=2, document='Madrid is the capital of Spain.', score=0.0024719");
        System.out.println("  [2] index=0, document='Berlin is the capital of Germany.', score=0.0014114");
        System.out.println("\nLatency:");
        System.out.println("  Data preprocess time: 0.0000019 seconds");
        System.out.println("  Inference time: 0.0215526 seconds");
        System.out.println("  Postprocess time: 0.0000012 seconds");
        System.out.println("\nToken Usage:");
        System.out.println("  Total tokens: 405");
    }

    private static void printResults(SemanticRerankResult result) {
        System.out.println("\nSemantic Rerank Results:");
        System.out.println("========================\n");

        // Print scores
        List<SemanticRerankScore> scores = result.getScores();
        if (scores != null && !scores.isEmpty()) {
            System.out.println("Scores (ranked by relevance):");
            for (int i = 0; i < scores.size(); i++) {
                SemanticRerankScore score = scores.get(i);
                System.out.printf("  [%d] index=%d, score=%.7f%n",
                    i, score.getIndex(), score.getScore());
                
                if (score.getDocument() != null) {
                    System.out.println("      document: " + score.getDocument());
                }
            }
        }

        // Print latency information
        SemanticRerankLatency latency = result.getLatency();
        if (latency != null) {
            System.out.println("\nLatency:");
            System.out.printf("  Data preprocess time: %.7f seconds%n", latency.getDataPreprocessTime());
            System.out.printf("  Inference time: %.7f seconds%n", latency.getInferenceTime());
            System.out.printf("  Postprocess time: %.7f seconds%n", latency.getPostprocessTime());
        }

        // Print token usage
        SemanticRerankTokenUsage tokenUsage = result.getTokenUsage();
        if (tokenUsage != null) {
            System.out.println("\nToken Usage:");
            System.out.printf("  Total tokens: %d%n", tokenUsage.getTotalTokens());
        }
    }
}
