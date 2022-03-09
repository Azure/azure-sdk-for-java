// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the linked entities of {@code String} documents.
 */
public class RecognizeLinkedEntitiesBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> documents = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Recognizing linked entities for each document in a batch of documents
        RecognizeLinkedEntitiesResultCollection linkedEntitiesResultCollection = client.recognizeLinkedEntitiesBatch(documents, "en", requestOptions);

        // Model version
        System.out.printf("Results of Azure Text Analytics \"Linked Entities Recognition\" Model, version: %s%n", linkedEntitiesResultCollection.getModelVersion());

        // Batch statistics
        TextDocumentBatchStatistics batchStatistics = linkedEntitiesResultCollection.getStatistics();
        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

        // Recognized linked entities from a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (RecognizeLinkedEntitiesResult entitiesResult : linkedEntitiesResultCollection) {
            System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
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
    }
}
