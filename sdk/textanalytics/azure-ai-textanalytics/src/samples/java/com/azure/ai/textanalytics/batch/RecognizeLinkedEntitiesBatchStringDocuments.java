// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
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
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> inputs = Arrays.asList(
            "Old Faithful is a geyser at Yellowstone Park.",
            "Mount Shasta has lenticular clouds."
        );

        // Recognizing batch entities
        AtomicInteger counter = new AtomicInteger();
        client.recognizeLinkedEntitiesBatch(inputs).forEach(entitiesResult -> {
            // Recognized linked entities from a batch of documents
            System.out.printf("%nDocument: %s%n", inputs.get(counter.getAndIncrement()));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize linked entities. Error: %s%n", entitiesResult.getError().getMessage());
                return;
            }
            // Valid document
            entitiesResult.getEntities().forEach(entity -> {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                    entity.getName(), entity.getDataSourceEntityId(), entity.getUrl(), entity.getDataSource());
                entity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                    "Matched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
            });
        });
    }
}
