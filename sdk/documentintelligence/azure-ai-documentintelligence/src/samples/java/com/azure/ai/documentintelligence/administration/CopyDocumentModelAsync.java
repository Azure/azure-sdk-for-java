// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationAsyncClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Async sample for copying a custom document analysis model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyDocumentModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        DocumentIntelligenceAdministrationAsyncClient sourceClient = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Instantiate the target client where we want to copy the custom document analysis model to.
        DocumentIntelligenceAdministrationAsyncClient targetClient = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String copiedModelId = "my-copied-model";
        // The ID of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-ID";

        // Get authorization to copy the model to target resource
        targetClient.authorizeModelCopy(new AuthorizeCopyRequest(copyModelId))
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> sourceClient.beginCopyModelTo(copyModelId, copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(asyncPollResponse -> asyncPollResponse.getFinalResult())
                    .subscribe(documentModelInfo -> {
                        System.out.printf("Original model has model ID: %s and was created on: %s.%n",
                            documentModelInfo.getModelId(),
                            documentModelInfo.getCreatedDateTime());

                        // Get the copied model from the target resource
                        targetClient.getModel(copyAuthorization.getTargetModelId()).subscribe(documentModel ->
                            System.out.printf("Copied model has model ID: %s was created on: %s.%n",
                                documentModel.getModelId(),
                                documentModel.getCreatedDateTime()));
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
