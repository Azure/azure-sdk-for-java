// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to analyze a healthcare task.
 */
public class AnalyzeHealthcareEntities {
    /**
     * Main method to invoke this demo about how to begin recognizing the healthcare long-running operation.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client =
            new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The patient is a 54-year-old gentleman with a history of progressive angina over the past several"
                    + " months."
            ));
        }

        // Request options: show statistics and model version
        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true);

        SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>>
            syncPoller = client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
        System.out.printf("Poller status: %s.%n", syncPoller.poll().getStatus());
        syncPoller.waitForCompletion();
        PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareResultIterable = syncPoller.getFinalResult();

        // Task operation statistics
        AnalyzeHealthcareEntitiesOperationDetail operationResult = syncPoller.poll().getValue();
        System.out.printf("Operation created time: %s, expiration time: %s.%n",
            operationResult.getCreatedAt(), operationResult.getExpiresAt());
        System.out.printf("Poller status: %s.%n", syncPoller.poll().getStatus());
        Iterable<PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> pagedResults =
            healthcareResultIterable.iterableByPage();
        for (PagedResponse<AnalyzeHealthcareEntitiesResultCollection> page : pagedResults) {
            System.out.println("Response code: " + page.getStatusCode());
            System.out.println("Continuation Token: " + page.getContinuationToken());
            page.getElements().forEach(healthcareEntitiesResultCollection -> {
                // Model version
                System.out.printf(
                    "Results of Azure Text Analytics \"Analyze Healthcare Entities\" Model, version: %s%n",
                    healthcareEntitiesResultCollection.getModelVersion());
                // Batch statistics
                TextDocumentBatchStatistics batchStatistics = healthcareEntitiesResultCollection.getStatistics();
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, "
                                      + "transaction count = %s, valid document count = %s.%n",
                    batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(),
                    batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                // Healthcare entities collection
                healthcareEntitiesResultCollection.forEach(healthcareEntitiesResult -> {
                    System.out.println("Document id = " + healthcareEntitiesResult.getId());
                    System.out.println("Document entities: ");
                    AtomicInteger ct = new AtomicInteger();
                    // Healthcare entities
                    healthcareEntitiesResult.getEntities().forEach(healthcareEntity -> {
                        System.out.printf("\ti = %d, Text: %s, category: %s, subcategory: %s, confidence score: %f.%n",
                            ct.getAndIncrement(), healthcareEntity.getText(), healthcareEntity.getCategory(),
                            healthcareEntity.getSubcategory(), healthcareEntity.getConfidenceScore());
                        // Data sources
                        IterableStream<EntityDataSource> dataSources = healthcareEntity.getDataSources();
                        if (dataSources != null) {
                            dataSources.forEach(dataSource -> System.out.printf(
                                "\t\tEntity ID in data source: %s, data source: %s.%n",
                                dataSource.getEntityId(), dataSource.getName()));
                        }
                        // Entities relationship
                        Map<HealthcareEntity, HealthcareEntityRelationType> relatedHealthcareEntities =
                            healthcareEntity.getRelatedEntities();
                        if (!CoreUtils.isNullOrEmpty(relatedHealthcareEntities)) {
                            relatedHealthcareEntities.forEach(
                                (relatedHealthcareEntity, entityRelationType) -> System.out.printf(
                                    "\t\tRelated entity: %s, relation type: %s.%n",
                                    relatedHealthcareEntity.getText(), entityRelationType));
                        }
                    });
                });
            });
        }
    }
}
