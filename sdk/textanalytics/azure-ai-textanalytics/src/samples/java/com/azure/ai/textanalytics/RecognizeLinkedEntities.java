// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.LinkedEntity;

/**
 * Sample demonstrates how to recognize the linked entities of an input text.
 */
public class RecognizeLinkedEntities {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("{subscription_key}")
            .endpoint("https://{servicename}.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        for (LinkedEntity linkedEntity : client.recognizeLinkedEntities(text).getLinkedEntities()) {
            System.out.printf("Recognized linked entity: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
        }
    }

}
