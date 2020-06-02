// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.TrainingFileFilter;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.Duration;

/**
 * Code snippet for {@link FormTrainingClient}
 */
public class FormTrainingClientJavaDocCodeSnippets {
    private FormTrainingClient formTrainingClient = new FormTrainingClientBuilder().buildClient();

    /**
     * Code snippet for {@link FormTrainingClient} initialization
     */
    public void formTrainingClientInInitialization() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.initialization
        FormTrainingClient formTrainingClient = new FormTrainingClientBuilder().buildClient();
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.initialization
    }

    /**
     * Code snippet for {@link FormTrainingClient#beginTraining(String, boolean)}
     */
    public void beginTraining() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean
        String trainingFilesUrl = "{training-set-SAS-URL}";
        boolean useTrainingLabels = true;
        CustomFormModel customFormModel =
            formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels).getFinalResult();
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        customFormModel.getSubmodels().forEach(customFormSubmodel ->
            customFormSubmodel.getFieldMap().forEach((key, customFormModelField) ->
                System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
                    key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean
    }

    /**
     * Code snippet for {@link FormTrainingClient#beginTraining(String, boolean, TrainingFileFilter, Duration)}
     * with options
     */
    public void beginTrainingWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean-trainingFileFilter-Duration
        String trainingFilesUrl = "{training-set-SAS-URL}";
        TrainingFileFilter trainingFileFilter = new TrainingFileFilter().setIncludeSubFolders(false).setPrefix("Invoice");
        boolean useTrainingLabels = true;

        CustomFormModel customFormModel = formTrainingClient.beginTraining(trainingFilesUrl, useTrainingLabels,
                trainingFileFilter, Duration.ofSeconds(5)).getFinalResult();

        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        customFormModel.getSubmodels().forEach(customFormSubmodel ->
            customFormSubmodel.getFieldMap().forEach((key, customFormModelField) ->
                System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
                    key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.beginTraining#string-boolean-trainingFileFilter-Duration
    }

    /**
     * Code snippet for {@link FormTrainingClient#getCustomModel(String)}
     */
    public void getCustomModel() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModel#string
        String modelId = "{model_id}";
        CustomFormModel customFormModel = formTrainingClient.getCustomModel(modelId);
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        customFormModel.getSubmodels().forEach(customFormSubmodel ->
            customFormSubmodel.getFieldMap().forEach((key, customFormModelField) ->
                System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %s%n",
                    key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModel#string
    }

    /**
     * Code snippet for {@link FormTrainingClient#getCustomModelWithResponse(String, Context)}
     */
    public void getCustomModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModelWithResponse#string-Context
        String modelId = "{model_id}";
        Response<CustomFormModel> response = formTrainingClient.getCustomModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        CustomFormModel customFormModel = response.getValue();
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        customFormModel.getSubmodels().forEach(customFormSubmodel ->
            customFormSubmodel.getFieldMap().forEach((key, customFormModelField) ->
                System.out.printf("Field: %s Field Text: %s Field Accuracy: %s%n",
                    key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.getCustomModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link FormTrainingClient#getAccountProperties()}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountProperties
        AccountProperties accountProperties = formTrainingClient.getAccountProperties();
        System.out.printf("Max number of models that can be trained for this account: %s%n",
            accountProperties.getCustomModelLimit());
        System.out.printf("Current count of trained custom models: %d%n", accountProperties.getCustomModelCount());
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountProperties
    }

    /**
     * Code snippet for {@link FormTrainingClient#getAccountPropertiesWithResponse(Context)}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountPropertiesWithResponse#Context
        Response<AccountProperties> response = formTrainingClient.getAccountPropertiesWithResponse(Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        AccountProperties accountProperties = response.getValue();
        System.out.printf("Max number of models that can be trained for this account: %s%n",
            accountProperties.getCustomModelLimit());
        System.out.printf("Current count of trained custom models: %d%n", accountProperties.getCustomModelCount());
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.getAccountPropertiesWithResponse#Context
    }

    /**
     * Code snippet for {@link FormTrainingClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModel#string
        String modelId = "{model_id}";
        formTrainingClient.deleteModel(modelId);
        System.out.printf("Model Id: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModel#string
    }

    /**
     * Code snippet for {@link FormTrainingClient#deleteModelWithResponse(String, Context)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModelWithResponse#string-Context
        String modelId = "{model_id}";
        Response<Void> response = formTrainingClient.deleteModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Model Id: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.deleteModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link FormTrainingClient#listCustomModels()}
     */
    public void listCustomModels() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels
        formTrainingClient.listCustomModels().forEach(customModel ->
            System.out.printf("Model Id: %s, Model status: %s, Created on: %s, Last updated on: %s.%n",
                customModel.getModelId(),
                customModel.getStatus(),
                customModel.getRequestedOn(),
                customModel.getCompletedOn())
        );
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels
    }

    /**
     * Code snippet for {@link FormTrainingClient#listCustomModels(Context)}
     */
    public void listCustomModelsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels#Context
        formTrainingClient.listCustomModels(Context.NONE).forEach(customModel ->
            System.out.printf("Model Id: %s, Model status: %s, Created on: %s, Last updated on: %s.%n",
                customModel.getModelId(),
                customModel.getStatus(),
                customModel.getRequestedOn(),
                customModel.getCompletedOn())
        );
        // END: com.azure.ai.formrecognizer.training.FormTrainingClient.listCustomModels#Context
    }
}
