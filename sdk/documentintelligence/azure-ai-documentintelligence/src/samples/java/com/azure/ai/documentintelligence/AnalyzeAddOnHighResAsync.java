// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentStyle;
import com.azure.ai.documentintelligence.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This async sample demonstrates how to extract all identified barcodes using the add-on 'OCR_HIGH_RESOLUTION' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnHighResAsync {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAsyncClient client = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .buildAsyncClient();

        File document = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
            + "sample-forms/addOns/highres.png");

        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                null,
                null,
                null,
                Arrays.asList(DocumentAnalysisFeature.OCR_HIGH_RESOLUTION),
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));

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
            // styles
            List<DocumentStyle> documentStyles = analyzeLayoutResult.getStyles();

            boolean isDocumentContainsHandwritten = documentStyles.stream().anyMatch(documentStyle -> {
                Boolean handwritten = documentStyle.isHandwritten();
                return handwritten != null && handwritten;
            });

            if (isDocumentContainsHandwritten) {
                System.out.println("Document contains handwritten content");
            } else {
                System.out.println("Document does not contains handwritten content");
            }

            // pages
            analyzeLayoutResult.getPages().forEach(documentPage -> {
                System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                    documentPage.getWidth(),
                    documentPage.getHeight(),
                    documentPage.getUnit());

                // lines
                documentPage.getLines().forEach(documentLine ->
                    System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                        documentLine.getContent(),
                        documentLine.getPolygon()));

                // words
                documentPage.getWords().forEach(documentWord ->
                    System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                        documentWord.getContent(),
                        documentWord.getConfidence()));

                // selection marks
                documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                    System.out.printf("Selection mark is '%s' and is within a bounding polygon %s with confidence %.2f.%n",
                        documentSelectionMark.getState().toString(),
                        documentSelectionMark.getPolygon(),
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
