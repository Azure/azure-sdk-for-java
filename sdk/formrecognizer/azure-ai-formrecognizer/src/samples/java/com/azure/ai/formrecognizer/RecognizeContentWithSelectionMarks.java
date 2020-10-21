// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class RecognizeContentWithSelectionMarks {
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
                                          .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_API_KEY")))
                                          .endpoint(Configuration.getGlobalConfiguration().get("AZURE_FORM_RECOGNIZER_ENDPOINT"))
                                          .buildClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/"
                                       + "forms/selectionMarkForm.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
            client.beginRecognizeContent(targetStream, sourceFile.length());

        List<FormPage> contentPageResults = recognizeContentPoller.getFinalResult();

        for (int i = 0; i < contentPageResults.size(); i++) {
            final FormPage formPage = contentPageResults.get(i);
            System.out.printf("---- Recognized content info for page %d ----%n", i);
            // Table information
            System.out.printf("Has width: %.2f and height: %.2f, measured with unit: %s%n", formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());
            final List<FormTable> tables = formPage.getTables();
            for (int i1 = 0; i1 < tables.size(); i1++) {
                final FormTable formTable = tables.get(i1);
                System.out.printf("Table %d has %d rows and %d columns.%n", i1, formTable.getRowCount(),
                    formTable.getColumnCount());
                formTable.getCells().forEach(formTableCell -> {
                    final StringBuilder boundingBoxStr = new StringBuilder();
                    if (formTableCell.getBoundingBox() != null) {
                        formTableCell.getBoundingBox().getPoints().forEach(
                            point -> boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                    }
                    System.out.printf("Cell has text '%s', within bounding box %s.%n", formTableCell.getText(),
                        boundingBoxStr);
                });
                System.out.println();
            }
            // Selection Mark
            final List<FormSelectionMark> selectionMarks = formPage.getSelectionMarks();
            if (selectionMarks != null) {
                for (int j = 0; j < selectionMarks.size(); j++) {
                    final FormSelectionMark selectionMark = selectionMarks.get(j);
                    final StringBuilder boundingBoxStr = new StringBuilder();
                    selectionMark.getBoundingBox().getPoints().forEach(
                        point -> boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                    System.out.printf(
                        "Page: %s, Selection mark is %s within bounding box %s has a confidence score %.2f.%n",
                        selectionMark.getPageNumber(),
                        selectionMark.getState(),
                        boundingBoxStr,
                        selectionMark.getConfidence());
                }
            }
        }
    }
}
