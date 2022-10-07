// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;
import java.util.stream.Collectors;

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
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-layout",
                "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer/"
                    + "azure-ai-formrecognizer/src/samples/resources/sample-forms/forms/selectionMarkForm.pdf");

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
                    getBoundingCoordinates(documentLine.getBoundingPolygon())));

            // words
            documentPage.getWords().forEach(documentWord ->
                System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                    documentWord.getContent(),
                    documentWord.getConfidence()));

            // selection marks
            documentPage.getSelectionMarks().forEach(documentSelectionMark ->
                System.out.printf("Selection mark is '%s' and is within a bounding polygon %s with confidence %.2f.%n",
                    documentSelectionMark.getSelectionMarkState().toString(),
                    getBoundingCoordinates(documentSelectionMark.getBoundingPolygon()),
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

    /**
     * Utility function to get the bounding polygon coordinates.
     */
    private static String getBoundingCoordinates(List<Point> boundingPolygon) {
        return boundingPolygon.stream().map(point -> String.format("[%.2f, %.2f]", point.getX(),
            point.getY())).collect(Collectors.joining(", "));
    }
}
