// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for copying a custom document analysis model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyDocumentModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        DocumentModelAdministrationClient sourceClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Instantiate the target client where we want to copy the custom document analysis model to.
        DocumentModelAdministrationClient targetClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String copiedModelId = "my-copied-model";

        // Get authorization to copy the model to target resource
        DocumentModelCopyAuthorization modelDocumentModelCopyAuthorization
            = targetClient.getCopyAuthorizationWithResponse(new CopyAuthorizationOptions().setModelId(copiedModelId),
            Context.NONE).getValue();

        // The ID of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-ID";
        // Start copy operation from the source client
        SyncPoller<OperationResult, DocumentModelDetails> copyPoller = sourceClient.beginCopyDocumentModelTo(copyModelId,
            modelDocumentModelCopyAuthorization);
        copyPoller.waitForCompletion();

        // Get the copied model
        DocumentModelDetails copiedModel = targetClient.getDocumentModel(modelDocumentModelCopyAuthorization.getTargetModelId());

        System.out.printf("Copied model has model ID: %s, was created on: %s.%n",
            copiedModel.getModelId(),
            copiedModel.getCreatedOn());
    }
}

