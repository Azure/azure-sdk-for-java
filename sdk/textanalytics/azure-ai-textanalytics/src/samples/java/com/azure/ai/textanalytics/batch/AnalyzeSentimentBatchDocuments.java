// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to analyze the sentiments of documents.
 */
public class AnalyzeSentimentBatchDocuments {
    /**
     * Main method to invoke this demo about how to analyze the sentiments of documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. I wouldn't recommend staying there.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi! The waiters were excellent.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Analyzing batch sentiments
        final Iterable<TextAnalyticsPagedResponse<AnalyzeSentimentResult>> sentimentBatchResult =
            client.analyzeSentimentBatch(inputs, requestOptions, Context.NONE).iterableByPage();

        sentimentBatchResult.forEach(pagedResponse -> {
            System.out.printf("Model version: %s%n", pagedResponse.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = pagedResponse.getStatistics();
            System.out.printf("A batch of documents statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

            // Analyzed sentiment for each of documents from a batch of documents
            pagedResponse.getElements().forEach(analyzeSentimentResult -> {
                System.out.printf("%nDocument ID: %s%n", analyzeSentimentResult.getId());
                if (analyzeSentimentResult.isError()) {
                    // Erroneous document
                    System.out.printf("Cannot analyze sentiment. Error: %s%n", analyzeSentimentResult.getError().getMessage());
                } else {
                    // Valid document
                    DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
                    System.out.printf("Analyzed document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                        documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());
                    documentSentiment.getSentences().forEach(sentenceSentiment -> {
                        SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
                        System.out.printf(
                            "Analyzed sentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                            sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
                    });
                }
            });
        });
    }
}
