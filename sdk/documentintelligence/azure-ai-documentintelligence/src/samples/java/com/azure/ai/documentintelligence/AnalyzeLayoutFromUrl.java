// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * Sample for analyzing content information from a document given through a URL.
 */
public class AnalyzeLayoutFromUrl {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                null,
                null,
                null,
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setUrlSource("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/documentintelligence/"
                    + "azure-ai-documentintelligence/src/samples/resources/sample-forms/forms/selectionMarkForm.pdf"));

        AnalyzeResult analyzeLayoutResult = analyzeLayoutPoller.getFinalResult();

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
                System.out.printf("Cell '%s', has row index %d and column index %d.%n", documentTableCell.getContent(),
                    documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
            });
            System.out.println();
        }

        // styles
        analyzeLayoutResult.getStyles().forEach(documentStyle
            -> System.out.printf("Document is handwritten %s.%n", documentStyle.isHandwritten()));
    }
}
