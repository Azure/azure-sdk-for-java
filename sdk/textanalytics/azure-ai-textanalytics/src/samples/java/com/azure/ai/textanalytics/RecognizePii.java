// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

/**
 * Sample demonstrates how to recognize the PII(Personally Identifiable Information) entities of an input text.
 */
public class RecognizePii {
    /**
     * Main method to invoke this demo about how to recognize the PII entities of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "My SSN is 555-55-5555";

        for (PiiEntity entity : client.recognizePiiEntities(text).getEntities()) {
            System.out.printf(
                "Recognized personal identifiable information entity: %s, entity Category: %s, entity Sub-category: %s, offset: %s, length: %s, score: %s.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore());
        }
    }
}
