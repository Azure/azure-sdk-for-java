// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the entities of a batch input text.
 */
public class RecognizeEntitiesBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to recognize the entities of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "Satya Nadella is the CEO of Microsoft.", "en"),
            new TextDocumentInput("2", "Elon Musk is the CEO of SpaceX and Tesla.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Recognizing batch entities
        client.recognizeBatchEntitiesWithResponse(inputs, requestOptions).subscribe(
            result -> {
                final DocumentResultCollection<RecognizeEntitiesResult> recognizedBatchResult = result.getValue();
                System.out.printf("Model version: %s%n", recognizedBatchResult.getModelVersion());

                // Batch statistics
                final TextDocumentBatchStatistics batchStatistics = recognizedBatchResult.getStatistics();
                System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                    batchStatistics.getDocumentCount(),
                    batchStatistics.getInvalidDocumentCount(),
                    batchStatistics.getTransactionCount(),
                    batchStatistics.getValidDocumentCount());

                // Recognized entities for each of document from a batch of documents
                for (RecognizeEntitiesResult recognizeEntitiesResult : recognizedBatchResult) {
                    System.out.printf("Document ID: %s%n", recognizeEntitiesResult.getId());
                    // Erroneous document
                    if (recognizeEntitiesResult.isError()) {
                        System.out.printf("Cannot recognize entities. Error: %s%n", recognizeEntitiesResult.getError().getMessage());
                        continue;
                    }
                    // Valid document
                    for (CategorizedEntity entity : recognizeEntitiesResult.getEntities()) {
                        System.out.printf("Recognized entity: %s, entity Category: %s, entity Sub-category: %s, offset: %s, length: %s, score: %s.%n",
                            entity.getText(),
                            entity.getCategory(),
                            entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                            entity.getOffset(),
                            entity.getLength(),
                            entity.getScore());
                    }
                }
            },
            error -> System.err.println("There was an error recognizing entities of the text inputs." + error),
            () -> System.out.println("Batch of entities recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
