// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for analyzing layout information from a document given through a file.
 */
public class AnalyzeLayoutAsync {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisAsyncClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/forms/selectionMarkForm.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        PollerFlux<DocumentOperationResult, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                Utility.toFluxByteBuffer(targetStream),
                sourceFile.length());

        Mono<AnalyzeResult> analyzeLayoutResultMono =
            analyzeLayoutPoller
                .last()
                .flatMap(pollResponse -> {
                    if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(pollResponse.getStatus())) {
                        System.out.println("Polling completed successfully");
                        return pollResponse.getFinalResult();
                    } else {
                        return Mono.error(
                            new RuntimeException(
                                "Polling completed unsuccessfully with status:" + pollResponse.getStatus()));
                    }
                });

        analyzeLayoutResultMono.subscribe(analyzeLayoutResult -> {
            // pages
            analyzeLayoutResult.getPages().forEach(documentPage -> {
                System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                    documentPage.getWidth(),
                    documentPage.getHeight(),
                    documentPage.getUnit());

                // lines
                documentPage.getLines().forEach(documentLine ->
                    System.out.printf("Line %s is within a bounding box %s.%n",
                        documentLine.getContent(),
                        documentLine.getBoundingBox().toString()));

                // words
                documentPage.getWords().forEach(documentWord ->
                    System.out.printf("Word %s has a confidence score of %.2f%n.",
                        documentWord.getContent(),
                        documentWord.getConfidence()));

                // selection marks
                documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                    System.out.printf("Selection mark is %s and is within a bounding box %s with confidence %.2f.%n",
                        documentSelectionMark.getState().toString(),
                        documentSelectionMark.getBoundingBox().toString(),
                        documentSelectionMark.getConfidence()));
            });

            // tables
            List<DocumentTable> tables = analyzeLayoutResult.getTables();
            for (int i = 0; i < tables.size(); i++) {
                DocumentTable documentTable = tables.get(i);
                System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
                    documentTable.getColumnCount());
                documentTable.getCells().forEach(documentTableCell -> {
                    System.out.printf("Cell '%s', has row index %d and column index %d.%n",
                        documentTableCell.getContent(),
                        documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
                });
                System.out.println();
            }

            // styles
            analyzeLayoutResult.getStyles().forEach(documentStyle
                -> System.out.printf("Document is handwritten %s%n.", documentStyle.isHandwritten()));
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
