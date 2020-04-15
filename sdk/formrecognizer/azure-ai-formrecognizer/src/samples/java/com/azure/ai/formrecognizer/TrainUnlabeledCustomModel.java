// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

/**
 * This sample demonstrates how to train a model with labeled data. See RecognizeCustomFormsAsync
 * to recognize forms with your custom model.
 */
public class TrainUnlabeledCustomModel {

    /**
     * Main method to invoke this demo about how to train a custom model.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient().getFormTrainingClient();

        // Train custom model
        String trainingSetSource = "{training-set-SAS-URL}";
        SyncPoller<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, false);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        // looping through the submodels, which contains the fields they were trained on
        // Since the given training documents are unlabeled, we still group them but they do not have a label.
        System.out.println("Recognized Fields:");
        customFormModel.getSubModels().forEach(customFormSubModel -> {
            // Since the training data is unlabeled, we are unable to return the accuracy of this model
            customFormSubModel.getFieldMap().forEach((field, customFormModelField) ->
                System.out.printf("Field: %s Field Label: %s%n",
                    field, customFormModelField.getLabel()));
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
