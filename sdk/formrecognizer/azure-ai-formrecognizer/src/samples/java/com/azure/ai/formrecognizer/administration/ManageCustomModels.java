// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample for demonstrating common custom document analysis model management operations.
 * To learn how to build your own models, look at BuildModel.java and BuildModelAsync.java.
 */
public class ManageCustomModels {

    /**
     * Main program to invoke the demo for performing operations of a custom document analysis model.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        AtomicReference<String> modelId = new AtomicReference<>();

        // First, we see how many models we have, and what our limit is
        AccountProperties accountProperties = client.getAccountProperties();
        System.out.printf("The account has %s models, and we can have at most %s models",
            accountProperties.getDocumentModelCount(), accountProperties.getDocumentModelLimit());

        // Next, we get a paged list of all of our models
        PagedIterable<DocumentModelInfo> customDocumentModels = client.listModels();
        System.out.println("We have following models in the account:");
        customDocumentModels.forEach(documentModelInfo -> {
            System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());

            // get custom document analysis model info
            modelId.set(documentModelInfo.getModelId());
            DocumentModel documentModel = client.getModel(documentModelInfo.getModelId());
            System.out.printf("Model ID: %s%n", documentModel.getModelId());
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model created on: %s%n", documentModel.getCreatedOn());
            documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                });
            });
        });

        // Delete Custom Model
        System.out.printf("Deleted model with model ID: %s, operation completed with status: %s%n", modelId.get(),
            client.deleteModelWithResponse(modelId.get(), Context.NONE).getStatusCode());
    }
}
