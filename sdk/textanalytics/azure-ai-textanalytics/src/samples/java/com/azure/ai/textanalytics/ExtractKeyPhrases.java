// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

/**
 * Sample demonstrate how to analyze key phrases of a text input.
 */
public class ExtractKeyPhrases {
    /**
     * Main method to invoke this demo about how to extract key phrases of a text input.
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
        String text = "My cat might need to see a veterinarian.";

        for (String keyPhrase : client.extractKeyPhrases(text).getKeyPhrases()) {
            System.out.printf("Recognized Phrases: %s.%n", keyPhrase);
        }
    }
}
