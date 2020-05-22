// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample to analyze a form from a document with a custom trained model. To learn how to train your own models,
 * look at TrainModelWithoutLabels.java and TrainModelWithLabels.java.
 */
public class RecognizeCustomForms {

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

        String analyzeFilePath = "{file_source_url}";
        String modelId = "{custom_trained_model_id}";
        SyncPoller<OperationResult, IterableStream<RecognizedForm>> recognizeFormPoller =
            client.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId);

        IterableStream<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        recognizedForms.forEach(form -> {
            System.out.println("----------- Recognized Form -----------");
            System.out.printf("Form type: %s%n", form.getFormType());
            form.getFields().forEach((label, formField) -> {
                System.out.printf("Field %s has value %s with confidence score of %.2f.%n", label,
                    formField.getFieldValue(),
                    formField.getConfidence());
            });
            System.out.print("-----------------------------------");
        });
    }
}
