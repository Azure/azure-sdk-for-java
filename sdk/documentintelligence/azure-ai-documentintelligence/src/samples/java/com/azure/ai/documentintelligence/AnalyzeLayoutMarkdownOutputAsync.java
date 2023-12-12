// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.ContentFormat;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Sample for analyzing layout information from a document given through a file, in a markdown output.
 */
public class AnalyzeLayoutMarkdownOutputAsync {
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

        File invoiceDocument = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/sample-forms/forms/Invoice_6.pdf");

        PollerFlux<AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutPoller =
                client.beginAnalyzeDocument("prebuilt-layout",
                        null,
                        null,
                        null,
                        null,
                        null,
                        ContentFormat.MARKDOWN,
                        new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(invoiceDocument.toPath())));

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
                        }).map(AnalyzeResultOperation::getAnalyzeResult);

        analyzeLayoutResultMono.subscribe(analyzeLayoutResult -> {
            System.out.println("Markdown output");
            System.out.println("------------------------------------------------");
            System.out.println(analyzeLayoutResult.getContent());
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
