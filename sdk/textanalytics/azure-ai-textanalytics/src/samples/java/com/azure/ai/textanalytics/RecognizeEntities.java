// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

public class RecognizeEntities {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "Satya Nadella is the CEO of Microsoft";

        client.recognizeEntities(text).getNamedEntities().forEach(
            entity -> System.out.printf(
                "Recognized NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                entity.getText(),
                entity.getType(),
                entity.getSubtype(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore()));
    }
}
