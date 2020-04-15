// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

/*
 *  This sample demonstrates how to analyze a form from a document with a custom
    trained model
 */
public class RecognizeCustomFormsAsync {

    /**
     * Main method to invoke this demo to analyze a form from a document with a custom
     * trained model.
     *
     * @param args Unused arguments to the program.
     *
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String analyzeFilePath = "{file_source_url}";
        String modelId = "{custom_trained_model_id}";
        PollerFlux<OperationResult, IterableStream<RecognizedForm>> recognizeFormPoller =
            client.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId);

        IterableStream<RecognizedForm> recognizedForms = recognizeFormPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            }).block();

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
