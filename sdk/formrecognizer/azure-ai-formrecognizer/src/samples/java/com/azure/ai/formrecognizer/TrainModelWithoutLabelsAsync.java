// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Async sample to train a model with unlabeled data.
 * For instructions on setting up forms for training in an Azure Storage Blob Container, see
 * https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data
 * For this sample, you can use the training forms found in
 * https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/training to
 * create your own custom models.
 * Further, see RecognizeCustomForms.java to recognize forms with your custom built model.
 */
public class TrainModelWithoutLabelsAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormTrainingAsyncClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Train custom model
        String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        PollerFlux<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl, false);

        Mono<CustomFormModel> customFormModelResult = trainingPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        customFormModelResult.subscribe(customFormModel -> {
            // Model Info
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            System.out.printf("Training started on: %s%n", customFormModel.getTrainingStartedOn());
            System.out.printf("Training completed on: %s%n%n", customFormModel.getTrainingCompletedOn());

            System.out.println("Recognized Fields:");
            // looping through the subModels, which contains the fields they were trained on
            // Since the given training documents are unlabeled, we still group them but they do not have a label.
            customFormModel.getSubmodels().forEach(customFormSubmodel -> {
                // Since the training data is unlabeled, we are unable to return the accuracy of this model
                System.out.printf("The subModel has form type %s%n", customFormSubmodel.getFormType());
                customFormSubmodel.getFields().forEach((field, customFormModelField) ->
                    System.out.printf("The model found field '%s' with label: %s%n",
                        field, customFormModelField.getLabel()));
            });
            System.out.println();

            // Training result information
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
