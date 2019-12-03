// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DetectLanguageResult;

import java.util.List;

public class HelloWorld {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscriptionKey")
            .endpoint("https://service.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        final DetectLanguageResult detectLanguageResult = client.detectLanguage(text, "US");
        final DetectedLanguage detectedDocumentLanguage = detectLanguageResult.getPrimaryLanguage();
        System.out.printf("Detected Primary Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedDocumentLanguage.getName(),
            detectedDocumentLanguage.getIso6391Name(),
            detectedDocumentLanguage.getScore());

        final List<DetectedLanguage> detectedLanguages = detectLanguageResult.getDetectedLanguages();
        detectedLanguages.forEach(detectedLanguage ->
            System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedLanguage.getName(),
            detectedLanguage.getIso6391Name(),
            detectedLanguage.getScore()));
    }
}
