// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the linked entities of an input text.
 */
public class RecognizeLinkedEntitiesAsync {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        client.recognizeLinkedEntities(text).subscribe(
            result -> {
                for (LinkedEntity linkedEntity : result.getLinkedEntities()) {
                    System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                        linkedEntity.getName(),
                        linkedEntity.getUrl(),
                        linkedEntity.getDataSource());
                }
            },
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
