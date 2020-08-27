// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.TrainingFileFilter;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.polling.AsyncPollResponse;

import java.time.Duration;

/**
 * Code snippet for {@link FormTrainingAsyncClient}
 */
public class FormTrainingAsyncClientJavaDocCodeSnippets {
    private FormTrainingAsyncClient formTrainingAsyncClient = new FormTrainingClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link FormTrainingAsyncClient} initialization
     */
    public void formTrainingAsyncClientInInitialization() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.initialization
        FormTrainingAsyncClient formTrainingAsyncClient = new FormTrainingClientBuilder().buildAsyncClient();
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.initialization
    }

    /**
     * Code snippet for creating a {@link FormTrainingAsyncClient} with pipeline
     */
    public void createFormTrainingAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        FormTrainingAsyncClient formTrainingAsyncClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildAsyncClient();
        // END:  com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginTraining(String, boolean)}
     */
    public void beginTraining() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        boolean useTrainingLabels = true;
        formTrainingAsyncClient.beginTraining(trainingFilesUrl, useTrainingLabels)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(customFormModel -> {
                System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                customFormModel.getSubmodels()
                    .forEach(customFormSubmodel -> customFormSubmodel.getFields()
                        .forEach((key, customFormModelField) ->
                            System.out.printf("Form type: %s Field Text: %s Field Accuracy: %f%n",
                                key, customFormModelField.getName(), customFormModelField.getAccuracy())));
            });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginTraining(String, boolean, TrainingOptions)}
     * with options
     */
    public void beginTrainingWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean-TrainingOptions
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        TrainingFileFilter trainingFileFilter = new TrainingFileFilter().setSubfoldersIncluded(true).setPrefix("Invoice");

        formTrainingAsyncClient.beginTraining(trainingFilesUrl, true,
            new TrainingOptions()
                .setTrainingFileFilter(trainingFileFilter)
                .setPollInterval(Duration.ofSeconds(5)))
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(customFormModel -> {
                System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                customFormModel.getSubmodels().forEach(customFormSubmodel ->
                    customFormSubmodel.getFields().forEach((key, customFormModelField) ->
                        System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %f%n",
                            key, customFormModelField.getName(), customFormModelField.getAccuracy())));
            });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean-TrainingOptions
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCustomModel(String)}
     */
    public void getCustomModel() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModel#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.getCustomModel(modelId).subscribe(customFormModel -> {
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            customFormModel.getSubmodels()
                .forEach(customFormSubmodel -> customFormSubmodel.getFields()
                    .forEach((key, customFormModelField) ->
                        System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %f%n",
                            key, customFormModelField.getName(), customFormModelField.getAccuracy())));

        });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModel#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCustomModelWithResponse(String)}
     */
    public void getCustomModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModelWithResponse#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.getCustomModelWithResponse(modelId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            CustomFormModel customFormModel = response.getValue();
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            customFormModel.getSubmodels()
                .forEach(customFormSubmodel -> customFormSubmodel.getFields()
                    .forEach((key, customFormModelField) ->
                        System.out.printf("Form Type: %s Field Text: %s Field Accuracy: %f%n",
                            key, customFormModelField.getName(), customFormModelField.getAccuracy())));
        });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModelWithResponse#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getAccountProperties()}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountProperties
        formTrainingAsyncClient.getAccountProperties()
            .subscribe(accountProperties -> {
                System.out.printf("Max number of models that can be trained for this account: %d%n",
                    accountProperties.getCustomModelLimit());
                System.out.printf("Current count of trained custom models: %d%n",
                    accountProperties.getCustomModelCount());
            });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountProperties
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getAccountPropertiesWithResponse()}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountPropertiesWithResponse
        formTrainingAsyncClient.getAccountPropertiesWithResponse()
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                AccountProperties accountProperties = response.getValue();
                System.out.printf("Max number of models that can be trained for this account: %d%n",
                    accountProperties.getCustomModelLimit());
                System.out.printf("Current count of trained custom models: %d%n",
                    accountProperties.getCustomModelCount());
            });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountPropertiesWithResponse
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#deleteModel}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.deleteModel(modelId)
            .subscribe(ignored -> System.out.printf("Model Id: %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#deleteModelWithResponse(String)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        formTrainingAsyncClient.deleteModelWithResponse(modelId)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model Id: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#listCustomModels()}
     */
    public void listCustomModels() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.listCustomModels
        formTrainingAsyncClient.listCustomModels()
            .subscribe(customModel ->
                System.out.printf("Model Id: %s, Model status: %s, Created on: %s, Last updated on: %s.%n",
                    customModel.getModelId(),
                    customModel.getStatus(),
                    customModel.getTrainingStartedOn(),
                    customModel.getTrainingCompletedOn()));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.listCustomModels
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginCopyModel(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization
        String resourceId = "target-resource-Id";
        String resourceRegion = "target-resource-region";
        String copyModelId = "copy-model-Id";
        formTrainingAsyncClient.getCopyAuthorization(resourceId, resourceRegion)
            .flatMapMany(copyAuthorization -> formTrainingAsyncClient.beginCopyModel(copyModelId, copyAuthorization))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(customFormModelInfo ->
                System.out.printf("Copied model has model Id: %s, model status: %s, training started on: %s,"
                    + " training completed on: %s.%n",
                customFormModelInfo.getModelId(),
                customFormModelInfo.getStatus(),
                customFormModelInfo.getTrainingStartedOn(),
                customFormModelInfo.getTrainingCompletedOn()));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#beginCopyModel(String, CopyAuthorization, Duration)}
     */
    public void beginCopyOverload() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization-Duration
        String resourceId = "target-resource-Id";
        String resourceRegion = "target-resource-region";
        String copyModelId = "copy-model-Id";
        formTrainingAsyncClient.getCopyAuthorization(resourceId, resourceRegion)
            .flatMapMany(copyAuthorization -> formTrainingAsyncClient.beginCopyModel(copyModelId, copyAuthorization,
                Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
                .subscribe(customFormModelInfo ->
                    System.out.printf("Copied model has model Id: %s, model status: %s, training started on: %s,"
                        + "training completed on: %s.%n",
                    customFormModelInfo.getModelId(),
                    customFormModelInfo.getStatus(),
                    customFormModelInfo.getTrainingStartedOn(),
                    customFormModelInfo.getTrainingCompletedOn()));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization-Duration
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorization#string-string
        String resourceId = "target-resource-Id";
        String resourceRegion = "target-resource-region";
        formTrainingAsyncClient.getCopyAuthorization(resourceId, resourceRegion)
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                        + "target resource Id; %s, target resource region: %s%n",
                    copyAuthorization.getModelId(),
                    copyAuthorization.getAccessToken(),
                    copyAuthorization.getExpiresOn(),
                    copyAuthorization.getResourceId(),
                    copyAuthorization.getResourceRegion()
                ));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorization#string-string
    }

    /**
     * Code snippet for {@link FormTrainingAsyncClient#getCopyAuthorizationWithResponse(String, String)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorizationWithResponse#string-string
        String resourceId = "target-resource-Id";
        String resourceRegion = "target-resource-region";
        formTrainingAsyncClient.getCopyAuthorizationWithResponse(resourceId, resourceRegion)
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization response status: %s, for model id: %s, access token: %s, "
                        + "expiration time: %s, target resource Id; %s, target resource region: %s%n",
                copyAuthorization.getStatusCode(),
                copyAuthorization.getValue().getModelId(),
                copyAuthorization.getValue().getAccessToken(),
                copyAuthorization.getValue().getExpiresOn(),
                copyAuthorization.getValue().getResourceId(),
                copyAuthorization.getValue().getResourceRegion()
            ));
        // END: com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorizationWithResponse#string-string
    }
}
