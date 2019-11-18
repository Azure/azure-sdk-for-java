// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentInput;
import com.azure.cs.textanalytics.models.DocumentResult;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.DocumentStatistics;
import com.azure.cs.textanalytics.models.Sentiment;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeSentimentBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<DocumentInput> documents = new ArrayList<>();
        DocumentInput input = new DocumentInput();
        input.setId("1").setText("The hotel was dark and unclean.").setLanguage("US");
        DocumentInput input2 = new DocumentInput();
        input2.setId("2").setText("The restaurant had amazing gnocci.").setLanguage("US");
        documents.add(input);
        documents.add(input2);

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStats(true).setModelVersion("1.0");

        // Document level statistics
        DocumentResultCollection<DocumentSentiment> detectedResult = client.analyzeDocumentSentiment(documents, requestOptions);
        final String modelVersion = detectedResult.getModelVersion();
        System.out.println(String.format("Model version: %s", modelVersion));

        final DocumentBatchStatistics documentBatchStatistics = detectedResult.getStatistics();
        System.out.println(String.format("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            documentBatchStatistics.getDocumentsCount(),
            documentBatchStatistics.getErroneousDocumentsCount(),
            documentBatchStatistics.getTransactionsCount(),
            documentBatchStatistics.getValidDocumentsCount()));

        // Detecting sentiment for each of document from a batch of documents
        for (DocumentResult<DocumentSentiment> documentSentimentDocumentResult : detectedResult) {
            // For each document
            final DocumentStatistics documentStatistics = documentSentimentDocumentResult.getDocumentStatistics();
            System.out.println(String.format("One sentiment document statistics, character count: %s, transaction count: %s.",
                documentStatistics.getCharactersCount(), documentStatistics.getTransactionsCount()));

            final List<DocumentSentiment> documentSentiments = documentSentimentDocumentResult.getItems();

            for (DocumentSentiment item : documentSentiments) {
                final Sentiment documentSentiment = item.getDocumentSentiment();
                System.out.println(String.format(
                    "Recognized document sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
                    documentSentiment.getSentimentClass(),
                    documentSentiment.getPositiveScore(),
                    documentSentiment.getNeutralScore(),
                    documentSentiment.getNegativeScore()));

                final List<Sentiment> sentenceSentiments = item.getItems();
                for (Sentiment sentenceSentiment : sentenceSentiments) {
                    System.out.println(String.format(
                        "Recognized sentence sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s. Length of sentence: %s, Offset of sentence: %s",
                        sentenceSentiment.getSentimentClass(),
                        sentenceSentiment.getPositiveScore(),
                        sentenceSentiment.getNeutralScore(),
                        sentenceSentiment.getNegativeScore(),
                        sentenceSentiment.getLength(),
                        sentenceSentiment.getOffSet()));
                }
            }
        }

    }
}
