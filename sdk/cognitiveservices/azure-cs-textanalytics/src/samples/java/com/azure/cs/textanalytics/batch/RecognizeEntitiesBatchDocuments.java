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
import com.azure.cs.textanalytics.models.Entity;
import com.azure.cs.textanalytics.models.TextAnalyticsRequestOptions;

import java.util.ArrayList;
import java.util.List;

public class RecognizeEntitiesBatchDocuments {
    public static void main(String[] args) {
        // TODO: user AAD token to do the authentication
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .buildClient();

        // The texts that need be analysed.
        List<DocumentInput> document = new ArrayList<>();
        DocumentInput input = new DocumentInput();
        input.setId("1").setText("Satya Nadella is the CEO of Microsoft").setLanguage("US");
        DocumentInput input2 = new DocumentInput();
        input2.setId("2").setText("Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("US");
        document.add(input);
        document.add(input2);

        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStats(true).setModelVersion("1.0");
        DocumentResultCollection<Entity> detectedResult = client.recognizeEntities(document, requestOptions);
        // Document level statistics
        final String modelVersion = detectedResult.getModelVersion();
        System.out.println(String.format("Model version: %s", modelVersion));

        final DocumentBatchStatistics documentBatchStatistics = detectedResult.getStatistics();
        System.out.println(String.format("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
            documentBatchStatistics.getDocumentsCount(),
            documentBatchStatistics.getErroneousDocumentsCount(),
            documentBatchStatistics.getTransactionsCount(),
            documentBatchStatistics.getValidDocumentsCount()));

        // Detecting entities for each of document from a batch of documents
        for (DocumentResult<Entity> entitiesList : detectedResult) {
            final DocumentStatistics documentStatistics = entitiesList.getDocumentStatistics();
            System.out.println(String.format("One entity document statistics, character count: %s, transaction count: %s.",
                documentStatistics.getCharactersCount(), documentStatistics.getTransactionsCount()));

            final List<Entity> entities = entitiesList.getItems();
            for (Entity entity : entities) {
                System.out.println(String.format("Recognized Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s",
                    entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore()));
            }
        }


    }
}
