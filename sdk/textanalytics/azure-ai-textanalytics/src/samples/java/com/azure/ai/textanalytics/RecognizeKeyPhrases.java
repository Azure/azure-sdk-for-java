// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.util.Configuration;

public class RecognizeKeyPhrases {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
//            .subscriptionKey("subscription-key")
//            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .subscriptionKey(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY"))
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
            .buildClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian";

        client.extractKeyPhrases(text).getKeyPhrases().stream().forEach(
            phrase -> System.out.printf("Recognized Phrases: %s", phrase));
    }
}
