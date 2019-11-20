// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DetectedLanguage;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.UnknownLanguageInput;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class DetectLanguageBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<UnknownLanguageInput> inputs = Arrays.asList(
            new UnknownLanguageInput("This is written in English").setCountryHint("US"),
            new UnknownLanguageInput("Este es un document escrito en Español.").setCountryHint("es")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<DetectedLanguage> detectedBatchResult = client.detectLanguages(inputs, requestOptions);

        final String modelVersion = detectedBatchResult.getModelVersion();
        System.out.printf("Model version: %s", modelVersion);
        final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentsCount(),
            batchStatistics.getErroneousDocumentsCount(),
            batchStatistics.getTransactionsCount(),
            batchStatistics.getValidDocumentsCount());


        // Detecting languages for a document from a batch of documents
        detectedBatchResult.stream().forEach(detectedLanguageDocumentResult ->
            detectedLanguageDocumentResult.getItems().stream().forEach(detectedLanguage ->
                System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
                    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore())));
    }

}
