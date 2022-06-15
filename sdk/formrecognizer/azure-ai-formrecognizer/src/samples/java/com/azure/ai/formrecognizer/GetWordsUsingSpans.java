// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentLine;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.DocumentWord;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
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
        byte[] fileContent = Files.readAllBytes(selectionMarkDocument.toPath());
        InputStream fileStream = new ByteArrayInputStream(fileContent);

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeLayoutResultPoller =
            client.beginAnalyzeDocument("prebuilt-layout", fileStream, selectionMarkDocument.length());

        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

        // pages
        analyzeLayoutResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                documentPage.getWidth(),
                documentPage.getHeight(),
                documentPage.getUnit());

            // lines
            documentPage.getLines().forEach(documentLine -> {
                System.out.printf("Line '%s' is within a bounding box %s.%n",
                    documentLine.getContent(),
                    documentLine.getBoundingPolygon().toString());

                List<DocumentWord> containedWords = getWordsInALine(documentLine, documentPage.getWords());

                System.out.printf("Total number of words in the line: %d.%n", containedWords.size());
                System.out.printf("Words contained in the line are: %s.%n",
                    containedWords.stream().map(DocumentWord::getContent).collect(Collectors.toList()));
            });
        });
    }

    /**
     * Utility function to get all the words contained in a line.
     */
    private static List<DocumentWord> getWordsInALine(DocumentLine documentLine, List<DocumentWord> pageWords) {
        List<DocumentWord> containedWords = new ArrayList<>();
        pageWords.forEach(documentWord -> {
            documentLine.getSpans().forEach(documentSpan -> {
                if ((documentWord.getSpan().getOffset() >= documentSpan.getOffset())
                    && ((documentWord.getSpan().getOffset()
                         + documentWord.getSpan().getLength()) <= (documentSpan.getOffset() + documentSpan.getLength()))) {
                    containedWords.add(documentWord);
                }
            });
        });

        return containedWords;
    }
}
