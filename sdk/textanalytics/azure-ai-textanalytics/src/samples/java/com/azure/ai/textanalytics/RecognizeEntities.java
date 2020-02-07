// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

/**
 * Sample demonstrates how to recognize the entities of an input text.
 */
public class RecognizeEntities {
    /**
     * Main method to invoke this demo about how to recognize the entities of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "Satya Nadella is the CEO of Microsoft";

        for (CategorizedEntity entity : client.recognizeEntities(text)) {
            System.out.printf(
                "Recognized entity: %s, entity category: %s, entity sub-category: %s, offset: %s, length: %s, score: %s.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore());
        }
    }
}
