// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.concurrent.TimeUnit;

/**
 * Async sample for copying a custom document analysis model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        DocumentModelAdministrationAsyncClient sourceClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Instantiate the target client where we want to copy the custom document analysis model to.
        DocumentModelAdministrationAsyncClient targetClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String copiedModelId = "my-copied-model";
        // The ID of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-ID";

        // Get authorization to copy the model to target resource
        targetClient.getCopyAuthorization(copiedModelId)
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> sourceClient.beginCopyModel(copyModelId, copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                    .subscribe(documentModelInfo -> {
                        System.out.printf("Original model has model ID: %s and was created on: %s.%n",
                            documentModelInfo.getModelId(),
                            documentModelInfo.getCreatedOn());

                        // Get the copied model from the target resource
                        targetClient.getModel(copyAuthorization.getTargetModelId()).subscribe(documentModel ->
                            System.out.printf("Copied model has model ID: %s was created on: %s.%n",
                                documentModel.getModelId(),
                                documentModel.getCreatedOn()));
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
