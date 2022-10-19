// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.textanalytics;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.core.credential.AzureKeyCredential;

/**
 * A GraalVM sample to demonstrate analyzing sentiment of a sentence using Azure Text Analytics.
 */
public class TextAnalyticsSample {
    private static final String AZURE_TEXT_ANALYTICS_KEY = System.getenv("AZURE_TEXT_ANALYTICS_KEY");
    private static final String AZURE_TEXT_ANALYTICS_ENDPOINT = System.getenv("AZURE_TEXT_ANALYTICS_ENDPOINT");

    /**
     * The method to run the text analytics sample.
     */
    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting Text Analytics Sample");
        System.out.println("================================================================");

        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential(AZURE_TEXT_ANALYTICS_KEY))
                .endpoint(AZURE_TEXT_ANALYTICS_ENDPOINT)
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

        System.out.println("\n================================================================");
        System.out.println(" Text Analytics Sample Complete");
        System.out.println("================================================================");
    }
}
