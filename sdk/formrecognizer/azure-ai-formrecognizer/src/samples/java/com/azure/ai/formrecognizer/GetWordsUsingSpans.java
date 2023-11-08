// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentWord;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample for highlighting the usage of spans information. Specifically here, to extract the exact words
 * contained in a line in the given document.
 */
public class GetWordsUsingSpans {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File selectionMarkDocument = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/forms/Form_1.jpg");
        Path filePath = selectionMarkDocument.toPath();
        BinaryData selectionMarkDocumentData = BinaryData.fromFile(filePath, (int) selectionMarkDocument.length());

        SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutResultPoller =
            client.beginAnalyzeDocument("prebuilt-layout", selectionMarkDocumentData);

        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

        // pages
        analyzeLayoutResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                documentPage.getWidth(),
                documentPage.getHeight(),
                documentPage.getUnit());

            // lines
            documentPage.getLines().forEach(documentLine -> {
                System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                    documentLine.getContent(),
                    getBoundingCoordinates(documentLine.getBoundingPolygon()));

                List<DocumentWord> containedWords = documentLine.getWords();

                System.out.printf("Total number of words in the line: %d.%n", containedWords.size());
                System.out.printf("Words contained in the line are: %s.%n",
                    containedWords.stream().map(DocumentWord::getContent).collect(Collectors.toList()));
            });
        });
    }

    /**
     * Utility function to get the bounding polygon coordinates.
     */
    private static String getBoundingCoordinates(List<Point> boundingPolygon) {
        return boundingPolygon.stream().map(point -> String.format("[%.2f, %.2f]", point.getX(),
            point.getY())).collect(Collectors.joining(", "));
    }
}
