// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute a "Custom Entities Recognition" action.
 */
public class RecognizeCustomEntities {
    /**
     * Main method to invoke this demo about how to analyze an "Custom Entities Recognition" action.
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

        // See the service documentation for regional support and how to train a model to recognize the custom entities,
        // see https://aka.ms/azsdk/textanalytics/customentityrecognition
        SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable> syncPoller =
            client.beginRecognizeCustomEntities(documents, "en",
              "{project_name}", "{deployment_name}",
                null);

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(documentsResults -> {
            System.out.printf("Project name: %s, deployment name: %s.%n",
                documentsResults.getProjectName(), documentsResults.getDeploymentName());
            for (RecognizeEntitiesResult documentResult : documentsResults) {
                System.out.println("Document ID: " + documentResult.getId());
                if (!documentResult.isError()) {
                    for (CategorizedEntity entity : documentResult.getEntities()) {
                        System.out.printf(
                            "\tText: %s, category: %s, confidence score: %f.%n",
                            entity.getText(), entity.getCategory(), entity.getConfidenceScore());
                    }
                } else {
                    System.out.printf("\tCannot recognize custom entities. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        });
    }
}
