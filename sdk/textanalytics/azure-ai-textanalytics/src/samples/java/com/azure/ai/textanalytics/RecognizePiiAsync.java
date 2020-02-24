// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously recognize the PII(Personally Identifiable Information) entities of an input
 * text.
 */
public class RecognizePiiAsync {
    /**
     * Main method to invoke this demo about how to recognize the Personally Identifiable Information entities of
     * an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The text that needs be analyzed.
        String text = "My SSN is 555-55-5555";

        client.recognizePiiEntities(text).subscribe(
            entity -> System.out.printf(
                "Recognized personal identifiable information entity: %s, entity category: %s, entity sub-category: %s, offset: %s, length: %s, score: %.2f.%n",
                entity.getText(),
                entity.getCategory(),
                entity.getSubCategory() == null || entity.getSubCategory().isEmpty() ? "N/A" : entity.getSubCategory(),
                entity.getOffset(),
                entity.getLength(),
                entity.getScore()),
            error -> System.err.printf(
                "There was an error recognizing Personally Identifiable Information entities of the text. %s%n", error),
            () -> System.out.println("Personally Identifiable Information entities recognized."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
