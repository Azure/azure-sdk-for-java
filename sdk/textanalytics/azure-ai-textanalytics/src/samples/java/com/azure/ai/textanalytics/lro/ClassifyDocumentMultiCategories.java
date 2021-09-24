// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.MultiCategoryClassifyAction;
import com.azure.ai.textanalytics.models.MultiCategoryClassifyActionResult;
import com.azure.ai.textanalytics.models.MultiCategoryClassifyResult;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.MultiCategoryClassifyResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute a "Multi-label Classification" action.
 */
public class ClassifyDocumentMultiCategories {
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
                new TextAnalyticsActions().setMultiCategoryClassifyActions(
                    new MultiCategoryClassifyAction("{project_name}", "{deployment_name}")),
                "en",
                null);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(actionsResult -> {
            for (MultiCategoryClassifyActionResult actionResult : actionsResult.getMultiCategoryClassifyResults()) {
                if (!actionResult.isError()) {
                    final MultiCategoryClassifyResultCollection documentsResults = actionResult.getDocumentsResults();
                    System.out.printf("Project name: %s, deployment name: %s.%n",
                        documentsResults.getProjectName(), documentsResults.getDeploymentName());
                    for (MultiCategoryClassifyResult documentResult : documentsResults) {
                        System.out.println("Document ID: " + documentResult.getId());
                        if (!documentResult.isError()) {
                            for (ClassificationCategory classificationCategory : documentResult.getClassifications()) {
                                System.out.printf("\tCategory: %s, confidence score: %f.%n",
                                    classificationCategory.getCategory(), classificationCategory.getConfidenceScore());
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
