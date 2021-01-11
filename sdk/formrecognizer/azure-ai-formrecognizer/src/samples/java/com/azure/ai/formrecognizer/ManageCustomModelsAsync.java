// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Async sample for demonstrating to perform common custom model management operations on your account.
 * To learn how to train your own models, look at TrainModelWithoutLabels.java and TrainModelWithLabels.java.
 */
public class ManageCustomModelsAsync {

    /**
     * Main program to invoke the demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormTrainingAsyncClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        AtomicReference<String> modelId = new AtomicReference<>();

        // First, we see how many custom models we have, and what our limit is
        client.getAccountProperties().subscribe(accountProperties ->
            System.out.printf("The account has %s custom models, and we can have at most %s custom models.%n",
                accountProperties.getCustomModelCount(), accountProperties.getCustomModelLimit()));
        // Next, we get a paged list of all of our custom models
        System.out.println("We have following models in the account:");
        client.listCustomModels().subscribe(customFormModelInfo -> {
            String createdModelId = customFormModelInfo.getModelId();
            System.out.printf("Model Id: %s%n", createdModelId);
            if (customFormModelInfo.getCustomModelProperties() != null) {
                System.out.printf("Is it a composed model? : %s%n", customFormModelInfo.getCustomModelProperties().isComposed());
            }
            // get custom model info
            modelId.set(createdModelId);
            client.getCustomModel(customFormModelInfo.getModelId()).subscribe(customModel -> {
                System.out.printf("Model Id: %s%n", customModel.getModelId());
                System.out.printf("Model Status: %s%n", customModel.getModelStatus());
                System.out.printf("Training started on: %s%n", customModel.getTrainingStartedOn());
                System.out.printf("Training completed on: %s%n", customModel.getTrainingCompletedOn());
                customModel.getSubmodels().forEach(customFormSubmodel -> {
                    System.out.printf("Custom Model Form type: %s%n", customFormSubmodel.getFormType());
                    System.out.printf("Custom Model Accuracy: %.2f%n", customFormSubmodel.getAccuracy());
                    if (customFormSubmodel.getFields() != null) {
                        customFormSubmodel.getFields().forEach((fieldText, customFormModelField) -> {
                            System.out.printf("Field Text: %s%n", fieldText);
                            System.out.printf("Field Accuracy: %.2f%n", customFormModelField.getAccuracy());
                        });
                    }
                });
            });
        });

        // Delete Custom Model
        client.deleteModel(modelId.get());
        System.out.printf("Deleted model with model Id: %s%n", modelId.get());

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
