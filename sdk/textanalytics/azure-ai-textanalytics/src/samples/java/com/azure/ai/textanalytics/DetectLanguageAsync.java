// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously detect the language of document.
 */
public class DetectLanguageAsync {
    /**
     * Main method to invoke this demo about how to detect the language of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        // The document that needs be analyzed.
        String document = "hello world";

        client.detectLanguage(document).subscribe(
            result -> System.out.printf("Detected primary language: %s, ISO 6391 name: %s, confidence score: %f.%n",
                result.getName(), result.getIso6391Name(), result.getConfidenceScore()),
            error -> System.err.println("There was an error detecting language of the text." + error),
            () -> System.out.println("Language detected."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
