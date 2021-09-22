// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.ClassifyCustomMultiCategoriesAction;
import com.azure.ai.textanalytics.models.ClassifyCustomMultiCategoriesActionResult;
import com.azure.ai.textanalytics.models.ClassifyCustomMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.ClassifyCustomMultiCategoriesResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute a "Multi-label Classification" action.
 */
public class ClassifyCustomMultiCategories {
    /**
     * Main method to invoke this demo about how to analyze an "Multi-label Classification" action.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                                         .credential(new AzureKeyCredential("{key}"))
                                         .endpoint("{endpoint}")
                                         .buildClient();

        List<String> documents = new ArrayList<>();
        documents.add(
            "I need a reservation for an indoor restaurant in China. Please don't stop the music."
                + " Play music and add it to my playlist"
        );

        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions().setClassifyCustomMultiCategoriesActions(
                    new ClassifyCustomMultiCategoriesAction("{project_name}", "{deployment_name}")),
                "en",
                null);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(actionsResult -> {
            for (ClassifyCustomMultiCategoriesActionResult actionResult : actionsResult.getClassifyCustomMultiCategoriesResults()) {
                if (!actionResult.isError()) {
                    final ClassifyCustomMultiCategoriesResultCollection documentsResults = actionResult.getDocumentsResults();
                    System.out.printf("Project name: %s, deployment name: %s.%n",
                        documentsResults.getProjectName(), documentsResults.getDeploymentName());
                    for (ClassifyCustomMultiCategoriesResult documentResult : documentsResults) {
                        System.out.println("Document ID: " + documentResult.getId());
                        if (!documentResult.isError()) {
                            for (DocumentClassification documentClassification : documentResult.getDocumentClassifications()) {
                                System.out.printf("\tCategory: %s, confidence score: %f.%n",
                                    documentClassification.getCategory(), documentClassification.getConfidenceScore());
                            }
                        } else {
                            System.out.printf("\tCannot classify multi categories of document. Error: %s%n",
                                documentResult.getError().getMessage());
                        }
                    }
                } else {
                    System.out.printf("\tCannot execute 'ClassifyCustomMultiCategoriesAction'. Error: %s%n",
                        actionResult.getError().getMessage());
                }
            }
        });
    }
}
