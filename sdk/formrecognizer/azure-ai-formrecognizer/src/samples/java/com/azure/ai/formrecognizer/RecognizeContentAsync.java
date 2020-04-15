// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Sample for extracting layout information using input stream.
 */
public class RecognizeContentAsync {

    /**
     * Sample for extracting layout information using input stream.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File sourceFile = new File("C/.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        PollerFlux<OperationResult, IterableStream<FormPage>> analyzeLayoutPoller =
            client.beginRecognizeContentFromUrl("source url");

        IterableStream<FormPage> layoutPageResults = analyzeLayoutPoller
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

        layoutPageResults.forEach(formPage -> {
            // Table information
            System.out.println("----Recognizing content ----");
            System.out.printf("Has width: %s and height: %s, measured with unit: %s%n", formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());
            formPage.getTables().forEach(formTable -> {
                System.out.printf("Table has %s rows and %s columns%n", formTable.getRowCount(),
                    formTable.getColumnCount());
                for (int i = 0; i < formTable.getRowCount(); i++) {
                    for (int j = 0; j < formTable.getColumnCount(); j++) {
                        int finalJ = j;
                        int finalI = i;
                        Optional<FormTableCell> optionalFormTableCell =
                            formTable.getCells().stream().filter(formTableCell ->
                                formTableCell.getRowIndex() == finalI && formTableCell.getColumnIndex() == finalJ).findFirst();
                        if (optionalFormTableCell.isPresent()) {
                            FormTableCell recognizedTableCell = optionalFormTableCell.get();
                            if (recognizedTableCell.isHeader()) {
                                System.out.println(recognizedTableCell.getText());
                            } else {
                                // System.out.printf("Cell text %s within bounding box %s%n", recognizedTableCell
                                // .getText(),
                                //     recognizedTableCell.getBoundingBox().getPoints().forEach(point ->
                                //         String.format("[%s, %s]", point.getX(), point.getY())));
                            }
                        }
                    }
                    System.out.println();
                }
            });
        });
    }
}
