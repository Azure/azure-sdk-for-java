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
import java.time.Duration;
import java.util.Optional;

/**
 * Sample for analyzing layout information.
 */
public class AnalyzeLayout {

    /**
     * Main method to invoke this demo to analyze layout information for provided document.
     *
     * @param args Unused arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("48c9ec5b1c444c899770946defc486c4"))
            .endpoint("https://javaformrecognizertestresource.cognitiveservices.azure.com/")
            .buildClient();

        // String layoutUrl = "/C:/Users/savaity/Downloads/layout1.jpg";

        File layoutUrl = new File("/C:/Users/savaity/Downloads/layout1.jpg");
        byte[] fileContent = Files.readAllBytes(layoutUrl.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<FormPage>> analyzeLayoutPoller =
            client.beginExtractContent(targetStream, layoutUrl.length(), FormContentType.IMAGE_JPEG, Duration.ofSeconds(5));

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
                            System.out.printf("%s || ", optionalFormTableCell.get().getText());
                        } else {
                            System.out.printf("N/A || ");
                        }
                    }
                    System.out.println();
                }
            });
        });
    }
}
