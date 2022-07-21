// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.ClassifyDocumentResult;
import com.azure.ai.textanalytics.models.MultiLabelClassifyAction;
import com.azure.ai.textanalytics.models.MultiLabelClassifyActionResult;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute a "Multi-label Classification" action.
 */
public class ClassifyDocumentMultiCategory {
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

        // See the service documentation for regional support and how to train a model to classify your documents,
        // see https://aka.ms/azsdk/textanalytics/customfunctionalities
        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions().setMultiLabelClassifyActions(
                    new MultiLabelClassifyAction("{project_name}", "{deployment_name}")),
                "en",
                null);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(actionsResult -> {
            for (MultiLabelClassifyActionResult actionResult : actionsResult.getMultiLabelClassifyResults()) {
                if (!actionResult.isError()) {
                    final ClassifyDocumentResultCollection documentsResults = actionResult.getDocumentsResults();
                    System.out.printf("Project name: %s, deployment name: %s.%n",
                        documentsResults.getProjectName(), documentsResults.getDeploymentName());
                    for (ClassifyDocumentResult documentResult : documentsResults) {
                        System.out.println("Document ID: " + documentResult.getId());
                        if (!documentResult.isError()) {
                            for (ClassificationCategory classificationCategory : documentResult.getClassificationCategories()) {
                                System.out.printf("\tCategory: %s, confidence score: %f.%n",
                                    classificationCategory.getCategory(), classificationCategory.getConfidenceScore());
                            }
                        } else {
                            System.out.printf("\tCannot classify multi categories of document. Error: %s%n",
                                documentResult.getError().getMessage());
                        }
                    }
                } else {
                    System.out.printf("\tCannot execute 'MultiCategoryClassifyAction'. Error: %s%n",
                        actionResult.getError().getMessage());
                }
            }
        });
    }
}
