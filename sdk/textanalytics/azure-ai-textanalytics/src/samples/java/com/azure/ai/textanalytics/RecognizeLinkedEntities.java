// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

public class RecognizeLinkedEntities {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park";

        client.recognizeLinkedEntities(text).getLinkedEntities().forEach(
            linkedEntity -> System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s",
                linkedEntity.getName(),
                linkedEntity.getUri(),
                linkedEntity.getDataSource()));
    }
}
