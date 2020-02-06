// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;

/**
 * Sample demonstrates how to analyze the sentiment of an input text.
 */
public class AnalyzeSentiment {
    /**
     * Main method to invoke this demo about how to analyze the sentiment of an input text.
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
        String text = "The hotel was dark and unclean.";

        final DocumentSentiment documentSentiment = client.analyzeSentiment(text);
        System.out.printf(
            "Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
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
    }
}
