// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.IterableStream;
import com.azure.cs.textanalytics.models.TextSentiment;
import com.azure.cs.textanalytics.models.TextSentimentResult;

public class AnalyzeSentiment {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();
        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        final TextSentimentResult sentimentResult = client.analyzeSentiment(text, "US");

        final TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
        System.out.printf(
            "Recognized TextSentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
            documentSentiment.getTextSentimentClass(),
            documentSentiment.getPositiveScore(),
            documentSentiment.getNeutralScore(),
            documentSentiment.getNegativeScore());

        final IterableStream<TextSentiment> sentiments = sentimentResult.getSentenceSentiments();
        sentiments.stream().forEach(textSentiment -> System.out.printf(
            "Recognized Sentence TextSentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
            textSentiment.getTextSentimentClass(),
            textSentiment.getPositiveScore(),
            textSentiment.getNeutralScore(),
            textSentiment.getNegativeScore()));
    }
}
