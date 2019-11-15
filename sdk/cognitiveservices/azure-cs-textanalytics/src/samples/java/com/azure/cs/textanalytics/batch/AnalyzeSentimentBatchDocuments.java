// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.core.util.Context;
import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.implementation.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.MultiLanguageBatchInput;
import com.azure.cs.textanalytics.models.MultiLanguageInput;
import com.azure.cs.textanalytics.implementation.models.SentenceSentiment;
import com.azure.cs.textanalytics.implementation.models.SentimentResponse;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeSentimentBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<MultiLanguageInput> documents = new ArrayList<>();
        MultiLanguageInput input = new MultiLanguageInput();
        input.setId("1").setText("The hotel was dark and unclean.").setLanguage("US");
        MultiLanguageInput input2 = new MultiLanguageInput();
        input2.setId("2").setText("The restaurant had amazing gnocci.").setLanguage("US");
        documents.add(input);
        documents.add(input2);
        MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput();
        batchInput.setDocuments(documents);

        // Detecting language from a batch of documents
        SentimentResponse detectedResult = client.analyzeSentimentWithResponse(batchInput, false, Context.NONE).getValue();
        List<DocumentSentiment> documentSentiments = detectedResult.getDocuments();
        for (DocumentSentiment documentSentiment : documentSentiments) {
            final String sentiment = documentSentiment.getSentiment();
            final Double documentScore = (Double) documentSentiment.getDocumentScores();
            System.out.println(String.format("Recognized Sentiment: %s, Document Score: %s", sentiment, documentScore));

            final List<SentenceSentiment> sentenceSentiments = documentSentiment.getSentences();
            for (SentenceSentiment sentenceSentiment : sentenceSentiments) {
                System.out.println(String.format("Recognized Sentence Sentiment: %s, Sentence Score: %s, Offset: %s, Length: %s",
                    sentenceSentiment.getSentiment(),
                    sentenceSentiment.getSentenceScores(),
                    sentenceSentiment.getOffset(),
                    sentenceSentiment.getLength()));
            }
        }
    }
}
