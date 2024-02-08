// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.CopyAuthorization;
import com.azure.ai.documentintelligence.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.core.credential.AzureKeyCredential;
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
        DocumentIntelligenceAdministrationClient sourceClient = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Instantiate the target client where we want to copy the custom document analysis model to.
        DocumentIntelligenceAdministrationClient targetClient = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String copiedModelId = "my-copied-model";

        // Get authorization to copy the model to target resource
        CopyAuthorization modelDocumentModelCopyAuthorization
            = targetClient.authorizeModelCopy(new AuthorizeCopyRequest(copiedModelId));

        // The ID of the model that needs to be copied to the target resource
        String copyModelId = "copy-model-ID";
        // Start copy operation from the source client
        SyncPoller<DocumentModelCopyToOperationDetails, DocumentModelDetails> copyPoller = sourceClient.beginCopyModelTo(copyModelId,
            modelDocumentModelCopyAuthorization);
        copyPoller.waitForCompletion();

        // Get the copied model
        DocumentModelDetails copiedModel = targetClient.getModel(modelDocumentModelCopyAuthorization.getTargetModelId());

        System.out.printf("Copied model has model ID: %s, was created on: %s.%n",
            copiedModel.getModelId(),
            copiedModel.getCreatedDateTime());
    }
}

