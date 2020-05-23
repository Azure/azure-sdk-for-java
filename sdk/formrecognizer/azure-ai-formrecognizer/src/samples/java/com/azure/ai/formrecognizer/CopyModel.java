// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CopyAuthorization;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for copying a custom model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String targetResourceId = "target-resource-Id";
        String targetResourceRegion = "target-resource-region";
        final String copyModelId = "copy-model-Id";

        // Get authorization to copy the model to target resource
        final CopyAuthorization modelCopyAuthorization =
            client.getCopyAuthorization(targetResourceId, targetResourceRegion);

        // Start copy operation
        SyncPoller<OperationResult, CustomFormModelInfo> copyPoller = client.beginCopyModel(copyModelId, modelCopyAuthorization);

        // Get the copied model
        CustomFormModelInfo modelCopy = copyPoller.getFinalResult();

        System.out.printf("Copied model has model Id: %s, model status: %s, was created on: %s,"
                + " last updated on: %s.%n",
            modelCopy.getModelId(),
            modelCopy.getStatus(),
            modelCopy.getCreatedOn(),
            modelCopy.getLastUpdatedOn());
    }
}

