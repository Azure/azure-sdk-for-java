// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
public class CreateComposedModelAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        FormTrainingAsyncClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Train custom model
        String model1TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_1}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        PollerFlux<FormRecognizerOperationResult, CustomFormModel> model1Poller = client.beginTraining(model1TrainingFiles, true);

        // Train custom model
        String model2TrainingFiles = "{SAS_URL_of_your_container_in_blob_storage_for_model_2}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        PollerFlux<FormRecognizerOperationResult, CustomFormModel> model2Poller = client.beginTraining(model2TrainingFiles, true);

        String labeledModelId1 = model1Poller.getSyncPoller().getFinalResult().getModelId();
        String labeledModelId2 = model2Poller.getSyncPoller().getFinalResult().getModelId();

        client.beginCreateComposedModel(Arrays.asList(labeledModelId1, labeledModelId2),
            new CreateComposedModelOptions().setModelName("my composed model name"))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(customFormModel -> {

                System.out.printf("Model Id: %s%n", customFormModel.getModelId());
                System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
                System.out.printf("Model name: %s%n", customFormModel.getModelName());
                System.out.printf("Is this a composed model: %s%n",
                    customFormModel.getCustomModelProperties().isComposed());
                System.out.printf("Composed model creation started on:  %s%n", customFormModel.getTrainingStartedOn());
                System.out.printf("Composed model creation completed on:  %s%n", customFormModel.getTrainingCompletedOn());

                System.out.println("Recognized Fields:");
                customFormModel.getSubmodels().forEach(customFormSubmodel -> {
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
                    System.out.printf("Document status: %s%n", trainingDocumentInfo.getStatus());
                    System.out.printf("Document page count: %d%n", trainingDocumentInfo.getPageCount());
                    if (!trainingDocumentInfo.getErrors().isEmpty()) {
                        System.out.println("Document Errors:");
                        trainingDocumentInfo.getErrors().forEach(formRecognizerError ->
                            System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getErrorCode(),
                                formRecognizerError.getMessage()));
                    }
                });
            });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

