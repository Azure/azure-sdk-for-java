// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DetectLanguageInput;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DetectLanguageResult;
import com.azure.cs.textanalytics.models.TextBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class DetectLanguageBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .subscriptionKey("subscriptionKey")
            .endpoint("https://service.cognitiveservices.azure.com/")
            .buildClient();

        // The texts that need be analysed.
        List<DetectLanguageInput> inputs = Arrays.asList(
            new DetectLanguageInput("1", "This is written in English","US"),
            new DetectLanguageInput("2", "Este es un document escrito en Español.", "es")
        );

        final TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true);
        final DocumentResultCollection<DetectLanguageResult> detectedBatchResult = client.detectBatchLanguages(inputs, requestOptions);
        System.out.printf("Model version: %s%n", detectedBatchResult.getModelVersion());

        final TextBatchStatistics batchStatistics = detectedBatchResult.getStatistics();
        System.out.printf("Batch statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s%n",
            batchStatistics.getDocumentCount(),
            batchStatistics.getErroneousDocumentCount(),
            batchStatistics.getTransactionCount(),
            batchStatistics.getValidDocumentCount());


        // Detecting languages for a document from a batch of documents
        detectedBatchResult.stream().forEach(result -> {

            final DetectedLanguage detectedPrimaryLanguage = result.getPrimaryLanguage();
            System.out.printf("Detected primary Language for Document: %s, %s, ISO 6391 Name: %s, Score: %s%n",
                result.getId(),
                detectedPrimaryLanguage.getName(),
                detectedPrimaryLanguage.getIso6391Name(),
                detectedPrimaryLanguage.getScore());

            result.getDetectedLanguages().forEach(detectedLanguage ->
                System.out.printf("Other detected Languages: %s, ISO 6391 Name: %s, Score: %s%n",
                    detectedLanguage.getName(),
                    detectedLanguage.getIso6391Name(),
                    detectedLanguage.getScore()));
        });
    }

}
