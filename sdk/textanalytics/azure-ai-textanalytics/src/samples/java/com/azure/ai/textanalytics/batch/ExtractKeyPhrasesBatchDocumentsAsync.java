// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously extract the key phrases of a batch input text.
 */
public class ExtractKeyPhrasesBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to extract the key phrases of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My cat might need to see a veterinarian.", "en"),
            new TextDocumentInput("2", "The pitot tube is used to measure airspeed.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Extracting batch key phrases
        client.extractKeyPhrasesBatchWithResponse(inputs, requestOptions).subscribe(
            result -> {
                final DocumentResultCollection<ExtractKeyPhraseResult> extractedBatchResult = result.getValue();
                System.out.printf("Model version: %s%n", extractedBatchResult.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = extractedBatchResult.getStatistics();
                System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getInvalidDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                // Extracted key phrase for each of document from a batch of documents
                for (ExtractKeyPhraseResult extractKeyPhraseResult : extractedBatchResult) {
                    System.out.printf("Document ID: %s%n", extractKeyPhraseResult.getId());
                    // Erroneous document
                    if (extractKeyPhraseResult.isError()) {
                        System.out.printf("Cannot extract key phrases. Error: %s%n", extractKeyPhraseResult.getError().getMessage());
                        continue;
                    }
                    // Valid document
                    System.out.println("Extracted phrases:");
                    for (String keyPhrases : extractKeyPhraseResult.getKeyPhrases()) {
                        System.out.printf("%s.%n", keyPhrases);
                    }
                }
            },
            error -> System.err.println("There was an error extracting key phrases of the text inputs." + error),
            () -> System.out.println("Batch of key phrases extracted."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
