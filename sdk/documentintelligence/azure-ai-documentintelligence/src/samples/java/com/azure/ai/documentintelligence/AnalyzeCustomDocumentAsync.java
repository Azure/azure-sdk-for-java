// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.ContentFormat;
import com.azure.ai.documentintelligence.models.Document;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentTable;
import com.azure.ai.documentintelligence.models.StringIndexType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Async sample to analyze a custom document with a custom-built model. To learn how to build your own models,
 * look at BuildDocumentModelAsync.java and BuildDocumentModel.java.
 */
public class AnalyzeCustomDocumentAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAsyncClient client = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        // The document you are analyzing must be of the same type as the documents provided for building the custom document analysis model
        File sourceFile = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
            + "sample-forms/forms/Invoice_6.pdf");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        String modelId = "{modelId}";
        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeDocumentPoller
            = client.beginAnalyzeDocument(modelId,
            "1",
            "en-US",
            StringIndexType.TEXT_ELEMENTS,
            Arrays.asList(DocumentAnalysisFeature.LANGUAGES),
            null,
            ContentFormat.TEXT, null, new AnalyzeDocumentRequest().setBase64Source(fileContent));


        Mono<AnalyzeResult> analyzeDocumentResult = analyzeDocumentPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        analyzeDocumentResult.subscribe(analyzeResult -> {
            for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
                Document analyzedDocument = analyzeResult.getDocuments().get(i);
                System.out.printf("----------- Analyzing custom document %d -----------%n", i);
                System.out.printf("Analyzed document has doc type %s with confidence : %.2f%n",
                    analyzedDocument.getDocType(), analyzedDocument.getConfidence());
            }

            analyzeResult.getPages().forEach(documentPage -> {
                System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                    documentPage.getWidth(),
                    documentPage.getHeight(),
                    documentPage.getUnit());

                // lines
                documentPage.getLines().forEach(documentLine ->
                    System.out.printf("Line '%s' is within a bounding box %s.%n",
                        documentLine.getContent(),
                        documentLine.getPolygon().toString()));

                // words
                documentPage.getWords().forEach(documentWord ->
                    System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                        documentWord.getContent(),
                        documentWord.getConfidence()));
            });

            // tables
            List<DocumentTable> tables = analyzeResult.getTables();
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
