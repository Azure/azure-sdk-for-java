// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Async sample for demonstrating to perform common custom document analysis model management operations on your Form
 * Recognizer resource.
 * To learn how to build your own models, look at BuildModelAsync.java and BuildModel.java.
 */
public class ManageCustomModelsAsync {

    /**
     * Main program to invoke the demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentModelAdministrationAsyncClient client = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        AtomicReference<String> modelId = new AtomicReference<>();

        // First, we see how many models we have, and what our limit is
        client.getAccountProperties().subscribe(accountProperties ->
            System.out.printf("The account has %s  models, and we can have at most %s models.%n",
                accountProperties.getDocumentModelCount(), accountProperties.getDocumentModelLimit()));
        // Next, we get a paged list of all of our models
        System.out.println("We have following models in the account:");
        client.listModels().subscribe(documentModelInfo -> {
            String createdModelId = documentModelInfo.getModelId();
            System.out.printf("Model ID: %s%n", createdModelId);

            // get custom document analysis model info
            modelId.set(createdModelId);
            client.getModel(documentModelInfo.getModelId()).subscribe(documentModel -> {
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
        });

        // Delete Custom Model
        client.deleteModel(modelId.get());
        System.out.printf("Deleted model with model ID: %s%n", modelId.get());

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
