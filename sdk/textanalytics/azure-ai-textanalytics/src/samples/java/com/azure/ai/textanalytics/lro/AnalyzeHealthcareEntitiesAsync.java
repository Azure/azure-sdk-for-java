// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        List<TextDocumentInput> documents = Arrays.asList(
            new TextDocumentInput("0",
                "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                    + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                    + " b/c she was having difficulty swallowing."),
            new TextDocumentInput("1",
                "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                    + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                    + "if diarrhea worsen."));

        AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions().setIncludeStatistics(true);

        client.beginAnalyzeHealthcareEntities(documents, options)
            .flatMap(pollResult -> {
                AnalyzeHealthcareEntitiesOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(analyzeHealthcareEntitiesPagedFlux -> analyzeHealthcareEntitiesPagedFlux.byPage())
            .subscribe(
                perPage -> processAnalyzeHealthcareEntitiesResultCollection(perPage),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processAnalyzeHealthcareEntitiesResultCollection(
        PagedResponse<AnalyzeHealthcareEntitiesResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());
        for (AnalyzeHealthcareEntitiesResultCollection resultCollection : perPage.getElements()) {
            // Model version
            System.out.printf("Results of Azure Text Analytics \"Analyze Healthcare\" Model, version: %s%n",
                resultCollection.getModelVersion());
            // Batch statistics
            TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics();
            System.out.printf("Documents statistics: document count = %s, erroneous document count = %s,"
                                  + " transaction count = %s, valid document count = %s.%n",
                batchStatistics.getDocumentCount(), batchStatistics.getInvalidDocumentCount(),
                batchStatistics.getTransactionCount(), batchStatistics.getValidDocumentCount());
            // Healthcare entities results
            for (AnalyzeHealthcareEntitiesResult healthcareEntitiesResult : resultCollection) {
                System.out.println("Document ID = " + healthcareEntitiesResult.getId());
                System.out.println("Document entities: ");
                // Recognized healthcare entities
                for (HealthcareEntity entity : healthcareEntitiesResult.getEntities()) {
                    System.out.printf(
                        "\tText: %s, normalized name: %s, category: %s, subcategory: %s, confidence score: %f.%n",
                        entity.getText(), entity.getNormalizedText(), entity.getCategory(),
                        entity.getSubcategory(), entity.getConfidenceScore());
                    // Assertion detection
                    HealthcareEntityAssertion assertion = entity.getAssertion();
                    if (assertion != null) {
                        System.out.printf("\t\tEntity assertion: association=%s, certainty=%s, conditionality=%s.%n",
                            assertion.getAssociation(), assertion.getCertainty(), assertion.getConditionality());
                    }
                    // Entity data source links
                    for (EntityDataSource dataSource : entity.getDataSources()) {
                        System.out.printf("\t\tEntity ID in data source: %s, data source: %s.%n",
                            dataSource.getEntityId(), dataSource.getName());
                    }
                }
                // Recognized healthcare entity relation groups
                for (HealthcareEntityRelation entityRelation : healthcareEntitiesResult.getEntityRelations()) {
                    System.out.printf("Relation type: %s.%n", entityRelation.getRelationType());
                    for (HealthcareEntityRelationRole role : entityRelation.getRoles()) {
                        HealthcareEntity entity = role.getEntity();
                        System.out.printf("\tEntity text: %s, category: %s, role: %s.%n",
                            entity.getText(), entity.getCategory(), role.getName());
                    }
                }
            }
        }
    }
}
