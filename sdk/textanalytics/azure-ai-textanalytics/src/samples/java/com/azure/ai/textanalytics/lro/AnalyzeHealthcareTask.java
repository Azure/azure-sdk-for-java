// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityDataSource;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationType;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
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
public class AnalyzeHealthcareTask {
    /**
     * Main method to invoke this demo about how to begin recognizing the healthcare long-running operation.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client =
            new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY")))
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT"))
                .buildClient();

        List<TextDocumentInput> documents = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            documents.add(new TextDocumentInput(Integer.toString(i),
                "The patient is a 54-year-old gentleman electro with a history of progressive angina over the past several months. " +
                    "The patient could have Parkinson's Disease (PD)."
            ));
        }

        // Request options: show statistics and model version
        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true);

        SyncPoller<AnalyzeHealthcareEntitiesOperationResult, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>> syncPoller =
            client.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
        System.out.printf("Poller status: %s.%n", syncPoller.poll().getStatus());
        syncPoller.waitForCompletion();
        PagedIterable<AnalyzeHealthcareEntitiesResultCollection> healthcareResultIterable = syncPoller.getFinalResult();

        // Task operation statistics
        AnalyzeHealthcareEntitiesOperationResult operationResult = syncPoller.poll().getValue();
        System.out.printf("Job created time: %s, expiration time: %s.%n",
            operationResult.getCreatedAt(), operationResult.getExpiresAt());

        System.out.printf("Poller status: %s.%n", syncPoller.poll().getStatus());

        Iterable<PagedResponse<AnalyzeHealthcareEntitiesResultCollection>> pagedResults = healthcareResultIterable.iterableByPage();
        for (PagedResponse<AnalyzeHealthcareEntitiesResultCollection> page : pagedResults) {
            System.out.println("Response code: " + page.getStatusCode());
            System.out.println("Continuation Token: " + page.getContinuationToken());
            page.getElements().forEach(healthcareTaskResult -> {
                // Model version
                System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                    healthcareTaskResult.getModelVersion());

                TextDocumentBatchStatistics healthcareTaskStatistics = healthcareTaskResult.getStatistics();
                // Batch statistics
                System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                    healthcareTaskStatistics.getDocumentCount(), healthcareTaskStatistics.getInvalidDocumentCount(),
                    healthcareTaskStatistics.getTransactionCount(), healthcareTaskStatistics.getValidDocumentCount());

                healthcareTaskResult.forEach(healthcareEntitiesResult -> {
                    System.out.println("Document id = " + healthcareEntitiesResult.getId());
                    System.out.println("Document entities: ");
                    AtomicInteger ct = new AtomicInteger();
                    healthcareEntitiesResult.getEntities().forEach(healthcareEntity -> {
                        System.out.printf("\ti = %d, Text: %s, category: %s, confidence score: %f.%n",
                            ct.getAndIncrement(),
                            healthcareEntity.getText(), healthcareEntity.getCategory(), healthcareEntity.getConfidenceScore());
                        IterableStream<HealthcareEntityDataSource> healthcareEntityDataSources = healthcareEntity.getHealthcareEntityDataSources();
                        if (healthcareEntityDataSources != null) {
                            healthcareEntityDataSources.forEach(healthcareEntityLink -> System.out.printf(
                                "\t\tHealthcare data source ID: %s, data source: %s.%n",
                                healthcareEntityLink.getDataSourceId(), healthcareEntityLink.getDataSource()));
                        }
                        Map<HealthcareEntity, HealthcareEntityRelationType> relatedHealthcareEntities = healthcareEntity.getRelatedHealthcareEntities();
                        if (!CoreUtils.isNullOrEmpty(relatedHealthcareEntities)) {
                            relatedHealthcareEntities.forEach((relatedHealthcareEntity, entityRelationType) -> System.out.printf(
                                "\t\tRelated entity: %s, relation type: %s.%n",
                                relatedHealthcareEntity.getText(), entityRelationType));
                        }
                    });
                });
            });
        }
        System.out.printf("Poller status: %s.%n", syncPoller.poll().getStatus());
    }
}
