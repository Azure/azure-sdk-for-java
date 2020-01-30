// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

/**
 * Sample demonstrates how to extract the key phrases of an input text.
 */
public class ExtractKeyPhrases {
    /**
     * Main method to invoke this demo about how to extract the key phrases of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian.";

        for (String keyPhrase : client.extractKeyPhrases(text).getKeyPhrases()) {
            System.out.printf("Recognized phrases: %s.%n", keyPhrase);
        }
    }
}
