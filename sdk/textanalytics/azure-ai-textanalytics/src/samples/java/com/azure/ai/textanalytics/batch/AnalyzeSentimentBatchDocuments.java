// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
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
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Analyzing batch sentiments
        DocumentResultCollection<AnalyzeSentimentResult> analyzedBatchResult = client.analyzeBatchSentimentWithResponse(
            inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s%n", analyzedBatchResult.getModelVersion());

        // Batch statistics
        final TextDocumentBatchStatistics batchStatistics = analyzedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s.%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getInvalidDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        // Analyzed sentiment for each of document from a batch of documents
        for (AnalyzeSentimentResult analyzeSentimentResult : analyzedBatchResult) {
            System.out.printf("Document ID: %s%n", analyzeSentimentResult.getId());
            // Erroneous document
            if (analyzeSentimentResult.isError()) {
                System.out.printf("Cannot analyze sentiment. Error: %s%n", analyzeSentimentResult.getError().getMessage());
                continue;
            }
            // Valid document
            final TextSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();
            System.out.printf("Recognized document sentiment: %s, positive score: %s, neutral score: %s, negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore(),
                documentSentiment.getLength(),
                documentSentiment.getOffset());
            for (TextSentiment sentenceSentiment : analyzeSentimentResult.getSentenceSentiments()) {
                System.out.printf("Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s, length of sentence: %s, offset of sentence: %s.%n",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore(),
                    sentenceSentiment.getLength(),
                    sentenceSentiment.getOffset());
            }
        }
    }
}
