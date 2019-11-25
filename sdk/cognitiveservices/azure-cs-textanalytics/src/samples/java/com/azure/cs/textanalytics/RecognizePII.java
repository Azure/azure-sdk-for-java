// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

public class RecognizePII {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The text that need be analysed.
        String text = "My SSN is 555-55-5555";

        client.recognizePiiEntities(text, "US").getItems().stream().forEach(
            entity -> System.out.printf(
                "Recognized PII NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                entity.getText(),
                entity.getType(),
                entity.getSubType(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore()));
    }
}
