// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

public class RecognizeKeyPhrases {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian";

        client.extractKeyPhrases(text, "US").getKeyPhrases().stream().forEach(
            phrase -> System.out.printf("Recognized Phrases: %s", phrase));
    }
}
