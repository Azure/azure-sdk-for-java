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
 * Sample for training a custom model using training data set source URL.
 */
public class TrainCustomModel {

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
        String trainingSetSource = "https://krpraticstorageacc.blob.core.windows.net/form-recognizer-merged?sp=racwdl&st=2020-04-07T19:04:26Z&se=2020-06-08T19:04:00Z&sv=2019-02-02&sr=c&sig=55DKi6ZtztMjWRd6IKxXWVwcJHY2BB3utLjLxush%2FpQ%3D";
        PollerFlux<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, true);

        CustomFormModel customFormModel = trainingPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().equals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    System.out.println("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus());
                    return Mono.empty();
                }
            }).block();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        customFormModel.getSubModels().forEach(customFormSubModel ->
            customFormSubModel.getFieldMap().forEach((key, customFormModelField) ->
                System.out.printf("Model Type Id: %s Field Text: %s Field Accuracy: %s%n",
                    key, customFormModelField.getFieldText(), customFormModelField.getAccuracy())));

    }
}
