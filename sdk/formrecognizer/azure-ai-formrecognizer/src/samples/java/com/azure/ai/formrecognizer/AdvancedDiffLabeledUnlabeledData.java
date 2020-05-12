// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Sample to show the differences in output that arise when RecognizeCustomForms
 * is called with custom models trained with labeled and unlabeled data.
 */
public class AdvancedDiffLabeledUnlabeledData {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File analyzeFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/"
            + "forms/Invoice_6.pdf");

        IterableStream<RecognizedForm> formsWithLabeledModel =
            client.beginRecognizeCustomForms(new FileInputStream(analyzeFile), "{labeled_model_Id}",
                analyzeFile.length(), FormContentType.APPLICATION_PDF, true, null).getFinalResult();
        IterableStream<RecognizedForm> formsWithUnlabeledModel =
            client.beginRecognizeCustomForms(new FileInputStream(analyzeFile), "{unlabeled_model_Id}",
                analyzeFile.length(), FormContentType.APPLICATION_PDF).getFinalResult();

        //  The main difference is found in the labels of its fields
        // The form recognized with a labeled model will have the labels it was trained with,
        // the unlabeled one will be denoted with indices
        System.out.println("--------Recognizing forms with labeled custom model--------");

        // With a form recognized by a model trained with labels, this 'name' key will be its
        // training-time label, otherwise it will be denoted by numeric indices.
        // Label data is not returned for model trained with labels.
        formsWithLabeledModel.forEach(labeledForm -> labeledForm.getFields().forEach((label, formField) -> {
            final StringBuilder boundingBoxStr = new StringBuilder();
            if (formField.getValueText().getBoundingBox() != null) {
                formField.getValueText().getBoundingBox().getPoints().forEach(point ->
                    boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
            }
            System.out.printf("Field %s has value %s based on %s within bounding box %s with a confidence score "
                    + "of %.2f.%n",
                label, formField.getFieldValue(), formField.getValueText().getText(), boundingBoxStr,
                formField.getConfidence());
        }));

        System.out.println("-----------------------------------------------------------");

        System.out.println("-------Recognizing forms with unlabeled custom model-------");
        formsWithUnlabeledModel.forEach(unLabeledForm -> unLabeledForm.getFields().forEach((label, formField) -> {
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
        }));
    }
}
