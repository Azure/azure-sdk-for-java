// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.SingleCategoryClassifyAction;
import com.azure.ai.textanalytics.models.SingleCategoryClassifyActionResult;
import com.azure.ai.textanalytics.models.SingleCategoryClassifyResult;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously execute an "Single-label Classification" action.
 */
public class ClassifyDocumentSingleCategoryAsync {
    /**
     * Main method to invoke this demo about how to analyze an "Single-label Classification" action.
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
        client.beginAnalyzeActions(documents,
            new TextAnalyticsActions().setSingleCategoryClassifyActions(
                new SingleCategoryClassifyAction("{project_name}", "{deployment_name}")),
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
        for (SingleCategoryClassifyActionResult actionResult : actionsResult.getSingleCategoryClassifyResults()) {
            if (!actionResult.isError()) {
                SingleCategoryClassifyResultCollection documentsResults = actionResult.getDocumentsResults();
                System.out.printf("Project name: %s, deployment name: %s.%n",
                    documentsResults.getProjectName(), documentsResults.getDeploymentName());
                for (SingleCategoryClassifyResult documentResult : documentsResults) {
                    System.out.println("Document ID: " + documentResult.getId());
                    if (!documentResult.isError()) {
                        ClassificationCategory classificationCategory = documentResult.getClassification();
                        System.out.printf("\tCategory: %s, confidence score: %f.%n",
                            classificationCategory.getCategory(), classificationCategory.getConfidenceScore());
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
    }
}
