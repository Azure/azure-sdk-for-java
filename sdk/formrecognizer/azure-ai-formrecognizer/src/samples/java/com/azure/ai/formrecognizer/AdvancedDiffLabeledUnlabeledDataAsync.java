// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * Async sample to show the differences in output that arise when RecognizeCustomForms
 * is called with custom models trained with labeled and unlabeled data.
 */
public class AdvancedDiffLabeledUnlabeledDataAsync {

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

        File analyzeFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/"
            + "forms/Invoice_6.pdf");
        byte[] fileContent = Files.readAllBytes(analyzeFile.toPath());

        PollerFlux<OperationResult, IterableStream<RecognizedForm>> labeledCustomFormPoller =
            client.beginRecognizeCustomForms(toFluxByteBuffer(new ByteArrayInputStream(fileContent)), "{labeled_model_Id}", analyzeFile.length(), FormContentType.APPLICATION_PDF, true, null);
        PollerFlux<OperationResult, IterableStream<RecognizedForm>> unlabeledCustomFormPoller =
            client.beginRecognizeCustomForms(toFluxByteBuffer(new ByteArrayInputStream(fileContent)), "{unlabeled_model_Id}", analyzeFile.length(), FormContentType.APPLICATION_PDF);

        Mono<IterableStream<RecognizedForm>> labeledDataResult = labeledCustomFormPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        Mono<IterableStream<RecognizedForm>> unlabeledDataResult = unlabeledCustomFormPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        //  The main difference is found in the labels of its fields
        // The form recognized with a labeled model will have the labels it was trained with,
        // the unlabeled one will be denoted with indices
        System.out.println("--------Recognizing forms with labeled custom model--------");
        labeledDataResult.subscribe(formsWithLabeledModel -> formsWithLabeledModel.forEach(labeledForm ->
            labeledForm.getFields().forEach((label, formField) -> {
                final StringBuilder boundingBoxStr = new StringBuilder();
                if (formField.getValueText().getBoundingBox() != null) {
                    formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                        boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                }
                System.out.printf("Field %s has value %s based on %s within bounding box %s with a confidence score "
                        + "of %.2f.%n",
                    label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr,
                    formField.getConfidence());
            })));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("-----------------------------------------------------------");

        System.out.println("-------Recognizing forms with unlabeled custom model-------");
        unlabeledDataResult.subscribe(recognizedForms -> recognizedForms.forEach(unLabeledForm ->
            unLabeledForm.getFields().forEach((label, formField) -> {
                final StringBuilder boundingBoxStr = new StringBuilder();
                if (formField.getValueText().getBoundingBox() != null) {
                    formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                        boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                }

                final StringBuilder boundingBoxLabelStr = new StringBuilder();
                if (formField.getLabelText() != null && formField.getLabelText().getBoundingBox() != null) {
                    formField.getLabelText().getBoundingBox().getPoints().forEach(point ->
                        boundingBoxLabelStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                }
                System.out.printf("Field %s has label %s  within bounding box %s with a confidence score "
                        + "of %.2f.%n",
                    label, formField.getLabelText().getText(), boundingBoxLabelStr, formField.getConfidence());

                System.out.printf("Field %s has value %s based on %s within bounding box %s with a confidence score "
                        + "of %.2f.%n",
                    label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr,
                    formField.getConfidence());
            })));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printFieldData(IterableStream<RecognizedForm> recognizedForms) {
        recognizedForms.forEach(labeledForm -> labeledForm.getFields().forEach((label, formField) -> {
            // With your labeled custom model, you will not get back label data but will get back value data
            // This is because your custom model didn't have to use any machine learning to deduce the label,
            // the label was directly provided to it.
            final StringBuilder boundingBoxStr = new StringBuilder();
            if (formField.getValueText().getBoundingBox() != null) {
                formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                    boundingBoxStr.append(String.format("[%2f, %.2f]", point.getX(), point.getY())));
            }
            // The unlabeled custom model will also include data about your labels
            System.out.printf("Field %s has value %s based on %s within bounding box %s with a confidence score "
                    + "of %.2f.%n",
                label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr,
                formField.getConfidence());
        }));
    }
}
