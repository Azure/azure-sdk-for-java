// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to recognize the Personally Identifiable Information entities of document.
 */
public class RecognizePiiEntitiesAsync {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of document.
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
        String document = "My SSN is 859-98-0987";

        client.recognizePiiEntities(document).subscribe(
            entityCollection -> entityCollection.forEach(entity -> System.out.printf(
                "Recognized Personal Identifiable Information entity: %s, entity category: %s, entity sub-category: %s, score: %f.%n",
                entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore())),
            error -> System.err.println("There was an error recognizing PII entities of the text." + error),
            () -> System.out.println("Entities recognized.")
        );

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
