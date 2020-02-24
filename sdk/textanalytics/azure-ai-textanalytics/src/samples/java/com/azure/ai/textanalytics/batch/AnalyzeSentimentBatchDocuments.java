// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsPagedResponse;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

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
            .apiKey(new TextAnalyticsApiKeyCredential("b2f8b7b697c348dcb0e30055d49f3d0f"))
            .endpoint("https://javatextanalyticstestresources.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

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
                System.out.printf("%nDocument ID: %s, input text: %s%n", analyzeSentimentResult.getId(), analyzeSentimentResult.getInputText());
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
    }
}
