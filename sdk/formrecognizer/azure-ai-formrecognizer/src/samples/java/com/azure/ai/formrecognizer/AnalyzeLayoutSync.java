// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Sample for extracting layout information using input stream.
 */
public class AnalyzeLayoutSync {

    /**
     * Sample for extracting layout information using input stream.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("C:/Users/savaity/Downloads/mixed-forms/Reg1_3.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<FormPage>> analyzeLayoutPoller =
            client.beginRecognizeContent(targetStream, sourceFile.length(), FormContentType.APPLICATION_PDF);

        IterableStream<FormPage> layoutPageResults = analyzeLayoutPoller.getFinalResult();

        layoutPageResults.forEach(formPage -> {
            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());

            // Table information
            System.out.println("Recognized Tables: ");
            formPage.getTables().forEach(formTable -> {
                for (int i = 0; i < formTable.getRowCount(); i++) {
                    for (int j = 0; j < formTable.getColumnCount(); j++) {
                        int finalJ = j;
                        int finalI = i;
                        Optional<FormTableCell> optionalFormTableCell = formTable.getCells().stream().filter(formTableCell ->
                            formTableCell.getRowIndex() == finalI && formTableCell.getColumnIndex() == finalJ).findFirst();
                        if (optionalFormTableCell.isPresent()) {
                            FormTableCell recognizedTableCell = optionalFormTableCell.get();
                            if (recognizedTableCell.isHeader()) {
                                System.out.println(recognizedTableCell.getText());
                            } else {
                                System.out.printf("%s ", recognizedTableCell.getText());
                            }
                        }
                    }
                    System.out.println();
                }
            });
        });
    }
}
