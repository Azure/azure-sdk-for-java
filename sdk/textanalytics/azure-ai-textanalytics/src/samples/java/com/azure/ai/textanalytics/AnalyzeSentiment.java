// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;

/**
 * Sample demonstrates how to analyze the sentiment of an input text.
 */
public class AnalyzeSentiment {
    /**
     * Main method to invoke this demo about how to analyze the sentiment of an input text.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey(new TextAnalyticsApiKeyCredential("{subscription_key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The text that need be analysed.
        String text = "The hotel was dark and unclean.";

        final AnalyzeSentimentResult sentimentResult = client.analyzeSentiment(text);

        final TextSentiment documentSentiment = sentimentResult.getDocumentSentiment();
        System.out.printf(
            "Recognized sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
            documentSentiment.getTextSentimentClass(),
            documentSentiment.getPositiveScore(),
            documentSentiment.getNeutralScore(),
            documentSentiment.getNegativeScore());

        for (TextSentiment textSentiment : sentimentResult.getSentenceSentiments()) {
            System.out.printf(
                "Recognized sentence sentiment: %s, positive score: %s, neutral score: %s, negative score: %s.%n",
                textSentiment.getTextSentimentClass(),
                textSentiment.getPositiveScore(),
                textSentiment.getNeutralScore(),
                textSentiment.getNegativeScore());
        }
    }
}
