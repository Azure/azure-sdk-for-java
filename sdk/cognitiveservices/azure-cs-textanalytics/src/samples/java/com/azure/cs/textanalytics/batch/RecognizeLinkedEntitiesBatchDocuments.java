// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.batch;

import com.azure.cs.textanalytics.TextAnalyticsClient;
import com.azure.cs.textanalytics.TextAnalyticsClientBuilder;
import com.azure.cs.textanalytics.models.DocumentBatchStatistics;
import com.azure.cs.textanalytics.models.DocumentInput;
import com.azure.cs.textanalytics.models.DocumentResult;
import com.azure.cs.textanalytics.models.DocumentResultCollection;
import com.azure.cs.textanalytics.models.DocumentStatistics;
import com.azure.cs.textanalytics.models.LinkedEntity;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.ArrayList;
import java.util.List;

public class RecognizeLinkedEntitiesBatchDocuments {

    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<DocumentInput> documents = new ArrayList<>();
        DocumentInput input = new DocumentInput();

        input.setId("1").setText("Old Faithful is a geyser at Yellowstone Park").setLanguage("US");
        DocumentInput input2 = new DocumentInput();
        input2.setId("2").setText("Mount Shasta has lenticular clouds.").setLanguage("US");

        documents.add(input);
        documents.add(input2);

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStats(true).setModelVersion("1.0");
        DocumentResultCollection<LinkedEntity> detectedResult = client.recognizeLinkedEntities(documents, requestOptions);
        // Document level statistics
        final String modelVersion = detectedResult.getModelVersion();
        System.out.println(String.format("Model version: %s", modelVersion));

        final DocumentBatchStatistics documentBatchStatistics = detectedResult.getStatistics();
        System.out.println(String.format("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            documentBatchStatistics.getDocumentsCount(),
            documentBatchStatistics.getErroneousDocumentsCount(),
            documentBatchStatistics.getTransactionsCount(),
            documentBatchStatistics.getValidDocumentsCount()));

        // Detecting language from a batch of documents
        for (DocumentResult<LinkedEntity> documentLinkedEntities : detectedResult) {
            final DocumentStatistics documentStatistics = documentLinkedEntities.getDocumentStatistics();
            System.out.println(String.format("One linked entity document statistics, character count: %s, transaction count: %s.",
                documentStatistics.getCharactersCount(), documentStatistics.getTransactionsCount()));

            final List<LinkedEntity> linkedEntities = documentLinkedEntities.getItems();
            for (LinkedEntity linkedEntity : linkedEntities) {
                System.out.println(String.format("Recognized Linked Entity: %s, URL: %s, Data Source: %s",
                    linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
            }
        }
    }
}
