// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentFormula;
import com.azure.ai.documentintelligence.models.DocumentFormulaKind;
import com.azure.ai.documentintelligence.models.DocumentPage;
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
 * This async sample demonstrates how to extract all identified barcodes using the add-on 'FORMULAS' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnFormulasAsync {
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
                + "sample-forms/addOns/formulas.pdf");

        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutPoller =
            client.beginAnalyzeDocument("prebuilt-layout",
                null,
                null,
                null,
                Arrays.asList(DocumentAnalysisFeature.FORMULAS),
                null,
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
            List<DocumentPage> pages = analyzeLayoutResult.getPages();
            for (int i = 0; i < pages.size(); i++) {
                DocumentPage documentPage = pages.get(i);
                System.out.printf("----Formulas detected from page #%d----%n", i);
                List<DocumentFormula> formulas = documentPage.getFormulas();
                formulas.stream()
                        .filter(f -> DocumentFormulaKind.INLINE.equals(f.getKind()))
                        .forEach(f -> System.out.printf("- Inline: %s, confidence: %.2f, bounding region: %s%n",
                                f.getValue(), f.getConfidence(), f.getPolygon()));
                formulas.stream()
                        .filter(f -> DocumentFormulaKind.DISPLAY.equals(f.getKind()))
                        .forEach(f -> System.out.printf("- Display: %s, confidence: %.2f, bounding region: %s%n",
                                f.getValue(), f.getConfidence(), f.getPolygon()));
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
