// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample for demonstrating common custom model management operations.
 * To learn how to train your own models, look at TrainModelWithoutLabels.java and TrainModelWithLabels.java.
 */
public class ManageCustomModels {

    /**
     * Main program to invoke the demo for performing operations of a custom model.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormTrainingClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        AtomicReference<String> modelId = new AtomicReference<>();

        // First, we see how many custom models we have, and what our limit is
        AccountProperties accountProperties = client.getAccountProperties();
        System.out.printf("The account has %s custom models, and we can have at most %s custom models",
            accountProperties.getCustomModelCount(), accountProperties.getCustomModelLimit());

        // Next, we get a paged list of all of our custom models
        PagedIterable<CustomFormModelInfo> customModels = client.listCustomModels();
        System.out.println("We have following models in the account:");
        customModels.forEach(customFormModelInfo -> {
            System.out.printf("Model Id: %s%n", customFormModelInfo.getModelId());
            if (customFormModelInfo.getCustomModelProperties() != null) {
                System.out.printf("Is it a composed model? : %s%n", customFormModelInfo.getCustomModelProperties().isComposed());
            }
            // get custom model info
            modelId.set(customFormModelInfo.getModelId());
            CustomFormModel customModel = client.getCustomModel(customFormModelInfo.getModelId());
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

        // Delete Custom Model
        System.out.printf("Deleted model with model Id: %s, operation completed with status: %s%n", modelId.get(),
            client.deleteModelWithResponse(modelId.get(), Context.NONE).getStatusCode());
    }
}
