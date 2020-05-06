// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.batch;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to recognize the linked entities of {@link TextDocumentInput} documents.
 */
public class RecognizeLinkedEntitiesBatchDocuments {
    /**
     * Main method to invoke this demo about how to recognize the linked entities of {@link TextDocumentInput} documents.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        // The texts that need be analyzed.
        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("A", "Old Faithful is a geyser at Yellowstone Park.", "en"),
            new TextDocumentInput("B", "Mount Shasta has lenticular clouds.", "en")
        );

        // Request options: show statistics and model version
        TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setIncludeStatistics(true).setModelVersion("latest");

        Iterable<TextAnalyticsPagedResponse<RecognizeLinkedEntitiesResult>> linkedEntitiesBatchResult =
            client.recognizeLinkedEntitiesBatch(documents, requestOptions, Context.NONE).iterableByPage();

        // Recognizing linked entities for each document in a batch of documents
        linkedEntitiesBatchResult.forEach(pagedResponse -> {
            System.out.printf("Results of Azure Text Analytics \"Linked Entities Recognition\" Model, version: %s%n", pagedResponse.getModelVersion());

            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = pagedResponse.getStatistics();
            System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(), batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());


            AtomicInteger counter = new AtomicInteger();
            for (RecognizeLinkedEntitiesResult entitiesResult : pagedResponse.getElements()) {
                // Recognized linked entities from documents
                System.out.printf("%n%s%n", documents.get(counter.getAndIncrement()));
                if (entitiesResult.isError()) {
                    // Erroneous document
                    System.out.printf("Cannot recognize linked entities. Error: %s%n", entitiesResult.getError().getMessage());
                } else {
                    // Valid document
                    entitiesResult.getEntities().forEach(linkedEntity -> {
                        System.out.println("Linked Entities:");
                        System.out.printf("\tName: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
                            linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
                        linkedEntity.getLinkedEntityMatches().forEach(entityMatch -> System.out.printf(
                            "\tMatched entity: %s, score: %f.%n", entityMatch.getText(), entityMatch.getConfidenceScore()));
                    });
                }
            }
        });
    }
}
