// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import textanalytics.models.DetectedLanguage;
import textanalytics.models.DocumentLanguage;
import textanalytics.models.LanguageResult;

import java.util.List;

public class HelloWorldAsync {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "hello world";

        client.detectLanguage(text, "US", false).subscribe(
            result -> {
              DocumentLanguage detectedLanguageResult = result;
              List<DetectedLanguage> detectedLanguages = detectedLanguageResult.getDetectedLanguage();
              for (DetectedLanguage detectedLanguage : detectedLanguages) {
                  System.out.println(String.format("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                      detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore()));
              }
            },
            error -> System.err.println("There was an error detecting language of the text" + error.toString()),
            () -> {
              System.out.println("Language detected.");
            });

    }
}
