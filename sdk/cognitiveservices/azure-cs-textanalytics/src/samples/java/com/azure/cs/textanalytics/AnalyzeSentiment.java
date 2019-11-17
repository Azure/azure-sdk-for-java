// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.cs.textanalytics.implementation.models.DocumentSentiment;

public class AnalyzeSentiment {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();
        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        DocumentSentiment documentSentiment = client.analyzeSentiment(text, "US", false);
        final String sentiment = documentSentiment.getSentiment();
        final Double documentScore = (Double) documentSentiment.getDocumentScores();
        System.out.println(String.format("Recognized Sentiment: %s, Document Score: %s", sentiment, documentScore));
    }
}
