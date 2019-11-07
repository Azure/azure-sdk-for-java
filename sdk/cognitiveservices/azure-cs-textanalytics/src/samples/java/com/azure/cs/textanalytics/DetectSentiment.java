// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import textanalytics.models.DocumentSentiment;

public class DetectSentiment {

    public static void main(String[] args) {
        // The connection string value can be obtained by going to your Text Analytics instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        // Detecting sentiment form a single text
        DocumentSentiment documentSentiment = client.detectSentiment(text, "US", false);
        final String sentiment = documentSentiment.getSentiment();
        final Double documentScore = (Double) documentSentiment.getDocumentScores();
        System.out.println(String.format("Recognized Sentiment: %s, Document Score: %s", sentiment, documentScore));
    }
}
