// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to analyze the sentiments of {@code String} documents with opinion mining.
 */
public class AnalyzeSentimentBatchStringWithOpinionMining {
    /**
     * Main method to invoke this demo about how to analyze the sentiments of {@link TextDocumentInput} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The documents that need to be analyzed.
        List<String> documents = Arrays.asList(
            "Great atmosphere. Close to plenty of restaurants, hotels, and transit! Staff are friendly and helpful.",
            "Bad atmosphere. Not close to plenty of restaurants, hotels, and transit! Staff are not friendly and helpful."
        );

        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions()
            .setIncludeOpinionMining(true).setIncludeStatistics(true).setModelVersion("latest");

        // Analyzing sentiment for each document in a batch of documents
        AnalyzeSentimentResultCollection sentimentBatchResultCollection = client.analyzeSentimentBatch(documents, "en", options);

        // Model version
        System.out.printf("Results of Azure Text Analytics \"Sentiment Analysis\" Model, version: %s%n", sentimentBatchResultCollection.getModelVersion());

        // Analyzed sentiment for each document in a batch of documents
        AtomicInteger counter = new AtomicInteger();
        for (AnalyzeSentimentResult analyzeSentimentResult : sentimentBatchResultCollection) {
            System.out.printf("%nText = %s%n", documents.get(counter.getAndIncrement()));
            DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment();

            SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
            System.out.printf("Analyzed document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());
            documentSentiment.getSentences().forEach(sentenceSentiment -> {
                SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
                System.out.printf("\tSentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                    sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
                sentenceSentiment.getMinedOpinions().forEach(minedOpinions -> {
                    AspectSentiment aspectSentiment = minedOpinions.getAspect();
                    System.out.printf("\t\tAspect sentiment: %s, aspect text: %s%n", aspectSentiment.getSentiment(),
                        aspectSentiment.getText());
                    for (OpinionSentiment opinionSentiment : minedOpinions.getOpinions()) {
                        System.out.printf("\t\t\t'%s' opinion sentiment because of \"%s\". Is the opinion negated: %s.%n",
                            opinionSentiment.getSentiment(), opinionSentiment.getText(), opinionSentiment.isNegated());
                    }
                });
            });
        }
    }
}
