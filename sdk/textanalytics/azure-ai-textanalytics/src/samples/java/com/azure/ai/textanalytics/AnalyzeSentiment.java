// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to analyze the sentiment of document.
 */
public class AnalyzeSentiment {
    /**
     * Main method to invoke this demo about how to analyze the sentiment of document.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        // The text that needs be analyzed.
        String document = "The hotel was dark and unclean. I like Microsoft.";

        final DocumentSentiment documentSentiment = client.analyzeSentiment(document);
        SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
        System.out.printf(
            "Recognized document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
            documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());

        documentSentiment.getSentences().forEach(sentenceSentiment -> {
            SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
            System.out.printf("Recognized sentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
        });
    }
}
