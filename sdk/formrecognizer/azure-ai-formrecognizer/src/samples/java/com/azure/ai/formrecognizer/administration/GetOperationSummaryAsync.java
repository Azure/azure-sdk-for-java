// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Async sample to get/list all document model operations associated with the Form Recognizer resource.
 * Kinds of operations returned are "documentModelBuild", "documentModelCompose", and "documentModelCopyTo".
 * Note that operation information only persists for 24 hours.
 * If the operation was successful, the document model can be accessed using get_model or list_models APIs
 */
public class GetOperationSummaryAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationAsyncClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        client.listOperations().subscribe(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperationSummary.getKind());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation resource location %s%n", modelOperationSummary.getResourceLocation());
            System.out.printf("Operation percent completion status: %d%n", modelOperationSummary.getPercentCompleted());

            // get the specific operation info
            client.getOperation(modelOperationSummary.getOperationId()).subscribe(modelOperationDetails -> {
                if (OperationStatus.FAILED.equals(modelOperationSummary.getStatus())) {
                    System.out.printf("Operation fail error: %s%n", modelOperationDetails.getError().getMessage());
                } else {
                    System.out.printf("Model ID created with this operation: %s%n",
                        ((DocumentModelBuildOperationDetails) modelOperationDetails).getResult().getModelId());
                }
            });

        });

    }
}
