// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the linked entities of a batch input text.
 */
public class RecognizeLinkedEntitiesBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of a batch input text.
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
            new TextDocumentInput("1", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("2", "Mount Shasta has lenticular clouds.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        client.recognizeLinkedEntitiesBatchWithResponse(inputs, requestOptions).subscribe(
            result -> {
                final DocumentResultCollection<RecognizeLinkedEntitiesResult> recognizedBatchResult = result.getValue();
                System.out.printf("Model version: %s%n", recognizedBatchResult.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizedBatchResult.getStatistics();
                System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getInvalidDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                // Recognized linked entities from a batch of documents
                for (RecognizeLinkedEntitiesResult linkedEntityDocumentResult : recognizedBatchResult) {
                    System.out.printf("Document ID: %s%n", linkedEntityDocumentResult.getId());
                    // Erroneous document
                    if (linkedEntityDocumentResult.isError()) {
                        System.out.printf("Cannot recognize linked entities. Error: %s%n", linkedEntityDocumentResult.getError().getMessage());
                        continue;
                    }
                    // Valid document
                    for (LinkedEntity linkedEntity : linkedEntityDocumentResult.getEntities()) {
                        System.out.println("Linked Entities:");
                        System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(),
                            linkedEntity.getDataSourceEntityId(),
                            linkedEntity.getUrl(),
                            linkedEntity.getDataSource());
                        for (LinkedEntityMatch linkedEntityMatch : linkedEntity.getLinkedEntityMatches()) {
                            System.out.printf("Text: %s, offset: %s, length: %s, score: %.2f.%n",
                                linkedEntityMatch.getText(),
                                linkedEntityMatch.getOffset(),
                                linkedEntityMatch.getLength(),
                                linkedEntityMatch.getScore());
                        }
                    }
                }
            },
            error -> System.err.println("There was an error recognizing linked entities of the text inputs." + error),
            () -> System.out.println("Batch of linked entities recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
