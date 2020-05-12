// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Sample for recognizing content information from a document given through a file.
 */
public class RecognizeContent {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/"
            + "forms/layout1.jpg");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<FormPage>> recognizeLayoutPoller =
            client.beginRecognizeContent(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG);

        IterableStream<FormPage> layoutPageResults = recognizeLayoutPoller.getFinalResult();

        layoutPageResults.forEach(formPage -> {
            // Table information
            System.out.println("----Recognizing content ----");
            System.out.printf("Has width: %s and height: %s, measured with unit: %s%n", formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());
            formPage.getTables().forEach(formTable -> {
                System.out.printf("Table has %s rows and %s columns.%n", formTable.getRowCount(),
                    formTable.getColumnCount());
                formTable.getCells().forEach(formTableCell -> {
                    final StringBuilder boundingBoxStr = new StringBuilder();
                    if (formTableCell.getBoundingBox() != null) {
                        formTableCell.getBoundingBox().getPoints().forEach(point ->
                            boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                    }
                    System.out.printf("Cell has text %s, within bounding box %s.%n", formTableCell.getText(),
                        boundingBoxStr);
                });
                System.out.println();
            });
        });
    }
}
