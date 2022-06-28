// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.ClassifiedCategory;
import com.azure.ai.textanalytics.models.SingleLabelClassifyAction;
import com.azure.ai.textanalytics.models.SingleLabelClassifyActionResult;
import com.azure.ai.textanalytics.models.LabelClassifyResult;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute a "Single-label Classification" action.
 */
public class ClassifyDocumentSingleCategory {
    /**
     * Main method to invoke this demo about how to analyze an "Single-label Classification" action.
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
            "A recent report by the Government Accountability Office (GAO) found that the dramatic increase "
                + "in oil and natural gas development on federal lands over the past six years has stretched the"
                + " staff of the BLM to a point that it has been unable to meet its environmental protection "
                + "responsibilities.");
        documents.add(
            "David Schmidt, senior vice president--Food Safety, International Food"
                + " Information Council (IFIC), Washington, D.C., discussed the physical activity component."
        );
        documents.add(
            "I need a reservation for an indoor restaurant in China. Please don't stop the music. Play music and add it to my playlist"
        );

        // See the service documentation for regional support and how to train a model to classify your documents,
        // see https://aka.ms/azsdk/textanalytics/customfunctionalities
        SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> syncPoller =
            client.beginAnalyzeActions(documents,
                new TextAnalyticsActions().setSingleLabelClassifyActions(
                    new SingleLabelClassifyAction("{project_name}", "{deployment_name}")),
                "en",
                null);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(actionsResult -> {
            for (SingleLabelClassifyActionResult actionResult : actionsResult.getSingleLabelClassifyResults()) {
                if (!actionResult.isError()) {
                    LabelClassifyResultCollection documentsResults = actionResult.getDocumentsResults();
                    System.out.printf("Project name: %s, deployment name: %s.%n",
                        documentsResults.getProjectName(), documentsResults.getDeploymentName());
                    for (LabelClassifyResult documentResult : documentsResults) {
                        System.out.println("Document ID: " + documentResult.getId());
                        if (!documentResult.isError()) {
                            for (ClassifiedCategory classifiedCategory : documentResult.getClassifiedCategories()) {
                                System.out.printf("\tCategory: %s, confidence score: %f.%n",
                                    classifiedCategory.getCategory(), classifiedCategory.getConfidenceScore());
                            }
                        } else {
                            System.out.printf("\tCannot classify category of document. Error: %s%n",
                                documentResult.getError().getMessage());
                        }
                    }
                } else {
                    System.out.printf("\tCannot execute 'ClassifyCustomSingleCategoryAction'. Error: %s%n",
                        actionResult.getError().getMessage());
                }
            }
        });
    }
}
