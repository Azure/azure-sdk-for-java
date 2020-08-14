// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;


import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to recognize the Personally Identifiable Information entities of document.
 */
public class RecognizePiiEntities {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of
     * document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The document that needs be analyzed.
        String document = "My SSN is 859-98-0987";

        client.recognizePiiEntities(document).forEach(entity -> System.out.printf(
            "Recognized Personal Identifiable Information entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
    }
}
