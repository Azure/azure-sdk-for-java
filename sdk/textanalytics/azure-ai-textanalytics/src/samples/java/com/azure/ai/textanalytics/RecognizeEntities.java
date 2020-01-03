// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.NamedEntity;

/**
 * Sample demonstrate how to recognize entities of a text input.
 */
public class RecognizeEntities {
    /**
     * Main method to invoke this demo about how to recognize entities of a text input.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "Satya Nadella is the CEO of Microsoft";

        for (NamedEntity entity : client.recognizeEntities(text).getNamedEntities()) {
            System.out.printf(
                "Recognized NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s.%n",
                entity.getText(),
                entity.getType(),
                entity.getSubtype() == null || entity.getSubtype().isEmpty() ? "N/A" : entity.getSubtype(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore());
        }
    }
}
