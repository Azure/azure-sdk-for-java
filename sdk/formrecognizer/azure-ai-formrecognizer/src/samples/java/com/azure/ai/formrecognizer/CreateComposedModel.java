// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.Arrays;

/**
 * Sample for creating a custom composed model.
 * <p>
 *     This is useful when you have trained different models and want to aggregate a group of
 *     them into a single model that you (or a user) could use to recognize a form. When doing
 *     so, you can let the service decide which model more accurately represents the form to
 *     recognize, instead of manually trying each trained model against the form and selecting
 *     the most accurate one.
 * </p>
 */
public class CreateComposedModel {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        FormTrainingClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Train custom model
        String model1TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_1}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<FormRecognizerOperationResult, CustomFormModel> model1Poller = client.beginTraining(model1TrainingFiles, true);

        // Train custom model
        String model2TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_2}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<FormRecognizerOperationResult, CustomFormModel> model2Poller = client.beginTraining(model2TrainingFiles, true);

        String labeledModelId1 = model1Poller.getFinalResult().getModelId();
        String labeledModelId2 = model2Poller.getFinalResult().getModelId();

        final CustomFormModel customFormModel =
            client.beginCreateComposedModel(
                Arrays.asList(labeledModelId1, labeledModelId2),
                new CreateComposedModelOptions().setModelName("my composed model name"),
                Context.NONE)
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model name: %s%n", customFormModel.getModelName());
        System.out.printf("Is this a composed model: %s%n",
            customFormModel.getCustomModelProperties().isComposed());
        System.out.printf("Composed model creation started on: %s%n", customFormModel.getTrainingStartedOn());
        System.out.printf("Composed model creation completed on:  %s%n", customFormModel.getTrainingCompletedOn());

        System.out.println("Recognized Fields:");
        customFormModel.getSubmodels().forEach(customFormSubmodel -> {
            System.out.printf("Submodel Id: %s%n", customFormSubmodel.getModelId());
            System.out.printf("The subModel with form type %s has accuracy: %.2f%n",
                customFormSubmodel.getFormType(), customFormSubmodel.getAccuracy());
            customFormSubmodel.getFields().forEach((label, customFormModelField) ->
                System.out.printf("The model found field '%s' to have name: %s with an accuracy: %.2f%n",
                    label, customFormModelField.getName(), customFormModelField.getAccuracy()));
        });
        System.out.println();

        customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
            System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
            System.out.printf("Document was provided to train model with Id : %s%n", trainingDocumentInfo.getModelId());
            System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
            System.out.printf("Document status: %s%n", trainingDocumentInfo.getStatus());
            System.out.printf("Document page count: %d%n", trainingDocumentInfo.getPageCount());
            if (!trainingDocumentInfo.getErrors().isEmpty()) {
                System.out.println("Document Errors:");
                trainingDocumentInfo.getErrors().forEach(formRecognizerError ->
                    System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getErrorCode(),
                        formRecognizerError.getMessage()));
            }
        });
    }
}

