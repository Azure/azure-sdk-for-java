// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

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
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that needs be analyzed.
        String text = "Old Faithful is a geyser at Yellowstone Park.";

        client.recognizeLinkedEntities(text).forEach(linkedEntity -> {

            System.out.println("Linked Entities:");
            System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                linkedEntity.getName(),
                linkedEntity.getDataSourceEntityId(),
                linkedEntity.getUrl(),
                linkedEntity.getDataSource());
            linkedEntity.getLinkedEntityMatches().forEach(linkedEntityMatch ->
                System.out.printf("(Linked Entity Match) Text: %s, offset: %s, length: %s, score: %.2f.%n",
                    linkedEntityMatch.getText(),
                    linkedEntityMatch.getOffset(),
                    linkedEntityMatch.getLength(),
                    linkedEntityMatch.getScore()));
        });
    }
}
