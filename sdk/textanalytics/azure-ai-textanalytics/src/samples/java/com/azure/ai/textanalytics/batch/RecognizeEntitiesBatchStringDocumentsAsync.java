// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously recognize the entities of {@code String} documents.
 */
public class RecognizeEntitiesBatchStringDocumentsAsync {
    /**
     * Main method to invoke this demo about how to recognize the entities of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analyzed.
        List<String> documents = Arrays.asList(
            "Satya Nadella is the CEO of Microsoft.",
            "Elon Musk is the CEO of SpaceX and Tesla."
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Recognizing entities for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        client.recognizeEntitiesBatch(documents, "en", requestOptions).subscribe(
            recognizeEntitiesResultCollection -> {
                // Model version
                System.out.printf("Results of Azure Text Analytics \"Entities Recognition\" Model, version: %s%n", recognizeEntitiesResultCollection.getModelVersion());

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResultCollection.getStatistics();
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                    batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                for (RecognizeEntitiesResult entitiesResult : recognizeEntitiesResultCollection) {
                    System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
                    if (entitiesResult.isError()) {
                        // Erroneous document
                        System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
                    } else {
                        // Valid document
                        entitiesResult.getEntities().forEach(entity -> System.out.printf(
                            "Recognized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
                    }
                }
            },
            error -> System.err.println("There was an error recognizing entities of the documents." + error),
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
