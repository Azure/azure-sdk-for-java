// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AspectSentiment;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.OpinionSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.textanalytics.models.TextSentiment.MIXED;
import static com.azure.ai.textanalytics.models.TextSentiment.NEGATIVE;
import static com.azure.ai.textanalytics.models.TextSentiment.POSITIVE;

/**
 * Sample demonstrates how to synchronously analyze the sentiment of document with opinion mining.
 */
public class AnalyzeSentimentWithAspects {
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

        final DocumentSentiment documentSentiment = client.analyzeSentiment(document, true, "en");
        SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();
        System.out.printf(
            "Recognized document sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
            documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());

        List<AspectSentiment> positiveAspects = new ArrayList<>();
        List<AspectSentiment> mixedAspects = new ArrayList<>();
        List<AspectSentiment> negativeAspects = new ArrayList<>();
        documentSentiment.getSentences().forEach(sentenceSentiment -> {
            SentimentConfidenceScores sentenceScores = sentenceSentiment.getConfidenceScores();
            System.out.printf("\tSentence sentiment: %s, positive score: %f, neutral score: %f, negative score: %f.%n",
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
    }
}
