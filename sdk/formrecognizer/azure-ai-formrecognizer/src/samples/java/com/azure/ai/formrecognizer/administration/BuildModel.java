// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
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
public class BuildModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Build custom document analysis model
        String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<DocumentOperationResult, DocumentModelInfo> buildOperationPoller =
            client.beginBuildModel(new AzureBlobContentSource().setContainerUrl(trainingFilesUrl),
                DocumentBuildMode.TEMPLATE,
                new BuildModelOptions()
                    .setModelId("custom-model-id")
                    .setDescription("model desc"),
                Context.NONE);

        DocumentModelInfo documentModelInfo = buildOperationPoller.getFinalResult();

        // Model Info
        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model created on: %s%n%n", documentModelInfo.getCreatedOn());

        System.out.println("Document Fields:");
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
    }
}
