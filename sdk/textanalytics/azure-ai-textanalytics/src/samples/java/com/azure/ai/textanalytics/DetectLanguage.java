// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

/**
 * Sample demonstrates how to detect the language of an input text.
 */
public class DetectLanguage {
    /**
     * Main method to invoke this demo about how to detect the language of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that needs be analyzed.
        String text = "hello world";

        final DetectedLanguage detectedLanguage = client.detectLanguage(text);
        System.out.printf("Detected primary language: %s, ISO 6391 name: %s, score: %.2f.%n",
            detectedLanguage.getName(),
            detectedLanguage.getIso6391Name(),
            detectedLanguage.getScore());
    }
}
