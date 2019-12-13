// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.core.util.Configuration;

import java.util.List;

public class HelloWorld {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
//            .subscriptionKey("subscription-key")
//            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .subscriptionKey(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_SUBSCRIPTION_KEY"))
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        final DetectLanguageResult detectLanguageResult = client.detectLanguage(text, "US");
        final DetectedLanguage detectedDocumentLanguage = detectLanguageResult.getPrimaryLanguage();
        System.out.printf("Detected Primary Language: %s, ISO 6391 Name: %s, Score: %s%n",
            detectedDocumentLanguage.getName(),
            detectedDocumentLanguage.getIso6391Name(),
            detectedDocumentLanguage.getScore());

        final List<DetectedLanguage> detectedLanguages = detectLanguageResult.getDetectedLanguages();
        detectedLanguages.forEach(detectedLanguage ->
            System.out.printf("Other detected languages: %s, ISO 6391 Name: %s, Score: %s%n",
            detectedLanguage.getName(),
            detectedLanguage.getIso6391Name(),
            detectedLanguage.getScore()));
    }
}
