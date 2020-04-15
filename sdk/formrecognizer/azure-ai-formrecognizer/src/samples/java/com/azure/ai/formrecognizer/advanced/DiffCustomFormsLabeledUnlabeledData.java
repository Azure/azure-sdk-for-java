// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.advanced;

import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This sample demonstrates the differences in output that arise when recognize_custom_forms
 * is called with custom models trained with labeled and unlabeled data.
 */
public class DiffCustomFormsLabeledUnlabeledData {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();
        File analyzeFile = new File("/Test/Invoice_6.pdf");
        byte[] fileContent = Files.readAllBytes(analyzeFile.toPath());

        IterableStream<RecognizedForm> formsWithLabeledModel = client.beginRecognizeCustomForms(new ByteArrayInputStream(fileContent)
            , "{labeled_model_id}", analyzeFile.length(), FormContentType.APPLICATION_PDF, true, null).getFinalResult();
        IterableStream<RecognizedForm> formsWithUnlabeledModel = client.beginRecognizeCustomForms(new ByteArrayInputStream(fileContent)
            ,  "{unlabeled_model_id}", analyzeFile.length(), FormContentType.APPLICATION_PDF).getFinalResult();

        //  The main difference is found in the labels of its fields
        // The form recognized with a labeled model will have the labels it was trained with,
        // the unlabeled one will be denoted with indices
        System.out.println("--------Recognizing forms with labeled custom model--------");
        formsWithLabeledModel.forEach(labeledForm -> labeledForm.getFields().forEach((label, formField) -> {
            // With your labeled custom model, you will not get back label data but will get back value data
            // This is because your custom model didn't have to use any machine learning to deduce the label,
            // the label was directly provided to it
           // AtomicReference<String> boundingBoxStr = new AtomicReference<>("");
            final StringBuilder  boundingBoxStr = new StringBuilder();
            if (formField.getValueText().getBoundingBox() != null) {
                formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                    boundingBoxStr.append(String.format("[%s, %s]", point.getX(), point.getY())));
            }
            System.out.printf("Field %s has value %s based on %s within bounding box %s with a confidence score "
                    + "of %s.%n",
                label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr, formField.getConfidence());
        }));

        System.out.println("-----------------------------------------------------------");

        System.out.println("-------Recognizing forms with unlabeled custom model-------");
        formsWithUnlabeledModel.forEach(recognizedForm -> {
            recognizedForm.getFields().forEach((label, formField) -> {
                final StringBuilder  boundingBoxStr = new StringBuilder();
                if (formField.getValueText().getBoundingBox() != null) {
                    formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                        boundingBoxStr.append(String.format("[%s, %s]", point.getX(), point.getY())));
                }

                // The unlabeled custom model will also include data about your labels
                System.out.printf("Field %s has value %s based on %s within bounding box %s, with a confidence score "
                        + "of %s.%n",
                    label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr, formField.getConfidence());
            });
        });
    }
}
