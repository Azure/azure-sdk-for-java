// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.ClassifyDocumentResult;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously execute a "Multi-label Classification".
 */
public class MultiLabelClassifyDocumentAsync {
    /**
     * Main method to invoke this demo about how to analyze an "Multi-label Classification".
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

        // See the service documentation for regional support and how to train a model to classify your documents,
        // see https://aka.ms/azsdk/textanalytics/customfunctionalities
        client.beginMultiLabelClassify(documents,
            "{project_name}",
            "{deployment_name}",
            "en",
            null)
            .flatMap(pollResult -> {
                ClassifyDocumentOperationDetail operationResult = pollResult.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationResult.getCreatedAt(), operationResult.getExpiresAt());
                return pollResult.getFinalResult();
            })
            .flatMap(pagedFluxAsyncPollResponse -> pagedFluxAsyncPollResponse.byPage())
            .subscribe(
                perPage -> processResult(perPage),
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

    private static void processResult(PagedResponse<ClassifyDocumentResultCollection> perPage) {
        System.out.printf("Response code: %d, Continuation Token: %s.%n",
            perPage.getStatusCode(), perPage.getContinuationToken());

        for (ClassifyDocumentResultCollection documentsResults : perPage.getElements()) {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (ClassifyDocumentResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (ClassificationCategory classification : documentResult.getClassifications()) {
                        System.out.printf("\tCategory: %s, confidence score: %f.%n",
                            classification.getCategory(), classification.getConfidenceScore());
                    }
                } else {
                    System.out.printf("\tCannot classify category of document. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        }
    }
}
