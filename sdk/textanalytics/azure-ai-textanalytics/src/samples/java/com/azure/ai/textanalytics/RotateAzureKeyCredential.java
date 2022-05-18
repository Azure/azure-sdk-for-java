// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;

/**
 * Sample demonstrates how to rotate the existing API key of text analytics client
 */
public class RotateAzureKeyCredential {

    /**
     * Main method to invoke this demo about how to rotate the existing API key of text analytics client.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        AzureKeyCredential credential = new AzureKeyCredential("{key}");
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(credential)
                                         .endpoint("{endpoint}")
                                         .buildClient();

        // The document that needs be analyzed.
        String document = "My cat might need to see a veterinarian.";

        try {
            client.extractKeyPhrases(document);
        } catch (HttpResponseException ex) {
            System.out.println(ex.getMessage());
        }

        // Update the API key
        credential.update("{valid_api_key}");

        System.out.println("Extracted phrases:");
        for (String keyPhrase : client.extractKeyPhrases(document)) {
            System.out.printf("%s.%n", keyPhrase);
        }
    }
}
