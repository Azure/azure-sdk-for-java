// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the linked entities of document.
 */
public class RecognizeLinkedEntitiesAsync {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of document.
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
        String document = "Old Faithful is a geyser at Yellowstone Park.";

        client.recognizeLinkedEntities(document).subscribe(
            linkedEntityCollection -> linkedEntityCollection.forEach(linkedEntity -> {
                System.out.println("Linked Entities:");
                System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s, "
                        + "Bing Entity Search API ID: %s.%n",
                    linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(),
                    linkedEntity.getDataSource(), linkedEntity.getBingEntitySearchApiId());
                linkedEntity.getMatches().forEach(entityMatch -> System.out.printf(
                    "Matched entity: %s, confidence score: %f.%n",
                    entityMatch.getText(), entityMatch.getConfidenceScore()));
            }),
            error -> System.err.println("There was an error recognizing linked entity of the text." + error),
            () -> System.out.println("Linked entity recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
