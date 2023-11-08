// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously analyze the sentiment of document.
 */
public class AnalyzeSentimentAsync {
    /**
     * Main method to invoke this demo about how to analyze the sentiment of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        // The document that needs be analyzed.
        String document = "The hotel was dark and unclean. I like Microsoft";

        client.analyzeSentiment(document).subscribe(
            documentSentiment -> {
                SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
                System.out.printf(
                    "Recognized document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                    documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());

                documentSentiment.getSentences().forEach(sentenceSentiment -> {
                    SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                        sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
                });
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
