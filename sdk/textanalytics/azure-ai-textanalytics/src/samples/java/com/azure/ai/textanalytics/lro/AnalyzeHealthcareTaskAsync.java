// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.HealthcareEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeHealthcareEntityOptions;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sample demonstrates how to asynchronously analyze a healthcare task.
 */
public class AnalyzeHealthcareTaskAsync {
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
                "The patient is a 54-year-old gentleman with a history of progressive angina over the past several months."));
        }

        // Request options: show statistics and model version
        RecognizeHealthcareEntityOptions options = new RecognizeHealthcareEntityOptions()
            .setIncludeStatistics(true);

        client.beginAnalyzeHealthcare(documents, options)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(healthcareTaskResultPagedFlux ->
                healthcareTaskResultPagedFlux.subscribe(
                    healthcareTaskResult -> {
                        System.out.printf("Job display name: %s, job ID: %s.%n", healthcareTaskResult.getDisplayName(),
                            healthcareTaskResult.getJobId());

                        RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection = healthcareTaskResult.getResult();
                        // Model version
                        System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                            healthcareEntitiesResultCollection.getModelVersion());

                        // Batch statistics
                        TextDocumentBatchStatistics batchStatistics = healthcareEntitiesResultCollection.getStatistics();
                        System.out.printf("Documents statistics: document count = %s, erroneous document count = %s, transaction count = %s, valid document count = %s.%n",
                            batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(),
                            batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());

                        healthcareEntitiesResultCollection.forEach(healthcareEntitiesResult -> {
                            System.out.println("Document id = " + healthcareEntitiesResult.getId());
                            System.out.println("Document entities: ");
                            HealthcareEntityCollection healthcareEntities = healthcareEntitiesResult.getEntities();
                            AtomicInteger ct = new AtomicInteger();
                            healthcareEntities.forEach(healthcareEntity -> {
                                System.out.printf("\ti = %d, Text: %s, category: %s, subcategory: %s, confidence score: %f.%n",
                                    ct.getAndIncrement(),
                                    healthcareEntity.getText(), healthcareEntity.getCategory(), healthcareEntity.getSubcategory(),
                                    healthcareEntity.getConfidenceScore());
                            });

                            healthcareEntities.getEntityRelations().forEach(
                                healthcareEntityRelation ->
                                    System.out.printf("Is bidirectional: %s, target: %s, source: %s, relation type: %s.%n",
                                        healthcareEntityRelation.isBidirectional(),
                                        healthcareEntityRelation.getTargetLink(),
                                        healthcareEntityRelation.getSourceLink(),
                                        healthcareEntityRelation.getRelationType()));
                        });
                    }
                ));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
