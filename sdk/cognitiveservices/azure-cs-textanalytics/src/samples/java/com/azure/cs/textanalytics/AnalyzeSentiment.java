// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.implementation.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.Sentiment;
import com.azure.cs.textanalytics.models.SentimentClass;

public class AnalyzeSentiment {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();
        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        Sentiment sentenceSentiment = client.analyzeSentenceSentiment(text, "US");
        final SentimentClass sentiment = sentenceSentiment.getSentimentClass();
        final double positiveScore = sentenceSentiment.getPositiveScore();
        final double neutralScore = sentenceSentiment.getNeutralScore();
        final double negativeScore = sentenceSentiment.getNegativeScore();

        System.out.println(String.format(
            "Recognized Sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
            sentiment, positiveScore, neutralScore, negativeScore));
    }
}
