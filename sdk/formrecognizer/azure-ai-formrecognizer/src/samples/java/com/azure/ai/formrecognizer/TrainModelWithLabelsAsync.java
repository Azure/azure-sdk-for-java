// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Async sample to train a model with labeled data. See RecognizeCustomFormsAsync to recognize forms with your
 * custom model.
 */
public class TrainModelWithLabelsAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingAsyncClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // Train custom model
        String trainingSetSource = "{labeled_training_set_SAS_URL}";
        PollerFlux<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, true);

        Mono<CustomFormModel> customFormModelResult = trainingPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().equals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        customFormModelResult.subscribe(customFormModel -> {
            // Model Info
            System.out.printf("Model Id: %s%n", customFormModel.getModelId());
            System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
            System.out.printf("Model requested on: %s%n", customFormModel.getRequestedOn());
            System.out.printf("Model training completed on: %s%n%n", customFormModel.getCompletedOn());

            // looping through the sub-models, which contains the fields they were trained on
            // The labels are based on the ones you gave the training document.
            System.out.println("Recognized Fields:");
            // Since the data is labeled, we are able to return the accuracy of the model
            customFormModel.getSubmodels().forEach(customFormSubmodel -> {
                System.out.printf("Sub-model accuracy: %.2f%n", customFormSubmodel.getAccuracy());
                customFormSubmodel.getFieldMap().forEach((label, customFormModelField) ->
                    System.out.printf("Field: %s Field Name: %s Field Accuracy: %.2f%n",
                        label, customFormModelField.getName(), customFormModelField.getAccuracy()));
            });
            System.out.println();
            customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
                System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
                System.out.printf("Document status: %s%n", trainingDocumentInfo.getName());
                System.out.printf("Document page count: %s%n", trainingDocumentInfo.getPageCount());
                if (!trainingDocumentInfo.getDocumentErrors().isEmpty()) {
                    System.out.println("Document Errors:");
                    trainingDocumentInfo.getDocumentErrors().forEach(formRecognizerError ->
                        System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getCode(),
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
