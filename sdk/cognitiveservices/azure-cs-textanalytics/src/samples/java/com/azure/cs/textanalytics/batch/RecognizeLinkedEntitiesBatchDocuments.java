// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class RecognizeLinkedEntitiesBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("Old Faithful is a geyser at Yellowstone Park.").setLanguage("US"),
            new TextDocumentInput("Mount Shasta has lenticular clouds.").setLanguage("US")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<LinkedEntity> detectedBatchResult = client.recognizeLinkedEntities(inputs, requestOptions);
        final String modelVersion = detectedBatchResult.getModelVersion();
        System.out.printf("Model version: %s", modelVersion);
        final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentsCount(),
            batchStatistics.getErroneousDocumentsCount(),
            batchStatistics.getTransactionsCount(),
            batchStatistics.getValidDocumentsCount());

        // Detecting language from a batch of documents
        detectedBatchResult.stream().forEach(linkedEntityDocumentResult ->
            linkedEntityDocumentResult.getItems().stream().forEach(linkedEntity ->
                System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource())));
    }
}
