// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.concurrent.TimeUnit;

/**
 * Async sample for copying a custom model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        FormTrainingAsyncClient sourceClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Instantiate the target client where we want to copy the custom model to.
        FormTrainingAsyncClient targetClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String targetResourceId = "target-resource-Id";
        String targetResourceRegion = "target-resource-region";
        // The Id of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-Id";

        // Get authorization to copy the model to target resource
        targetClient.getCopyAuthorization(targetResourceId, targetResourceRegion)
            // Start copy operation from the source client
            // The Id of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> sourceClient.beginCopyModel(copyModelId, copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                    .subscribe(customFormModelInfo -> {
                        System.out.printf("Original model has model Id: %s, model status: %s, training started on: %s,"
                                + " training completed on: %s.%n",
                            customFormModelInfo.getModelId(),
                            customFormModelInfo.getStatus(),
                            customFormModelInfo.getTrainingStartedOn(),
                            customFormModelInfo.getTrainingCompletedOn());

                        // Get the copied model from the target resource
                        targetClient.getCustomModel(copyAuthorization.getModelId()).subscribe(customFormModel ->
                            System.out.printf("Copied model has model Id: %s, model status: %s, training started on: %s,"
                                    + " training completed on: %s.%n",
                                customFormModel.getModelId(),
                                customFormModel.getModelStatus(),
                                customFormModel.getTrainingStartedOn(),
                                customFormModel.getTrainingCompletedOn()));
                    }));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
