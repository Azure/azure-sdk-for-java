// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentSentiment;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class AnalyzeSentimentBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("The hotel was dark and unclean.").setLanguage("US"),
            new TextDocumentInput("The restaurant had amazing gnocci.").setLanguage("US")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<DocumentSentiment> detectedBatchResult = client.analyzeDocumentSentiment(inputs, requestOptions);

        final String modelVersion = detectedBatchResult.getModelVersion();
        System.out.printf("Model version: %s", modelVersion);

        final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentsCount(),
            batchStatistics.getErroneousDocumentsCount(),
            batchStatistics.getTransactionsCount(),
            batchStatistics.getValidDocumentsCount());

        // Detecting sentiment for each of document from a batch of documents
        detectedBatchResult.stream().forEach(documentSentimentDocumentResult ->
            documentSentimentDocumentResult.getItems().stream().forEach(documentSentiment ->
                documentSentiment.getItems().stream().forEach(sentenceSentiment ->
                System.out.printf("Recognized sentence sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s. Length of sentence: %s, Offset of sentence: %s",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore(),
                    sentenceSentiment.getLength(),
                    sentenceSentiment.getOffSet()))));
    }
}
