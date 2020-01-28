// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
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
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        final DetectLanguageResult detectLanguageResult = client.detectLanguage(text);
        final DetectedLanguage detectedPrimaryLanguage = detectLanguageResult.getPrimaryLanguage();
        System.out.printf("Detected primary language: %s, ISO 6391 name: %s, score: %s.%n",
            detectedPrimaryLanguage.getName(),
            detectedPrimaryLanguage.getIso6391Name(),
            detectedPrimaryLanguage.getScore());

        for (DetectedLanguage detectedLanguage : detectLanguageResult.getDetectedLanguages()) {
            System.out.printf("Another detected language: %s, ISO 6391 name: %s, score: %s.%n",
                detectedLanguage.getName(),
                detectedLanguage.getIso6391Name(),
                detectedLanguage.getScore());
        }
    }
}
