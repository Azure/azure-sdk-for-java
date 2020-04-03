// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Sample for demonstrating common custom model management operations.
 */
public class CustomModelOperations {

    /**
     * Main program to invoke the demo for performing operations of a custom model.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormTrainingClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient().getFormTrainingClient();

        String modelId = "{model_Id}";
        // Get Custom Model
        CustomFormModel customModel = client.getCustomModel(modelId);
        System.out.printf("Model Id: %s%n", customModel.getModelId());
        System.out.printf("Model Status: %s%n", customModel.getModelStatus());
        customModel.getSubModels().forEach(customFormSubModel -> {
            System.out.printf("Custom Model Form type: %s%n", customFormSubModel.getFormType());
            System.out.printf("Custom Model Accuracy: %s%n", customFormSubModel.getAccuracy());
            if (customFormSubModel.getFieldMap() != null) {
                customFormSubModel.getFieldMap().forEach((fieldText, customFormModelField) -> {
                    System.out.printf("Field Text: %s%n", fieldText);
                    System.out.printf("Field Accuracy: %s%n", customFormModelField.getAccuracy());
                });
            }
            // Model Training info
            System.out.println("Model Training Info:");
            customModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
                System.out.printf("Training document Name: %s%n", trainingDocumentInfo.getName());
                System.out.printf("Training document Status: %s%n", trainingDocumentInfo.getTrainingStatus());
            });
        });

        // Get model Info
        AccountProperties accountProperties = client.getAccountProperties();
        System.out.println("Account Properties");
        System.out.printf("Model count in subscription : %s%n", modelId, accountProperties.getCount());
        System.out.printf("Model limit in subsciption: %s%n", accountProperties.getLimit());

        // Delete Custom Model
        Response<Void> deletedModel = client.deleteModelWithResponse(modelId, Context.NONE);
        System.out.printf("Deleted model with model Id: %s operation completed with status: %s%n", modelId, deletedModel.getStatusCode());

        // List Custom Model
        // client.listModels()
    }
}
