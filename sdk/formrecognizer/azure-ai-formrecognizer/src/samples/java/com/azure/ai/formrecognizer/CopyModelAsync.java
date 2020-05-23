// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Async sample for copying a custom model from a source Form Recognizer resource to a target Form Recognizer resource.
 */
public class CopyModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     *
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingAsyncClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String targetResourceId = "target-resource-Id";
        String targetResourceRegion = "target-resource-region";
        String copyModelId = "copy-model-Id";

        client.getCopyAuthorization(targetResourceId, targetResourceRegion)
            .subscribe(copyAuthorization -> client.beginCopyModel(copyModelId, copyAuthorization)
                .subscribe(copyPoller -> copyPoller.getFinalResult()
                    .subscribe(customFormModelInfo -> {
                        System.out.printf("Copied model has model Id: %s, model status: %s, was created on: %s,"
                            + " last updated on: %s.%n",
                            customFormModelInfo.getModelId(),
                            customFormModelInfo.getStatus(),
                            customFormModelInfo.getCreatedOn(),
                            customFormModelInfo.getLastUpdatedOn());
                    })));
    }
}
