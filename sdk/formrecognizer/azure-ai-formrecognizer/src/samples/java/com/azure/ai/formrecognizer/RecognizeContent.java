// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Sample for recognizing content information from a document given through a file.
 */
public class RecognizeContent {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
                                       + "sample-forms/forms/selectionMarkForm.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
            client.beginRecognizeContent(targetStream, sourceFile.length());

        List<FormPage> contentPageResults = recognizeContentPoller.getFinalResult();

        for (int i = 0; i < contentPageResults.size(); i++) {
            final FormPage formPage = contentPageResults.get(i);
            System.out.printf("---- Recognized content info for page %d ----%n", i);

            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());

            // Table information
            final List<FormTable> tables = formPage.getTables();
            for (int i1 = 0; i1 < tables.size(); i1++) {
                final FormTable formTable = tables.get(i1);
                System.out.printf("Table %d has %d rows and %d columns.%n", i1, formTable.getRowCount(),
                    formTable.getColumnCount());
                formTable.getCells().forEach(formTableCell -> {
                    System.out.printf("Cell has text '%s', within bounding box %s.%n", formTableCell.getText(),
                        formTableCell.getBoundingBox().toString());
                });
                System.out.println();
            }

            // Selection Mark
            for (FormSelectionMark selectionMark : formPage.getSelectionMarks()) {
                System.out.printf(
                    "Page: %s, Selection mark is %s within bounding box %s has a confidence score %.2f.%n",
                    selectionMark.getPageNumber(),
                    selectionMark.getState(),
                    selectionMark.getBoundingBox().toString(),
                    selectionMark.getConfidence());
            }

            // Lines
            formPage.getLines().forEach(formLine -> {
                if (formLine.getAppearance() != null) {
                    System.out.printf(
                        "Line %s consists of %d words and has a text style %s with a confidence score of %.2f.%n",
                        formLine.getText(), formLine.getWords().size(),
                        formLine.getAppearance().getStyleName(),
                        formLine.getAppearance().getStyleConfidence());
                }
            });
        }
    }
}
