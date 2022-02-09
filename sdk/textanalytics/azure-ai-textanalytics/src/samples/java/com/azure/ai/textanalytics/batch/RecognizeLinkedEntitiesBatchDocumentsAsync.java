// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously recognize the linked entities of {@link TextDocumentInput} documents.
 */
public class RecognizeLinkedEntitiesBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of {@link TextDocumentInput} documents.
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
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("A", "Old Faithful is a geyser at Yellowstone Park.").setLanguage("en"),
            new TextDocumentInput("B", "Mount Shasta has lenticular clouds.").setLanguage("en")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Recognizing linked entities for each document in a batch of documents
        client.recognizeLinkedEntitiesBatchWithResponse(documents, requestOptions).subscribe(
            linkedEntitiesBatchResultResponse -> {
                // Response's status code
                System.out.printf("Status code of request response: %d%n", linkedEntitiesBatchResultResponse.getStatusCode());
                RecognizeLinkedEntitiesResultCollection linkedEntitiesResultCollection = linkedEntitiesBatchResultResponse.getValue();

                // Model version
                System.out.printf("Results of Azure Text Analytics \"Linked Entities Recognition\" Model, version: %s%n", linkedEntitiesResultCollection.getModelVersion());

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = linkedEntitiesResultCollection.getStatistics();
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                    batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Recognized linked entities from a batch of documents
                AtomicInteger counter = new AtomicInteger();
                for (RecognizeLinkedEntitiesResult entitiesResult : linkedEntitiesResultCollection) {
                    System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                    if (entitiesResult.isError()) {
                        // Erroneous document
                        System.out.printf("Cannot recognize linked entities. Error: %s%n", entitiesResult.getError().getMessage());
                    } else {
                        // Valid document
                        entitiesResult.getEntities().forEach(linkedEntity -> {
                            System.out.println("Linked Entities:");
                            System.out.printf("\tName: %s, entity ID in data source: %s, URL: %s, data source: %s,"
                                    + " Bing Entity Search API ID: %s.%n",
                                linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                                linkedEntity.getDataSource(), linkedEntity.getBingEntitySearchApiId());
                            linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                                "\tMatched entity: %s, confidence score: %f.%n",
                                entityMatch.getText(), entityMatch.getConfidenceScore()));
                        });
                    }
                }
            },
            error -> System.err.println("There was an error recognizing linked entities of the documents." + error),
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
