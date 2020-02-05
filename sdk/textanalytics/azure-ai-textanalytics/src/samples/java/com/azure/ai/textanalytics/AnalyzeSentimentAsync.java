// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.SentenceSentiment;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously analyze the sentiment of an input text.
 */
public class AnalyzeSentimentAsync {
    /**
     * Main method to invoke this demo about how to analyze the sentiment of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        client.analyzeSentiment(text).subscribe(
            documentSentiment -> {
                System.out.printf(
                    "Recognized sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                    documentSentiment.getSentimentLabel(),
                    documentSentiment.getSentimentScorePerLabel().getPositiveScore(),
                    documentSentiment.getSentimentScorePerLabel().getNeutralScore(),
                    documentSentiment.getSentimentScorePerLabel().getNegativeScore());

                for (SentenceSentiment sentenceSentiment : documentSentiment.getSentenceSentiments()) {
                    System.out.printf(
                        "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                        sentenceSentiment.getSentimentLabel(),
                        sentenceSentiment.getSentimentScorePerLabel().getPositiveScore(),
                        sentenceSentiment.getSentimentScorePerLabel().getNeutralScore(),
                        sentenceSentiment.getSentimentScorePerLabel().getNegativeScore());
                }
            },
            error -> System.err.println("There was an error analyzing sentiment of the text." + error),
            () -> System.out.println("Sentiment analyzed."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
