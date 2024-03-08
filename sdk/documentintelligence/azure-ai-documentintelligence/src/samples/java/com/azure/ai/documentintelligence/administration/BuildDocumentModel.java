// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to build a model with training data.
 * For instructions on setting up documents for training in an Azure Storage Blob Container, see
 * <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
 * <p>
 * For this sample, you can use the training documents found in
 * <a href="https://aka.ms/azsdk/formrecognizer/sampletrainingfiles">here</a>
 * to create your own custom document analysis models.
 * For instructions to create a label file for your training forms, please see:
 * <a href="https://aka.ms/azsdk/formrecognizer/labelingtool">here</a>.
 * <p>
 * Further, see AnalyzeCustomDocumentFromUrl.java to analyze a custom document with your built model.
 */
public class BuildDocumentModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAdministrationClient client = new DocumentIntelligenceAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Build custom document analysis model
        String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildOperationPoller =
            client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl)));

        DocumentModelDetails documentModelDetails = buildOperationPoller.getFinalResult();

        // Model Info
        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model created on: %s%n%n", documentModelDetails.getCreatedDateTime());

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
