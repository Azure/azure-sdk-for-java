// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to rotate the existing subscription key of text analytics client
 */
public class RotateSubscriptionKeyAsync {

    /**
     * Main method to invoke this demo about how to rotate the existing subscription key of text analytics client.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsApiKeyCredential credential = new TextAnalyticsApiKeyCredential("{invalid_subscription_key}");
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(credential)
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "My cat might need to see a veterinarian.";

        client.extractKeyPhrases(text).subscribe(
            result -> {
                for (String keyPhrase : result.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            },
            error -> System.err.println("There was an error extracting key phrases of the text." + error),
            () -> System.out.println("Key phrases extracted."));

        // Update the subscription key
        credential.updateCredential("{valid_subscription_key}");

        client.extractKeyPhrases(text).subscribe(
            result -> {
                for (String keyPhrase : result.getKeyPhrases()) {
                    System.out.printf("Recognized phrases: %s.%n", keyPhrase);
                }
            },
            error -> System.err.println("There was an error extracting key phrases of the text." + error),
            () -> System.out.println("Key phrases extracted."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
