// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentLanguage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates how to extract all identified barcodes using the add-on 'LANGUAGES' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnLanguages {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .buildClient();

        File document = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
                + "sample-forms/addOns/fonts_and_languages.png");

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutResultPoller =
            client.beginAnalyzeDocument("prebuilt-layout", null,
                null,
                null,
                Arrays.asList(DocumentAnalysisFeature.LANGUAGES),
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));

        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

        System.out.println("----Languages detected in the document----");
        List<DocumentLanguage> languages = analyzeLayoutResult.getLanguages();
        String content = analyzeLayoutResult.getContent();
        languages.forEach(language -> {
            System.out.printf("- Language local is \"%s\", confidence: %.2f%n",
                    language.getLocale(), language.getConfidence());
            language.getSpans()
                    .forEach(span -> {
                        int offset = span.getOffset();
                        System.out.println(" content: " + content.substring(offset, offset + span.getLength()));
                    });
        });
    }
}
