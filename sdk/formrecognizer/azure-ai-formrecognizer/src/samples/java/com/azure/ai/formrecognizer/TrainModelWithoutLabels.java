// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to train a model with unlabeled data. See RecognizeCustomFormsAsync to recognize forms with your custom model.
 */
public class TrainModelWithoutLabels {

    /**
     * Main method to invoke this demo about how to train a custom model.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.

        FormTrainingClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient().getFormTrainingClient();

        // Train custom model
        String trainingSetSource = "{unlabeled_training_set_SAS_URL}";
        SyncPoller<OperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingSetSource, false);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model created on: %s%n", customFormModel.getCreatedOn());
        System.out.printf("Model last updated: %s%n%n", customFormModel.getLastUpdatedOn());

        System.out.println("Recognized Fields:");
        // looping through the sub-models, which contains the fields they were trained on
        // Since the given training documents are unlabeled, we still group them but they do not have a label.
        customFormModel.getSubModels().forEach(customFormSubModel -> {
            // Since the training data is unlabeled, we are unable to return the accuracy of this model
            customFormSubModel.getFieldMap().forEach((field, customFormModelField) ->
                System.out.printf("Field: %s Field Label: %s%n",
                    field, customFormModelField.getLabel()));
        });
        System.out.println();

        // Training result information
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
