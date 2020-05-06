// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
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
        FormTrainingClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient().getFormTrainingClient();

        AtomicReference<String> modelId = new AtomicReference<>();

        // First, we see how many custom models we have, and what our limit is
        AccountProperties accountProperties = client.getAccountProperties();
        System.out.printf("The account has %s custom models, and we can have at most %s custom models",
            accountProperties.getCount(), accountProperties.getLimit());

        // Next, we get a paged list of all of our custom models
        PagedIterable<CustomFormModelInfo> customModels = client.getModelInfos();
        System.out.println("We have following models in the account:");
        customModels.forEach(customFormModelInfo -> {
            System.out.printf("Model Id: %s%n", customFormModelInfo.getModelId());
            // get custom model info
            modelId.set(customFormModelInfo.getModelId());
            CustomFormModel customModel = client.getCustomModel(customFormModelInfo.getModelId());
            System.out.printf("Model Id: %s%n", customModel.getModelId());
            System.out.printf("Model Status: %s%n", customModel.getModelStatus());
            System.out.printf("Created on: %s%n", customModel.getCreatedOn());
            System.out.printf("Updated on: %s%n", customModel.getLastUpdatedOn());
            customModel.getSubModels().forEach(customFormSubModel -> {
                System.out.printf("Custom Model Form type: %s%n", customFormSubModel.getFormType());
                System.out.printf("Custom Model Accuracy: %.2f%n", customFormSubModel.getAccuracy());
                if (customFormSubModel.getFieldMap() != null) {
                    customFormSubModel.getFieldMap().forEach((fieldText, customFormModelField) -> {
                        System.out.printf("Field Text: %s%n", fieldText);
                        System.out.printf("Field Accuracy: %.2f%n", customFormModelField.getAccuracy());
                    });
                }

            });
        });

        // Delete Custom Model
        System.out.printf("Deleted model with model Id: %s operation completed with status: %s%n", modelId.get(),
            client.deleteModelWithResponse(modelId.get(), Context.NONE).getStatusCode());
    }
}
