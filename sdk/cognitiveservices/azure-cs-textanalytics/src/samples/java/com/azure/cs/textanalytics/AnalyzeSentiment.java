// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.models.TextSentiment;
import com.azure.cs.textanalytics.models.TextSentimentClass;

public class AnalyzeSentiment {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();
        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        TextSentiment sentenceTextSentiment = client.analyzeSentenceSentiment(text, "US");
        final TextSentimentClass sentiment = sentenceTextSentiment.getTextSentimentClass();
        final double positiveScore = sentenceTextSentiment.getPositiveScore();
        final double neutralScore = sentenceTextSentiment.getNeutralScore();
        final double negativeScore = sentenceTextSentiment.getNegativeScore();

        System.out.printf(
            "Recognized TextSentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
            sentiment, positiveScore, neutralScore, negativeScore));
    }
}
