// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to recognize the PII(Personally Identifiable Information) entities of documents.
 */
public class RecognizePiiBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of
     * documents.
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
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "My SSN is 555-55-5555", "en"),
            new TextDocumentInput("2", "Visa card 4111 1111 1111 1111", "en")
        );

        // Recognizing batch entities
        client.recognizePiiEntitiesBatch(inputs, null, Context.NONE).forEach(entitiesResult -> {
            // Recognized Personally Identifiable Information entities for each of documents from a batch of documents
            System.out.printf("%nDocument ID: %s%n", entitiesResult.getId());
            if (entitiesResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot recognize Personally Identifiable Information entities. Error: %s%n", entitiesResult.getError().getMessage());
            } else {
                // Valid document
                entitiesResult.getEntities().forEach(entity -> System.out.printf(
                    "Recognized personal identifiable information entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
                    entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getConfidenceScore()));
            }
        });
    }
}
