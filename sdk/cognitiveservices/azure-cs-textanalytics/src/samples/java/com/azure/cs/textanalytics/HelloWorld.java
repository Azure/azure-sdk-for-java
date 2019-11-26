// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.IterableStream;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DetectedLanguageResult;

public class HelloWorld {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        final DetectedLanguageResult detectedLanguageResult = client.detectLanguage(text, "US");
        final DetectedLanguage detectedDocumentLanguage = detectedLanguageResult.getPrimaryLanguage();
        System.out.printf("Detected Primary Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedDocumentLanguage.getName(),
            detectedDocumentLanguage.getIso6391Name(),
            detectedDocumentLanguage.getScore());

        final IterableStream<DetectedLanguage> detectedLanguages = detectedLanguageResult.getDetectedLanguages();
        detectedLanguages.stream().forEach(detectedLanguage ->
            System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedLanguage.getName(),
            detectedLanguage.getIso6391Name(),
            detectedLanguage.getScore()));
    }
}
