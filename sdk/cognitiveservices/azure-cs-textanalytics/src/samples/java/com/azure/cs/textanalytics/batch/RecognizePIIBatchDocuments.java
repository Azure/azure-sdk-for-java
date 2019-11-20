// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.NamedEntity;
import com.azure.cs.textanalytics.models.TextDocumentInput;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.Arrays;
import java.util.List;

public class RecognizePIIBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<TextDocumentInput> inputs = Arrays.asList(
            new TextDocumentInput("My SSN is 555-55-5555").setLanguage("US"),
            new TextDocumentInput("Visa card 4147999933330000").setLanguage("US")
        );

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
        DocumentResultCollection<NamedEntity> detectedBatchResult = client.recognizePiiEntities(inputs, requestOptions);
        final String modelVersion = detectedBatchResult.getModelVersion();
        System.out.printf("Model version: %s", modelVersion);
        final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
        System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            batchStatistics.getDocumentsCount(),
            batchStatistics.getErroneousDocumentsCount(),
            batchStatistics.getTransactionsCount(),
            batchStatistics.getValidDocumentsCount());


        // Detecting pii entities from a batch of documents
        detectedBatchResult.stream().forEach(piiEntityDocumentResult ->
            piiEntityDocumentResult.getItems().stream().forEach(entity ->
                System.out.printf("Recognized Personal Identifiable Info NamedEntity: %s, NamedEntity Type: %s, NamedEntity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                    entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore())));
    }

}
