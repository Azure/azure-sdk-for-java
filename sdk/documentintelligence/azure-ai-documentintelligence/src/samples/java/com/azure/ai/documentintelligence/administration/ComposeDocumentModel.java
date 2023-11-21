// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ComponentDocumentModelDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelRequest;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.Arrays;

/**
 * Sample for creating a custom document analysis composed model.
 * <p>
 * This is useful when you have build different analysis models and want to aggregate a group of
 * them into a single model that you (or a user) could use to analyze a custom document. When doing
 * so, you can let the service decide which model more accurately represents the document to
 * analyze, instead of manually trying each built model against the form and selecting
 * the most accurate one.
 * </p>
 */
public class ComposeDocumentModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAdministrationClient client = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Build custom document analysis model
        String model1TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_1}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> model1Poller =
            client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(model1TrainingFiles)));

        // Build custom document analysis model
        String model2TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_2}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> model2Poller =
            client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(model2TrainingFiles)));

        String labeledModelId1 = model1Poller.getFinalResult().getModelId();
        String labeledModelId2 = model2Poller.getFinalResult().getModelId();
        String composedModelId = "my-composed-model";
        final DocumentModelDetails documentModelDetails =
            client.beginComposeModel(
                new ComposeDocumentModelRequest(composedModelId, Arrays.asList(new ComponentDocumentModelDetails(labeledModelId1), new ComponentDocumentModelDetails(labeledModelId2)))
                    .setDescription("my composed model description"))
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Composed model created on: %s%n", documentModelDetails.getCreatedDateTime());

        System.out.println("Document Fields:");
        documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
    }
}

