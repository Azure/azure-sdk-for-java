// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class RecognizeKeyPhrasesBatchDocuments {

    public static void main(String[] args) {

        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("My cat might need to see a veterinarian").setLanguage("US"),
            new TextDocumentInput("The pitot tube is used to measure airspeed.").setLanguage("US")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<String> detectedBatchResult = client.extractKeyPhrases(inputs, requestOptions);

        final String modelVersion = detectedBatchResult.getModelVersion();
        System.out.printf("Model version: %s", modelVersion);
        final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentsCount(),
            batchStatistics.getErroneousDocumentsCount(),
            batchStatistics.getTransactionsCount(),
            batchStatistics.getValidDocumentsCount());

        // Detecting key phrase for each of document from a batch of documents
        detectedBatchResult.stream().forEach(keyPhraseResult ->
            keyPhraseResult.getItems().stream().forEach(keyPhrases ->
                System.out.printf("Recognized Phrases: %s", keyPhrases)));
    }
}
