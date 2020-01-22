// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.NamedEntity;
import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the PII(Personally Identifiable Information) entities of an input
 * text.
 */
public class RecognizePiiAsync {
    /**
     * Main method to invoke this demo about how to recognize the PII entities of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsSubscriptionKeyCredential("{subscription_key}"))
            .endpoint("https://{servicename}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "My SSN is 555-55-5555";

        client.recognizePiiEntities(text).subscribe(
            result -> {
                for (NamedEntity entity : result.getNamedEntities()) {
                    System.out.printf(
                        "Recognized personal identifiable information entity: %s, entity type: %s, entity subtype: %s, offset: %s, length: %s, score: %s.%n",
                        entity.getText(),
                        entity.getType(),
                        entity.getSubtype() == null || entity.getSubtype().isEmpty() ? "N/A" : entity.getSubtype(),
                        entity.getOffset(),
                        entity.getLength(),
                        entity.getScore());
                }
            },
            error -> System.err.println("There was an error recognizing PII entities of the text." + error),
            () -> System.out.println("PII entities recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
