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
        boolean useLabelFile = true;
        formTrainingAsyncClient.beginTraining(trainingSetSource, useLabelFile).subscribe(
            recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(customFormModel -> {
                    System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                    System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                    customFormModel.getSubModels().forEach(customFormSubModel ->
                        customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                            System.out.printf("Form type: %s Field Text: %s Field Accuracy: %s%n",
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
        boolean useLabelFile = true;

        formTrainingAsyncClient.beginTraining(trainingSetSource, useLabelFile, isIncludeSubFolders, filePrefix,
            Duration.ofSeconds(5)).subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(customFormModel -> {
                    System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                    System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                    customFormModel.getSubModels().forEach(customFormSubModel ->
                        customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                            System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
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
                    System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
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
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            CustomFormModel customFormModel = response.getValue();
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            customFormModel.getSubModels().forEach(customFormSubModel ->
                customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                    System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
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
            System.out.printf("Max number of models that can be trained for this account: %s%n",
                accountProperties.getLimit());
            System.out.printf("Current count of trained custom models: %d%n", accountProperties.getCount());
        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountProperties
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getAccountPropertiesWithResponse}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getAccountPropertiesWithResponse
        formTrainingAsyncClient.getAccountPropertiesWithResponse().subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            AccountProperties accountProperties = response.getValue();
            System.out.printf("Max number of models that can be trained for this account: %s%n",
                accountProperties.getLimit());
            System.out.printf("Current count of trained custom models: %d%n", accountProperties.getCount());
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
            System.out.printf("Model Id: %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#deleteModelWithResponse}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.deleteModelWithResponse(modelId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            System.out.printf("Model Id: %s is deleted%n", modelId);
        });
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getModelInfos}
     */
    public void getModelInfos() {
        // BEGIN: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getModelInfos
        formTrainingAsyncClient.getModelInfos().subscribe(customModel ->
            System.out.printf("Model Id: %s, Model status: %s, Created on: %s, Last updated on: %s.%n",
                customModel.getModelId(),
                customModel.getStatus(),
                customModel.getCreatedOn(),
                customModel.getLastUpdatedOn()));
        // END: com.azure.ai.formrecognizer.FormTrainingAsyncClient.getModelInfos
    }
}
