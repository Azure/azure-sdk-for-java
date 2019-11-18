// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.models.DetectedLanguage;

public class HelloWorld {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        DetectedLanguage detectedLanguage = client.detectLanguage(text, "US");
        System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
    }
}
