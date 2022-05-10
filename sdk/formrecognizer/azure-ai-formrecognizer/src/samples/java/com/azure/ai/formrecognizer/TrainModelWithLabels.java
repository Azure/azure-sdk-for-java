// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to train a model with labeled data.
 * For instructions on setting up forms for training in an Azure Storage Blob Container, see
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>.
 *
 * For this sample, you can use the training forms found in
 * <a href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/resources/sample-forms/training">here</a>
 * to create your own custom models.
 * For instructions to create a label file for your training forms, please see:
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/label-tool?tabs=v2-1">here</a>.
 *
 * Further, see RecognizeCustomForms.java to recognize forms with your custom built model.
 */
public class TrainModelWithLabels {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormTrainingClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        // Train custom model
        String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
            client.beginTraining(trainingFilesUrl,
                true,
                new TrainingOptions()
                    .setModelName("model trained with labels"),
                Context.NONE);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model name: %s%n", customFormModel.getModelName());
        System.out.printf("Training started on: %s%n", customFormModel.getTrainingStartedOn());
        System.out.printf("Training completed on: %s%n%n", customFormModel.getTrainingCompletedOn());

        // looping through the subModels, which contains the fields they were trained on
        // The labels are based on the ones you gave the training document.
        System.out.println("Recognized Fields:");
        // Since the data is labeled, we are able to return the accuracy of the model
        customFormModel.getSubmodels().forEach(customFormSubmodel -> {
            System.out.printf("Submodel Id: %s%n", customFormSubmodel.getModelId());
            System.out.printf("The submodel with form type %s has accuracy: %.2f%n",
                customFormSubmodel.getFormType(), customFormSubmodel.getAccuracy());
            customFormSubmodel.getFields().forEach((label, customFormModelField) ->
                System.out.printf("The model found field '%s' to have name: %s with an accuracy: %.2f%n",
                    label, customFormModelField.getName(), customFormModelField.getAccuracy()));
        });
        System.out.println();
        customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
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
