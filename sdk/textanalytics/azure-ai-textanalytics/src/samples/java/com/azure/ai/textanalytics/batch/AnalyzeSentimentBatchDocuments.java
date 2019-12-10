// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.ai.textanalytics.models.TextSentimentResult;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;

public class AnalyzeSentimentBatchDocuments {

    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscription-key")
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("1", "The hotel was dark and unclean.", "US"),
            new TextDocumentInput("2", "The restaurant had amazing gnocci.", "US")
        );

        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<TextSentimentResult> detectedBatchResult = client.analyzeBatchSentimentWithResponse(inputs, requestOptions, Context.NONE).getValue();
        System.out.printf("Model version: %s", detectedBatchResult.getModelVersion());

        final TextBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());

        // Detecting sentiment for each of document from a batch of documents
        detectedBatchResult.stream().forEach(result -> {
            final TextSentiment documentSentiment = result.getDocumentSentiment();
            System.out.printf("Recognized document sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s. Length of sentence: %s, Offset of sentence: %s",
                documentSentiment.getTextSentimentClass(),
                documentSentiment.getPositiveScore(),
                documentSentiment.getNeutralScore(),
                documentSentiment.getNegativeScore(),
                documentSentiment.getLength(),
                documentSentiment.getOffset());

            result.getSentenceSentiments().forEach(sentenceSentiment ->
                System.out.printf("Recognized sentence sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s. Length of sentence: %s, Offset of sentence: %s",
                    sentenceSentiment.getTextSentimentClass(),
                    sentenceSentiment.getPositiveScore(),
                    sentenceSentiment.getNeutralScore(),
                    sentenceSentiment.getNegativeScore(),
                    sentenceSentiment.getLength(),
                    sentenceSentiment.getOffset()));
        });
    }
}
