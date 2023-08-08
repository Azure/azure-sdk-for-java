// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to rotate the existing API key of text analytics client
 */
public class RotateAzureKeyCredentialAsync {

    /**
     * Main method to invoke this demo about how to rotate the existing API key of text analytics client.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        AzureKeyCredential credential = new AzureKeyCredential("{key}");
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(credential)
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        // The document that needs be analyzed.
        String document = "My cat might need to see a veterinarian.";

        System.out.println("Extracted phrases:");
        client.extractKeyPhrases(document).subscribe(
            keyPhrasesCollection -> {
                for (String keyPhrase : keyPhrasesCollection) {
                    System.out.printf("%s.%n", keyPhrase);
                }
            },
            error -> System.err.println("There was an error extracting key phrases of the text." + error),
            () -> System.out.println("Key phrases extracted."));

        // Update the API key
        credential.update("{valid_api_key}");

        System.out.println("Extracted phrases:");
        client.extractKeyPhrases(document).subscribe(
            keyPhrasesCollection -> {
                for (String keyPhrase : keyPhrasesCollection) {
                    System.out.printf("%s.%n", keyPhrase);
                }
            },
            error -> System.err.println("There was an error extracting key phrases of the text." + error),
            () -> System.out.println("Key phrases extracted."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
