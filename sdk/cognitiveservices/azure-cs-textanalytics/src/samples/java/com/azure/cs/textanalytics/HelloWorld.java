// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import textanalytics.models.DetectedLanguage;
import textanalytics.models.DocumentLanguage;

import java.util.List;

public class HelloWorld {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The text that need be analysed.
        String text = "hello world";

        final DocumentLanguage detectedResult = client.detectLanguage(text, "US", false);
        List<DetectedLanguage> detectedLanguages = detectedResult.getDetectedLanguage();
        for (DetectedLanguage detectedLanguage : detectedLanguages) {
            System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
        }
    }
}
