// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;

import java.util.List;

/**
 * Sample demonstrate how to analyze sentiment of a text input.
 */
public class AnalyzeSentiment {
    /**
     * Main method to invoke this demo about how to analyze sentiment of a text input.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        final AnalyzeSentimentResult sentimentResult = client.analyzeSentiment(text);

        final TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
        System.out.printf(
            "Recognized TextSentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.%n",
            documentSentiment.getTextSentimentClass(),
            documentSentiment.getPositiveScore(),
            documentSentiment.getNeutralScore(),
            documentSentiment.getNegativeScore());

        final List<TextSentiment> sentiments = sentimentResult.getSentenceSentiments();
        for (TextSentiment textSentiment : sentiments) {
            System.out.printf(
                "Recognized Sentence TextSentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.%n",
                textSentiment.getTextSentimentClass(),
                textSentiment.getPositiveScore(),
                textSentiment.getNeutralScore(),
                textSentiment.getNegativeScore());
        }
    }
}
