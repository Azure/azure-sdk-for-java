// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;

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
            .subscriptionKey(new TextAnalyticsSubscriptionKeyCredential("{subscription_key}"))
            .endpoint("https://{servicename}.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "My SSN is 555-55-5555";

        for (NamedEntity entity : client.recognizePiiEntities(text).getNamedEntities()) {
            System.out.printf(
                "Recognized personal identifiable information entity: %s, entity type: %s, entity subtype: %s, offset: %s, length: %s, score: %s.%n",
                entity.getText(),
                entity.getType(),
                entity.getSubtype() == null || entity.getSubtype().isEmpty() ? "N/A" : entity.getSubtype(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore());
        }
    }
}
