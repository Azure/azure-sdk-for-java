// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Sample demonstrates how to analyze the sentiments of {@code String} documents.
 */
public class AnalyzeSentimentBatchStringDocuments {
    /**
     * Main method to invoke this demo about how to analyze the sentiments of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The documents that need to be analyzed.
        List<String> inputs = Arrays.asList(
            "The hotel was dark and unclean. I wouldn't recommend staying there.",
            "The restaurant had amazing gnocchi! The waiters were excellent.",
            "The hotel was dark and unclean. The restaurant had amazing gnocchi!"
        );

        // Analyzing batch sentiments
        AtomicInteger counter = new AtomicInteger();
        client.analyzeSentimentBatch(inputs).forEach(analyzeSentimentResult -> {
            System.out.printf("%nDocument: %s%n", inputs.get(counter.getAndIncrement()));
            if (analyzeSentimentResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot analyze sentiment. Error: %s%n", analyzeSentimentResult.getError().getMessage());
                return;
            }
            // Valid document
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            SentimentConfidenceScores documentScores = documentSentiment.getConfidenceScores();
            System.out.printf("Document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                documentSentiment.getSentiment(), documentScores.getPositive(), documentScores.getNeutral(), documentScores.getNegative());
            // Each sentence sentiment
            documentSentiment.getSentences().forEach(sentiment -> {
                SentimentConfidenceScores sentencesScores = sentiment.getConfidenceScores();
                System.out.printf("Sentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                    sentiment.getSentiment(), sentencesScores.getPositive(), sentencesScores.getNeutral(), sentencesScores.getNegative());
            });
        });
    }
}
