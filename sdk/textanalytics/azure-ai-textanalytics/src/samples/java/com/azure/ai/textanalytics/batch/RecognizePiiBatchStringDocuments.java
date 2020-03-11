// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the PII(Personally Identifiable Information) entities of a batch document.
 */
public class RecognizePiiBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of a
     * batch of documents.
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
            "My SSN is 555-55-5555",
            "Visa card 4111 1111 1111 1111"
        );

        // Recognizing batch entities
        client.recognizePiiEntitiesBatch(inputs).forEach(entitiesResult -> {
            // Recognized Personally Identifiable Information entities for each of documents from a batch of documents
            System.out.printf("%nDocument ID: %s%n", entitiesResult.getId());
            System.out.printf("Document: %s%n", inputs.get(Integer.parseInt(entitiesResult.getId())));
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n",
                    entitiesResult.getError().getMessage());
                return;
            }
            // Valid document
            entitiesResult.getEntities().forEach(entity -> System.out.printf(
                "PII entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getConfidenceScore()));
        });
    }
}
