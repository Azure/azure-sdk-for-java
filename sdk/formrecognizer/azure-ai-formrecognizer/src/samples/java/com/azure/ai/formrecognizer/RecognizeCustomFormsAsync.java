// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * Async sample to analyze a form from a document with a custom trained model. To learn how to train your own models,
 * look at TrainModelWithoutLabels.java and TrainModelWithLabels.java.
 */
public class RecognizeCustomFormsAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // The form you are recognizing must be of the same type as the forms the custom model was trained on
        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/forms/Invoice_6.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        String modelId = "{modelId}";
        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> recognizeFormPoller;
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            recognizeFormPoller = client.beginRecognizeCustomForms(modelId, toFluxByteBuffer(targetStream),
                sourceFile.length());
        }

        Mono<List<RecognizedForm>> recognizeFormResult = recognizeFormPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        recognizeFormResult.subscribe(recognizedForms -> {
            for (int i = 0; i < recognizedForms.size(); i++) {
                final RecognizedForm form = recognizedForms.get(i);
                System.out.printf("----------- Recognized custom form info for page %d -----------%n", i);
                System.out.printf("Form type: %s%n", form.getFormType());
                System.out.printf("Form has form type confidence : %.2f%n", form.getFormTypeConfidence());
                System.out.printf("Form was analyzed with model with ID: %s%n", form.getModelId());
                form.getFields().forEach((label, formField) -> {
                    System.out.printf("Field '%s' has label '%s' with confidence score of %.2f.%n", label,
                        formField.getLabelData().getText(),
                        formField.getConfidence());
                });
            }
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
