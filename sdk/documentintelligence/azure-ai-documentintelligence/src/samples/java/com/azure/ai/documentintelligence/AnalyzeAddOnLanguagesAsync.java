// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentLanguage;
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
 * This async sample demonstrates how to extract all identified barcodes using the add-on 'LANGUAGES' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnLanguagesAsync {
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
            + "sample-forms/addOns/fonts_and_languages.png");

        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                null,
                null,
                null,
                Arrays.asList(DocumentAnalysisFeature.LANGUAGES),
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
