// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

/**
 * This sample demonstrates how to train a model with labeled data. See RecognizeCustomFormsAsync
 * to recognize forms with your custom model.
 */
public class TrainLabeledCustomModelAsync {

    /**
     * Main method to invoke this demo about how to train a custom model.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient().getFormTrainingAsyncClient();

        // Train custom model
        String trainingSetSource = "{training-set-SAS-URL}";
        PollerFlux<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, false);

        CustomFormModel customFormModel = trainingPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().equals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            }).block();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        // looping through the sub-models, which contains the fields they were trained on
        // The labels are based on the ones you gave the training document.
        System.out.println("Recognized Fields:");
        customFormModel.getSubModels().forEach(customFormSubModel -> {
            // Since the data is labeled, we are able to return the accuracy of the model
            System.out.printf("Sub-model accuracy: %s%n", customFormSubModel.getAccuracy());
            customFormSubModel.getFieldMap().forEach((label, customFormModelField) ->
                System.out.printf("Field: %s Field Name: %s Field Accuracy: %s%n",
                    label, customFormModelField.getName(), customFormModelField.getAccuracy()));
        });

        customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
            System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
            System.out.printf("Document status: %s%n", trainingDocumentInfo.getName());
            System.out.printf("Document page count: %s%n", trainingDocumentInfo.getName());
            System.out.println("Document errors:");
            trainingDocumentInfo.getDocumentError().forEach(formRecognizerError -> {
                System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getCode(),
                    formRecognizerError.getMessage());
            });
        });

    }
}
