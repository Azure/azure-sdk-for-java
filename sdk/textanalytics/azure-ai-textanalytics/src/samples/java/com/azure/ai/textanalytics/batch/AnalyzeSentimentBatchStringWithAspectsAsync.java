// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.ai.textanalytics.models.TextSentiment.MIXED;
import static com.azure.ai.textanalytics.models.TextSentiment.NEGATIVE;
import static com.azure.ai.textanalytics.models.TextSentiment.POSITIVE;

/**
 * Sample demonstrates how to asynchronously analyze the sentiments of {@code String} documents with opinion mining.
 */
public class AnalyzeSentimentBatchStringWithAspectsAsync {

    /**
     * Main method to invoke this demo about how to analyze the sentiments of {@code String} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // The texts that need be analyzed.
        List<String> documents = Arrays.asList(
            "Great atmosphere. Close to plenty of restaurants, hotels, and transit! Staff are friendly and helpful.",
            "Bad atmosphere. Not close to plenty of restaurants, hotels, and transit! Staff are not friendly and helpful."
        );

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        // Analyzing sentiment for each document in a batch of documents
        client.analyzeSentimentBatch(documents, true, "en", requestOptions).subscribe(
            sentimentBatchResultCollection -> {
                System.out.printf("Results of Azure Text Analytics \"Sentiment Analysis\" Model, version: %s%n", sentimentBatchResultCollection.getModelVersion());

                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = sentimentBatchResultCollection.getStatistics();
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                    batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                // Analyzed sentiment for each document in a batch of documents
                AtomicInteger counter = new AtomicInteger();

                List<AspectSentiment> positiveAspects = new ArrayList<>();
                List<AspectSentiment> mixedAspects = new ArrayList<>();
                List<AspectSentiment> negativeAspects = new ArrayList<>();
                for (AnalyzeSentimentResult analyzeSentimentResult : sentimentBatchResultCollection) {
                    // Analyzed sentiment for each document
                    System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
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
                                "\tAnalyzed sentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                                sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
                            sentenceSentiment.getAspects().forEach(aspectSentiment -> {
                                TextSentiment aspectTextSentiment = aspectSentiment.getSentiment();
                                if (NEGATIVE.equals(aspectTextSentiment)) {
                                    negativeAspects.add(aspectSentiment);
                                } else if (POSITIVE.equals(aspectTextSentiment)) {
                                    positiveAspects.add(aspectSentiment);
                                } else if (MIXED.equals(aspectTextSentiment)) {
                                    mixedAspects.add(aspectSentiment);
                                }
                            });
                        });
                    }
                }

                System.out.printf("Positive aspects count: %d%n", positiveAspects.size());
                for (AspectSentiment positiveAspect : positiveAspects) {
                    System.out.printf("\tAspect: %s%n", positiveAspect.getText());
                    for (OpinionSentiment opinionSentiment : positiveAspect.getOpinions()) {
                        System.out.printf("\t\t'%s' sentiment because of \"%s\". Does the aspect negated: %s.%n",
                            opinionSentiment.getSentiment(), opinionSentiment.getText(), opinionSentiment.isNegated());
                    }
                }

                System.out.printf("Mixed aspects count: %d%n", mixedAspects.size());
                for (AspectSentiment mixedAspect : mixedAspects) {
                    System.out.printf("\tAspect: %s%n", mixedAspect.getText());
                    for (OpinionSentiment opinionSentiment : mixedAspect.getOpinions()) {
                        System.out.printf("\t\t'%s' sentiment because of \"%s\". Does the aspect negated: %s.%n",
                            opinionSentiment.getSentiment(), opinionSentiment.getText(), opinionSentiment.isNegated());
                    }
                }

                System.out.printf("Negative aspects count: %d%n", negativeAspects.size());
                for (AspectSentiment negativeAspect : negativeAspects) {
                    System.out.printf("\tAspect: %s%n", negativeAspect.getText());
                    for (OpinionSentiment opinionSentiment : negativeAspect.getOpinions()) {
                        System.out.printf("\t\t'%s' sentiment because of \"%s\". Does the aspect negated: %s.%n",
                            opinionSentiment.getSentiment(), opinionSentiment.getText(), opinionSentiment.isNegated());
                    }
                }
            },
            error -> System.err.println("There was an error analyzing sentiment of the documents." + error),
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
