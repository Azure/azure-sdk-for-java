// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to extract the key phrases of document.
 */
public class ExtractKeyPhrases {
    /**
     * Main method to invoke this demo about how to extract the key phrases of document.
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
        String document = "My cat might need to see a veterinarian.";

        System.out.println("Extracted phrases:");
        client.extractKeyPhrases(document).forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
    }
}
