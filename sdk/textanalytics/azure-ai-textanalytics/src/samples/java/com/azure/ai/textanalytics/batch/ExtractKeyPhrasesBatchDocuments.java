// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrate how to analyze key phrases of a batch of text inputs.
 */
public class ExtractKeyPhrasesBatchDocuments {
    /**
     * Main method to invoke this demo about how to extract key phrases of a batch of text inputs.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("<replace-with-your-text-analytics-key-here>")
            .endpoint("<replace-with-your-text-analytics-endpoint-here>")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Extracting batch key phrases
        final DocumentResultCollection<ExtractKeyPhraseResult> extractedBatchResult = client.extractBatchKeyPhrasesWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s%n", extractedBatchResult.getModelVersion());

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = extractedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        // Extracted key phrase for each of document from a batch of documents
        for (ExtractKeyPhraseResult extractKeyPhraseResult : extractedBatchResult) {
            System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
            final List<String> documentKeyPhrases = extractKeyPhraseResult.getKeyPhrases();
            // Erroneous document
            if (documentKeyPhrases == null) {
                System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
                continue;
            }
            // Valid document
            for (String keyPhrases : documentKeyPhrases) {
                System.out.printf("Extracted phrases: %s.%n", keyPhrases);
            }
        }
    }
}
