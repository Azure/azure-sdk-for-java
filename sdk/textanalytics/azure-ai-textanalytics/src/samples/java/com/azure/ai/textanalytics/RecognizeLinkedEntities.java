// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.LinkedEntity;

/**
 * Sample demonstrate how to recognize linked entities of a text input.
 */
public class RecognizeLinkedEntities {
    /**
     * Main method to invoke this demo about how to recognize linked entities of a text input.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        for (LinkedEntity linkedEntity : client.recognizeLinkedEntities(text).getLinkedEntities()) {
            System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
        }
    }

}
