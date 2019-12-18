// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.NamedEntity;

/**
 * Sample demonstrate how to recognize PII(personal information identification) entities of a text input.
 */
public class RecognizePii {
    /**
     * Main method to invoke this demo about how to analyze sentiment of a text input.
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
        String text = "My SSN is 555-55-5555";

        for (NamedEntity entity : client.recognizePiiEntities(text).getNamedEntities()) {
            System.out.printf(
                "Recognized PII Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s.%n",
                entity.getText(),
                entity.getType(),
                entity.getSubtype(),
                entity.getScore());
        }
    }
}
