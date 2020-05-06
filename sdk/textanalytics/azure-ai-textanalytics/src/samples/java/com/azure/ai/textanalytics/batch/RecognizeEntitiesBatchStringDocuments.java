// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeCategorizedEntitiesResult;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the entities of {@code String} documents.
 */
public class RecognizeEntitiesBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to recognize the entities of {@code String} documents.
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
            "Satya Nadella is the CEO of Microsoft.",
            "Elon Musk is the CEO of SpaceX and Tesla."
        );

        // Recognizing entities for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (RecognizeCategorizedEntitiesResult entitiesResult : client.recognizeEntitiesBatch(documents, "en")) {
            // Recognized entities for each of documents from a batch of documents
            System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
            } else {
                // Valid document
                entitiesResult.getEntities().forEach(entity -> System.out.printf(
                    "Recognized entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getConfidenceScore()));
            }
        }
    }
}
