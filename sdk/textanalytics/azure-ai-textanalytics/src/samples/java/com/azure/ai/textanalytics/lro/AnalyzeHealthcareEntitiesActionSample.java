// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesActionResult;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.EntityDataSource;
import com.azure.ai.textanalytics.models.FhirVersion;
import com.azure.ai.textanalytics.models.HealthcareEntity;
import com.azure.ai.textanalytics.models.HealthcareEntityAssertion;
import com.azure.ai.textanalytics.models.HealthcareEntityRelation;
import com.azure.ai.textanalytics.models.HealthcareEntityRelationRole;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute an "Healthcare Entities Analysis" action in a batch of documents.
 */
public class AnalyzeHealthcareEntitiesActionSample {
    /**
     * Main method to invoke this demo about how to analyze an "Healthcare Entities Analysis" action.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        List<String> documents = Arrays.asList(
            "Woman in NAD with a h/o CAD, DM2, asthma and HTN on ramipril for 8 years awoke from sleep around"
                + " 2:30 am this morning of a sore throat and swelling of tongue. She came immediately to the ED"
                + " b/c she was having difficulty swallowing.",
            "Patient's brother died at the age of 64 from lung cancer. She was admitted for likely gastroparesis"
                + " but remains unsure if she wants to start adjuvant hormonal therapy. Please hold lactulose "
                + "if diarrhea worsen.");

        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions()
                    .setDisplayName("{tasks_display_name}")
                    .setAnalyzeHealthcareEntitiesActions(
                        new AnalyzeHealthcareEntitiesAction().setFhirVersion(FhirVersion.V4_0_1)),
                "en",
                new AnalyzeActionsOptions());

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(actionsResult -> processAnalyzeActionsResult(actionsResult));
    }

    private static void processAnalyzeActionsResult(AnalyzeActionsResult actionsResult) {
        System.out.println("Healthcare Entities Analysis action results:");

        for (AnalyzeHealthcareEntitiesActionResult actionResult : actionsResult.getAnalyzeHealthcareEntitiesResults()) {
            if (!actionResult.isError()) {
                for (AnalyzeHealthcareEntitiesResult healthcareEntitiesResult : actionResult.getDocumentsResults()) {
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
            } else {
                System.out.printf("\tCannot execute Healthcare Entities Analysis action. Error: %s%n",
                    actionResult.getError().getMessage());
            }
        }
    }
}
