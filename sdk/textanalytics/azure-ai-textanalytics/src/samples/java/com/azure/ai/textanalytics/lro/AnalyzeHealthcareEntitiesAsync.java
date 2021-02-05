// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously analyze a healthcare task.
 */
public class AnalyzeHealthcareEntitiesAsync {
    /**
     * Main method to invoke this demo about how to begin recognizing the healthcare long-running operation.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client =
            new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The patient is a 54-year-old gentleman with a history of progressive angina over the past several"
                    + " months."));
        }

        // Request options: show statistics and model version
        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions()
            .setIncludeStatistics(true);

        client.beginAnalyzeHealthcareEntities(documents, options)
            .flatMap(pollResult -> {
                AnalyzeHealthcareEntitiesOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .subscribe(healthcareTaskResultPagedFlux -> healthcareTaskResultPagedFlux.subscribe(
                healthcareEntitiesResultCollection -> {
                    // Model version
                    System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                        healthcareEntitiesResultCollection.getModelVersion());
                    // Batch statistics
                    TextDocumentBatchStatistics batchStatistics = healthcareEntitiesResultCollection.getStatistics();
                    System.out.printf("Documents statistics: document count = %s, erroneous document count = %s,"
                                          + " transaction count = %s, valid document count = %s.%n",
                        batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(),
                        batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
                    // Healthcare entities collection
                    healthcareEntitiesResultCollection.forEach(healthcareEntitiesResult -> {
                        System.out.println("Document id = " + healthcareEntitiesResult.getId());
                        System.out.println("Document entities: ");
                        AtomicInteger ct = new AtomicInteger();
                        // Healthcare entities
                        healthcareEntitiesResult.getEntities().forEach(healthcareEntity -> {
                            System.out.printf(
                                "\ti = %d, Text: %s, category: %s, subcategory: %s, confidence score: %f.%n",
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
                }
            ));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
