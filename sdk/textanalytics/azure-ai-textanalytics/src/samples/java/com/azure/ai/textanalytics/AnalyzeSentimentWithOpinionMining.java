// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample demonstrates how to synchronously analyze the sentiment of document with opinion mining.
 */
public class AnalyzeSentimentWithOpinionMining {
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
        String document = "Bad atmosphere. Not close to plenty of restaurants, hotels, and transit! Staff are not friendly and helpful.";

        System.out.printf("Text = %s%n", document);

        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions().setIncludeOpinionMining(true);
        final DocumentSentiment documentSentiment = client.analyzeSentiment(document, "en", options);
        SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
        System.out.printf(
            "Recognized document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
            documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());

        documentSentiment.getSentences().forEach(sentenceSentiment -> {
            SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
            System.out.printf("\tSentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
                sentenceSentiment.getSentiment(), sentenceScores.getPositive(), sentenceScores.getNeutral(), sentenceScores.getNegative());
            sentenceSentiment.getOpinions().forEach(opinion -> {
                TargetSentiment targetSentiment = opinion.getTarget();
                System.out.printf("\t\tTarget sentiment: %s, target text: %s%n", targetSentiment.getSentiment(),
                    targetSentiment.getText());
                for (AssessmentSentiment assessmentSentiment : opinion.getAssessments()) {
                    System.out.printf("\t\t\t'%s' assessment sentiment because of \"%s\". Is the assessment negated: %s.%n",
                        assessmentSentiment.getSentiment(), assessmentSentiment.getText(), assessmentSentiment.isNegated());
                }
            });
        });
    }
}
