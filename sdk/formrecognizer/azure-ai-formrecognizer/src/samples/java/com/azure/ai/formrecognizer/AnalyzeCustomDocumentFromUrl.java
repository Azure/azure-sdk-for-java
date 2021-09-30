// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * Sample to analyze a custom document with a custom-built model. To learn how to build your own models,
 * look at BuildModelAsync.java and BuildModel.java.
 */
public class AnalyzeCustomDocumentFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String formUrl = "{document-url}";
        String modelId = "{custom-built-model-ID}";
        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeDocumentPoller =
            client.beginAnalyzeDocumentFromUrl(modelId, formUrl);

        AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

        for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
            final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(i);
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
                System.out.printf("Line %s is within a bounding box %s.%n",
                    documentLine.getContent(),
                    documentLine.getBoundingBox().toString()));

            // words
            documentPage.getWords().forEach(documentWord ->
                System.out.printf("Word %s has a confidence score of %.2f%n.",
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
    }
}
