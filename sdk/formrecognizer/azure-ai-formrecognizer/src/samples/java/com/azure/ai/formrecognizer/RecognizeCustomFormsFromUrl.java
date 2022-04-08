// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * Sample to analyze a form from a document with a custom trained model. To learn how to train your own models,
 * look at TrainModelWithoutLabels.java and TrainModelWithLabels.java.
 */
public class RecognizeCustomFormsFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String formUrl = "{form_url}";
        String modelId = "{custom_trained_model_id}";
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> recognizeFormPoller =
            client.beginRecognizeCustomFormsFromUrl(modelId, formUrl);

        List<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        for (int i = 0; i < recognizedForms.size(); i++) {
            final RecognizedForm form = recognizedForms.get(i);
            System.out.printf("----------- Recognized custom form info for page %d -----------%n", i);
            System.out.printf("Form type: %s%n", form.getFormType());
            System.out.printf("Form has form type confidence : %.2f%n", form.getFormTypeConfidence());
            System.out.printf("Form was analyzed with model with ID: %s%n", form.getModelId());
            form.getFields().forEach((label, formField) ->
                // label data is populated if you are using a model trained with unlabeled data,
                // since the service needs to make predictions for labels if not explicitly given to it.
                System.out.printf("Field '%s' has label '%s' with a confidence score of %.2f.%n",
                    label, formField.getLabelData().getText(), formField.getConfidence()));
        }
    }
}
