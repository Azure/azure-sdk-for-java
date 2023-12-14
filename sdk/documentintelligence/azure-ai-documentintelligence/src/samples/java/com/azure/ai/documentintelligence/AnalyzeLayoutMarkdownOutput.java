// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.ContentFormat;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Sample for analyzing layout information from a document given through a file, in a markdown output.
 */
public class AnalyzeLayoutMarkdownOutput {
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

        File invoiceDocument = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/sample-forms/forms/Invoice_6.pdf");

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutResultPoller =
                client.beginAnalyzeDocument("prebuilt-layout", null,
                        null,
                        null,
                        null,
                        null,
                        ContentFormat.MARKDOWN,
                        new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(invoiceDocument.toPath())));

        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();
        System.out.println("Markdown output");
        System.out.println("------------------------------------------------");
        System.out.println(analyzeLayoutResult.getContent());
    }
}
