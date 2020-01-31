// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.core.exception.HttpResponseException;

/**
 * Sample demonstrates how to rotate the existing subscription key of text analytics client
 */
public class RotateSubscriptionKey {

    /**
     * Main method to invoke this demo about how to rotate the existing subscription key of text analytics client.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential("{invalid_subscription_key}");
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian.";

        try {
            client.extractKeyPhrases(text);
        } catch (HttpResponseException ex) {
            System.out.println(ex.getMessage());
        }

        // Update the subscription key
        credential.updateCredential("{valid_subscription_key}");

        for (String keyPhrase : client.extractKeyPhrases(text).getKeyPhrases()) {
            System.out.printf("Recognized phrases: %s.%n", keyPhrase);
        }
    }
}
