// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

/*
 *  This sample demonstrates how to analyze a form from a document with a custom
    trained model
 */
public class RecognizeCustomForms {

    /**
     * Main method to invoke this demo to analyze a form from a document with a custom
     * trained model.
     *
     * @param args Unused arguments to the program.
     *
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String analyzeFilePath = "{file_source_url}";
        String modelId = "{custom_trained_model_id}";
        SyncPoller<OperationResult, IterableStream<RecognizedForm>> recognizeFormPoller =
            client.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId);

        IterableStream<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        recognizedForms.forEach(form -> {
            System.out.println("----------- Recognized Form -----------");
            System.out.printf("Form has type {}", form.getFormType());
            form.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Field % has value %s with confidence score of %s", fieldText, fieldValue,
                    fieldValue.getFieldValue());
            });
            System.out.print("-----------------------------------");
        });
    }
}
