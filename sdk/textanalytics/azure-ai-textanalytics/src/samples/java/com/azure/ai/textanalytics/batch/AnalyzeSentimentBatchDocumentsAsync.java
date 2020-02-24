// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously analyze the sentiments of a batch input text.
 */
public class AnalyzeSentimentBatchDocumentsAsync {
    /**
     * Main method to invoke this demo about how to analyze the sentiments of a batch input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .apiKey(new TextAnalyticsApiKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean. The restaurant had amazing gnocchi.", "en"),
            new TextDocumentInput("2", "The restaurant had amazing gnocchi. The hotel was dark and unclean.", "en")
        );

        // Request options: show statistics and model version
        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);

        // Analyzing batch sentiments
        client.analyzeSentimentBatch(inputs, requestOptions).byPage().subscribe(
            pagedResponse -> {
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
                        System.out.printf("Analyzed document sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n",
                            documentSentiment.getSentiment(),
                            documentSentiment.getConfidenceScores().getPositive(),
                            documentSentiment.getConfidenceScores().getNeutral(),
                            documentSentiment.getConfidenceScores().getNegative());
                        documentSentiment.getSentences().forEach(sentenceSentiment ->
                            System.out.printf("Analyzed sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f, length of sentence: %s, offset of sentence: %s.%n",
                                sentenceSentiment.getSentiment(),
                                sentenceSentiment.getConfidenceScores().getPositive(),
                                sentenceSentiment.getConfidenceScores().getNeutral(),
                                sentenceSentiment.getConfidenceScores().getNegative(),
                                sentenceSentiment.getLength(),
                                sentenceSentiment.getOffset()));
                    }
                });
            },
            error -> System.err.println("There was an error analyzing sentiment of the text inputs." + error),
            () -> System.out.println("Batch of sentiment analyzed."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        }
    }
}
