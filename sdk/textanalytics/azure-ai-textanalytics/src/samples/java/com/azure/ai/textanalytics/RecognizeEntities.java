// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to recognize the entities of document.
 */
public class RecognizeEntities {
    /**
     * Main method to invoke this demo about how to recognize the entities of document.
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
        String document = "Satya Nadella is the CEO of Microsoft";

        client.recognizeEntities(document).forEach(entity -> System.out.printf(
            "Recognized categorized entity: %s, entity category: %s, entity subcategory: %s, confidence score: %f.%n",
            entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
    }
}
