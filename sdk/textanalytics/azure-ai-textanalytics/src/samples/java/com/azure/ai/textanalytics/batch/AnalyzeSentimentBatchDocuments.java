// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DocumentSentimentLabel;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedIterable;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Sample demonstrates how to analyze the sentiments of a batch input text.
 */
public class AnalyzeSentimentBatchDocuments {
    /**
     * Main method to invoke this demo about how to analyze the sentiments of a batch input text.
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
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("latest");

        // Analyzing batch sentiments
        final Iterable<TextAnalyticsPagedResponse<AnalyzeSentimentResult>> sentimentBatchResult =
            client.analyzeSentimentBatch(inputs, requestOptions, Context.NONE).iterableByPage();

        sentimentBatchResult.forEach(pagedResponse -> {
            System.out.printf("Model version: %s%n", pagedResponse.getModelVersion());

            // Batch statistics
            final TextDocumentBatchStatistics batchStatistics = pagedResponse.getStatistics();
            System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
                batchStatistics.getDocumentCount(),
                batchStatistics.getInvalidDocumentCount(),
                batchStatistics.getTransactionCount(),
                batchStatistics.getValidDocumentCount());

            // Analyzed sentiment for each of document from a batch of documents
            pagedResponse.getElements().forEach(analyzeSentimentResult -> {
                System.out.printf("%nDocument ID: %s%n", analyzeSentimentResult.getId());
                if (analyzeSentimentResult.isError()) {
                    // Erroneous document
                    System.out.printf("Cannot analyze sentiment. Error: %s%n", analyzeSentimentResult.getError().getMessage());
                } else {
                    // Valid document
                    final DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                    System.out.printf("Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                        documentSentiment.getSentiment(),
                        documentSentiment.getConfidenceScores().getPositive(),
                        documentSentiment.getConfidenceScores().getNeutral(),
                        documentSentiment.getConfidenceScores().getNegative());
                    documentSentiment.getSentences().forEach(sentenceSentiment ->
                        System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f, length of sentence: %s, offset of sentence: %s.%n",
                            sentenceSentiment.getSentiment(),
                            sentenceSentiment.getConfidenceScores().getPositive(),
                            sentenceSentiment.getConfidenceScores().getNeutral(),
                            sentenceSentiment.getConfidenceScores().getNegative(),
                            sentenceSentiment.getLength(),
                            sentenceSentiment.getOffset()));
                }
            });
        });

        filterPositiveDocumentSentiment(client, requestOptions);
        countMixedAndSortDocumentSentiment(client, requestOptions);
    }


    private static void filterPositiveDocumentSentiment(TextAnalyticsClient client, TextAnalyticsRequestOptions requestOptions) {
        System.out.println("====================== Positive Filtering========================");
        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en"),
            new TextDocumentInput("3", "The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("4", "Azure SDK is awesome.", "en")
        );

        // Analyzing batch sentiments
        final TextAnalyticsPagedIterable<AnalyzeSentimentResult> sentimentBatchResult =
            client.analyzeSentimentBatch(inputs, requestOptions, Context.NONE);
        // Filter only positive document sentiment;
        Stream<AnalyzeSentimentResult> result = sentimentBatchResult.stream().filter(analyzeSentimentResult ->
            analyzeSentimentResult.getDocumentSentiment().getSentiment().equals(DocumentSentimentLabel.POSITIVE));
        result.forEach(analyzeSentimentResult -> {
            System.out.printf("%nDocument ID: %s%n", analyzeSentimentResult.getId());
            if (analyzeSentimentResult.isError()) {
                // Erroneous document
                System.out.printf("Cannot analyze sentiment. Error: %s%n", analyzeSentimentResult.getError().getMessage());
            } else {
                // Valid document
                final DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    documentSentiment.getSentiment(),
                    documentSentiment.getConfidenceScores().getPositive(),
                    documentSentiment.getConfidenceScores().getNeutral(),
                    documentSentiment.getConfidenceScores().getNegative());
                documentSentiment.getSentences().forEach(sentenceSentiment ->
                    System.out.printf("Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f, length of sentence: %s, offset of sentence: %s.%n",
                        sentenceSentiment.getSentiment(),
                        sentenceSentiment.getConfidenceScores().getPositive(),
                        sentenceSentiment.getConfidenceScores().getNeutral(),
                        sentenceSentiment.getConfidenceScores().getNegative(),
                        sentenceSentiment.getLength(),
                        sentenceSentiment.getOffset()));
            }
        });
    }

    private static void countMixedAndSortDocumentSentiment(TextAnalyticsClient client, TextAnalyticsRequestOptions requestOptions) {
        System.out.println("====================== Count Mixed Document Sentiment ========================");
        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("C", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("D", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en"),
            new TextDocumentInput("A", "The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("B", "Azure SDK is awesome.", "en")
        );

        // Analyzing batch sentiments
        final TextAnalyticsPagedIterable<AnalyzeSentimentResult> sentimentBatchResult =
            client.analyzeSentimentBatch(inputs, requestOptions, Context.NONE);

        long count = sentimentBatchResult.stream().filter(analyzeSentimentResult ->
            analyzeSentimentResult.getDocumentSentiment().getSentiment().equals(DocumentSentimentLabel.MIXED)).count();

        System.out.printf("Count of mixed document sentiment: %s%n", count);

        System.out.println("====================== Sort Document Sentiment ========================");

        sentimentBatchResult.stream().sorted(Comparator.comparing(DocumentResult::getId))
            .forEach(sentimentResult -> {
                System.out.printf("%nDocument ID: %s%n", sentimentResult.getId());
                final DocumentSentiment documentSentiment = sentimentResult.getDocumentSentiment();
                System.out.printf("Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                    documentSentiment.getSentiment(),
                    documentSentiment.getConfidenceScores().getPositive(),
                    documentSentiment.getConfidenceScores().getNeutral(),
                    documentSentiment.getConfidenceScores().getNegative());
            });
    }
}
