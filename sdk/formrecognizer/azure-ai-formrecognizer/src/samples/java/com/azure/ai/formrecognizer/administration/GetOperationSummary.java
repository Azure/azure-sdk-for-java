// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Sample to get/list all document model operations associated with the Form Recognizer resource.
 * Kinds of operations returned are "documentModelBuild", "documentModelCompose", and "documentModelCopyTo".
 * Note that operation information only persists for 24 hours.
 * If the operation was successful, the document model can be accessed using getDocumentModel() or listDocumentModels() APIs
 */
public class GetOperationSummary {

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

        client.listOperations().forEach(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperationSummary.getKind());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation resource location %s%n", modelOperationSummary.getResourceLocation());
            System.out.printf("Operation percent completion status: %d%n", modelOperationSummary.getPercentCompleted());

            // get the specific operation info
            OperationDetails modelOperationDetails =
                client.getOperation(modelOperationSummary.getOperationId());
            if (OperationStatus.FAILED.equals(modelOperationSummary.getStatus())) {
                System.out.printf("Operation fail error: %s%n", modelOperationDetails.getError().getMessage());
            } else {
                System.out.printf("Model ID created with this operation: %s%n",
                    ((DocumentModelBuildOperationDetails) modelOperationDetails).getResult().getModelId());
            }
        });

    }
}
