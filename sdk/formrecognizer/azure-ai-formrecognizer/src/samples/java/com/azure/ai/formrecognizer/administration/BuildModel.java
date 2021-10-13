// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to build a model with training data.
 * For instructions on setting up documents for training in an Azure Storage Blob Container, see
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>.
 * <p>
 * For this sample, you can use the training documents found in
 * <a href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/resources/sample-forms/training">here</a>
 * to create your own custom document analysis models.
 * For instructions to create a label file for your training forms, please see:
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/label-tool?tabs=v2-1">here</a>.
 * <p>
 * Further, see AnalyzeCustomDocument.java to analyze a custom document with your built model.
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
        SyncPoller<DocumentOperationResult, DocumentModel> buildOperationPoller =
            client.beginBuildModel(trainingFilesUrl,
                "my-build-model",
                new BuildModelOptions().setDescription("model desc"),
                Context.NONE);

        DocumentModel documentModel = buildOperationPoller.getFinalResult();

        // Model Info
        System.out.printf("Model ID: %s%n", documentModel.getModelId());
        System.out.printf("Model Description: %s%n", documentModel.getDescription());
        System.out.printf("Model created on: %s%n%n", documentModel.getCreatedOn());

        System.out.println("Document Fields:");
        documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
    }
}
