// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample to get/list all document model operations associated with the Form Recognizer resource.
 * Kinds of operations returned are "documentModelBuild", "documentModelCompose", and "documentModelCopyTo".
 * Note that operation information only persists for 24 hours.
 * If the operation was successful, the document model can be accessed using get_model or list_models APIs
 */
public class GetOperationInfo {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        client.listOperations().forEach(modelOperationInfo -> {
            System.out.printf("Operation ID: %s%n", modelOperationInfo.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperationInfo.getKind());
            System.out.printf("Operation Status: %s%n", modelOperationInfo.getStatus());
            System.out.printf("Operation resource location %s%n", modelOperationInfo.getResourceLocation());
            System.out.printf("Operation percent completion status: %d%n", modelOperationInfo.getPercentCompleted());

            // get the specific operation info
            ModelOperation modelOperation =
                client.getOperation(modelOperationInfo.getOperationId());
            System.out.printf("Model ID created with this operation: %s%n", modelOperation.getModelId());
            if (ModelOperationStatus.FAILED.equals(modelOperationInfo.getStatus())) {
                System.out.printf("Operation fail error: %s%n", modelOperation.getError().getMessage());
            }
        });

    }
}
