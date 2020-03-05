// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the entities of a batch input text.
 */
public class RecognizedCategorizedEntitiesBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to recognize the entities of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<String> inputs = Arrays.asList(
            "Satya Nadella is the CEO of Microsoft.",
            "Elon Musk is the CEO of SpaceX and Tesla."
        );

        // Recognizing batch entities
        client.recognizeEntitiesBatch(inputs).forEach(entitiesResult -> {
            // Recognized entities for each of documents from a batch of documents
            System.out.printf("%nDocument ID: %s%n", entitiesResult.getId());
            System.out.printf("Input text: %s%n", inputs.get(Integer.parseInt(entitiesResult.getId())));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize entities. Error: %s%n", entitiesResult.getError().getMessage());
                return;
            }
            // Valid document
            entitiesResult.getEntities().forEach(entity -> System.out.printf(
                "Recognized categorized entity: %s, entity category: %s, entity sub-category: %s, offset: %s, length: %s, score: %.2f.%n",
                entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getGraphemeOffset(), entity.getGraphemeLength(), entity.getScore()));
        });
    }
}
