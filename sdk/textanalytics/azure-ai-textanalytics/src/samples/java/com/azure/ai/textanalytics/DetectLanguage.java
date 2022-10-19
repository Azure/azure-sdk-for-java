// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to detect the language of document.
 */
public class DetectLanguage {
    /**
     * Main method to invoke this demo about how to detect the language of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        // The document that needs be analyzed.
        String document = "hello world";

        final DetectedLanguage detectedLanguage = client.detectLanguage(document);
        System.out.printf("Detected primary language: %s, ISO 6391 name: %s, confidence score: %f.%n",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore());
    }
}
