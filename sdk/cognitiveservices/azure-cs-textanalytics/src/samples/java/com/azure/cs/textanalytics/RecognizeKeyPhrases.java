// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import textanalytics.models.DocumentKeyPhrases;
import java.util.List;

public class RecognizeKeyPhrases {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian";

        // Detecting language from a batch of documents
        DocumentKeyPhrases detectedResult = client.detectKeyPhrases(text, "US", false);
        List<String> phrases = detectedResult.getKeyPhrases();
        for (String phrase : phrases) {
            System.out.println(String.format("Recognized Phrases: %s", phrase));
        }
    }
}
