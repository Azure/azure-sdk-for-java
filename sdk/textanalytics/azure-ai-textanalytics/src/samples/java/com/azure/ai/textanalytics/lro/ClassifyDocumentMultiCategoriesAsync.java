// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.ClassifyDocumentMultiCategoriesAction;
import com.azure.ai.textanalytics.models.ClassifyDocumentMultiCategoriesActionResult;
import com.azure.ai.textanalytics.models.ClassifyDocumentMultiCategoriesResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.ClassifyDocumentMultiCategoriesResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously execute a "Multi-label Classification" action.
 */
public class ClassifyDocumentMultiCategoriesAsync {
    /**
     * Main method to invoke this demo about how to analyze an "Multi-label Classification" action.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
                                              .credential(new AzureKeyCredential("{key}"))
                                              .endpoint("{endpoint}")
                                              .buildAsyncClient();

        List<String> documents = new ArrayList<>();
        documents.add(
            "I need a reservation for an indoor restaurant in China. Please don't stop the music."
                + " Play music and add it to my playlist"
        );

        client.beginAnalyzeActions(documents,
            new TextAnalyticsActions().setClassifyDocumentMultiCategoriesActions(
                new ClassifyDocumentMultiCategoriesAction("{project_name}", "{deployment_name}")),
            "en",
            null)
            .flatMap(result -> {
                AnalyzeActionsOperationDetail operationDetail = result.getValue();
                System.out.printf("Action display name: %s, Successfully completed actions: %d, in-process actions: %d,"
                                      + " failed actions: %d, total actions: %d%n",
                    operationDetail.getDisplayName(), operationDetail.getSucceededCount(),
                    operationDetail.getInProgressCount(), operationDetail.getFailedCount(),
                    operationDetail.getTotalCount());
                return result.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux) // this unwrap the Mono<> of Mono<PagedFlux<T>> to return PagedFlux<T>
            .subscribe(
                actionsResult -> processAnalyzeActionsResult(actionsResult),
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

    private static void processAnalyzeActionsResult(AnalyzeActionsResult actionsResult) {
        for (ClassifyDocumentMultiCategoriesActionResult actionResult : actionsResult.getClassifyDocumentMultiCategoriesResults()) {
            if (!actionResult.isError()) {
                final ClassifyDocumentMultiCategoriesResultCollection documentsResults = actionResult.getDocumentsResults();
                System.out.printf("Project name: %s, deployment name: %s.%n",
                    documentsResults.getProjectName(), documentsResults.getDeploymentName());
                for (ClassifyDocumentMultiCategoriesResult documentResult : documentsResults) {
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
    }
}
