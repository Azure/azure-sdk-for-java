// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
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
        // Instantiate a source client which has the model that we want to copy.
        FormTrainingClient sourceClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Instantiate the target client where we want to copy the custom model to.
        FormTrainingClient targetClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String targetResourceId = "target-resource-Id";
        String targetResourceRegion = "target-resource-region";

        // Get authorization to copy the model to target resource
        CopyAuthorization modelCopyAuthorization = targetClient.getCopyAuthorization(targetResourceId,
            targetResourceRegion);

        // The Id of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-Id";
        // Start copy operation from the source client
        SyncPoller<FormRecognizerOperationResult, CustomFormModelInfo> copyPoller = sourceClient.beginCopyModel(copyModelId,
            modelCopyAuthorization);
        copyPoller.waitForCompletion();

        // Get the copied model
        CustomFormModel copiedModel = targetClient.getCustomModel(modelCopyAuthorization.getModelId());

        System.out.printf("Copied model has model Id: %s, model status: %s, was created on: %s,"
                + " transfer completed on: %s.%n",
            copiedModel.getModelId(),
            copiedModel.getModelStatus(),
            copiedModel.getTrainingStartedOn(),
            copiedModel.getTrainingCompletedOn());
    }
}

