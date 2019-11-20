// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

public class RecognizeLinkedEntities {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The text that need be analysed.
        String text = "Old Faithful is a geyser at Yellowstone Park";

        client.recognizeLinkedEntities(text, "US").stream().forEach(
            linkedEntity -> System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s",
                linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource())));
    }
}
