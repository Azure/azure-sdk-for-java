// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to train a model with labeled data. See RecognizeCustomFormsAsync to recognize forms with your custom model.
 */
public class TrainModelWithLabels {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient().getFormTrainingClient();

        // Train custom model
        String trainingSetSource = "{labeled_training_set_SAS_URL}";
        SyncPoller<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, true);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model created on: %s%n", customFormModel.getCreatedOn());
        System.out.printf("Model last updated: %s%n%n", customFormModel.getLastUpdatedOn());

        // looping through the sub-models, which contains the fields they were trained on
        // The labels are based on the ones you gave the training document.
        System.out.println("Recognized Fields:");
        // Since the data is labeled, we are able to return the accuracy of the model
        customFormModel.getSubModels().forEach(customFormSubModel -> {
            System.out.printf("Sub-model accuracy: %.2f%n", customFormSubModel.getAccuracy());
            customFormSubModel.getFieldMap().forEach((label, customFormModelField) ->
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
    }
}
