// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;

import java.time.Duration;

/**
 * Code snippet for {@link FormTrainingAsyncClient}
 */
public class FormTrainingAsyncClientJavaDocCodeSnippets {
    private FormTrainingAsyncClient formTrainingAsyncClient = new FormRecognizerClientBuilder().buildAsyncClient()
        .getFormTrainingAsyncClient();

    /**
     * Code snippet for {@link FormTrainingAsyncClient} initialization
     */
    public void formTrainingAsyncClientInInitialization() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.initialization
        FormTrainingAsyncClient formTrainingAsyncClient = new FormRecognizerClientBuilder().buildAsyncClient()
            .getFormTrainingAsyncClient();
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.initialization
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginTraining}
     */
    public void beginTraining() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.beginTraining#string-boolean
        String trainingSetSource = "{training-set-SAS-URL}";
        formTrainingAsyncClient.beginTraining(trainingSetSource, true).subscribe(
            trainingOperationResponse -> {
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(customFormModel -> {
                    System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                    System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                    customFormModel.getSubModels().forEach(customFormSubModel ->
                        customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                            System.out.printf("Model Type Id: %s Field Text: %s Field Accuracy: %s%n",
                                key, customFormModelField.getName(), customFormModelField.getAccuracy())));
                    });
            });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.beginTraining#string-boolean
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginTraining} with options
     */
    public void beginTrainingWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.beginTraining#string-boolean-boolean-string-Duration
        String trainingSetSource = "{training-set-SAS-URL}";
        boolean isIncludeSubFolders = false; // {is-include-subfolders}
        String filePrefix = "{file-prefix}";
        formTrainingAsyncClient.beginTraining(trainingSetSource, true, isIncludeSubFolders, filePrefix,
            Duration.ofSeconds(5)).subscribe(trainingOperationResponse -> {
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(customFormModel -> {
                    System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                    System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                    customFormModel.getSubModels().forEach(customFormSubModel ->
                        customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                            System.out.printf("Model Type Id: %s Field Text: %s Field Accuracy: %s%n",
                                key, customFormModelField.getName(), customFormModelField.getAccuracy())));
                });
            });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.beginTraining#string-boolean-boolean-string-Duration
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCustomModel}
     */
    public void getCustomModel() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getCustomModel#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.getCustomModel(modelId).subscribe(customFormModel -> {
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            customFormModel.getSubModels().forEach(customFormSubModel ->
                customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                    System.out.printf("Model Type Id: %s Field Text: %s Field Accuracy: %s%n",
                        key, customFormModelField.getName(), customFormModelField.getAccuracy())));

        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getCustomModel#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCustomModelWithResponse}
     */
    public void getCustomModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getCustomModelWithResponse#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.getCustomModelWithResponse(modelId).subscribe(response -> {
            CustomFormModel customFormModel = response.getValue();
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            customFormModel.getSubModels().forEach(customFormSubModel ->
                customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                    System.out.printf("Model Type Id: %s Field Text: %s Field Accuracy: %s%n",
                        key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getCustomModelWithResponse#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getAccountProperties}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountProperties
        formTrainingAsyncClient.getAccountProperties().subscribe(accountProperties -> {
            System.out.printf("Account properties limit: %s%n", accountProperties.getLimit());
            System.out.printf("Account properties count: %d%n", accountProperties.getCount());
        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountProperties
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getAccountPropertiesWithResponse}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountPropertiesWithResponse
        formTrainingAsyncClient.getAccountPropertiesWithResponse().subscribe(response -> {
            AccountProperties accountProperties = response.getValue();
            System.out.printf("Account properties limit: %s%n", accountProperties.getLimit());
            System.out.printf("Account properties count: %d%n", accountProperties.getCount());
        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountPropertiesWithResponse
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#deleteModel}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.deleteModel(modelId).subscribe(val ->
            System.out.printf("Model ID = %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#deleteModelWithResponse}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.deleteModelWithResponse(modelId).subscribe(val ->
            System.out.printf("Model ID = %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#listModels}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.listModels
        formTrainingAsyncClient.listModels().subscribe(result ->
            System.out.printf("Model ID = %s, model status = %s, created on = %s, last updated on = %s.%n",
                result.getModelId(),
                result.getStatus(),
                result.getCreatedOn(),
                result.getLastUpdatedOn()));
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.listModels
    }
}
