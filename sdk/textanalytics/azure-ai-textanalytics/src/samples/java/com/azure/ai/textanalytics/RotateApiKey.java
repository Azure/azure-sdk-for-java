// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.core.exception.HttpResponseException;

/**
 * Sample demonstrates how to rotate the existing API key of text analytics client
 */
public class RotateApiKey {

    /**
     * Main method to invoke this demo about how to rotate the existing API key of text analytics client.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential("{api_key}");
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(credential)
            .endpoint("{endpoint}")
            .buildClient();

        // The text that needs be analyzed.
        String text = "My cat might need to see a veterinarian.";

        try {
            client.extractKeyPhrases(text);
        } catch (HttpResponseException ex) {
            System.out.println(ex.getMessage());
        }

        // Update the API key
        credential.updateCredential("{valid_api_key}");

        System.out.println("Extracted phrases:");
        for (String keyPhrase : client.extractKeyPhrases(text)) {
            System.out.printf("%s.%n", keyPhrase);
        }
    }
}
