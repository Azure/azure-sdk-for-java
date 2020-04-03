// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class AnalyzeLayout {
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String layoutUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-invoice.png";

        PollerFlux<OperationResult, IterableStream<FormPage>> analyzeLayoutPoller =
            client.beginExtractContentFromUrl(layoutUrl);

        IterableStream<FormPage> layoutPageResults = analyzeLayoutPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    System.out.println("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus());
                    return Mono.empty();
                }
            }).block();

        layoutPageResults.forEach(formPage -> {
            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());

            // Table information
            System.out.printf("Recognized Tables: ");
            formPage.getTables().forEach(formTable -> {
                for (int i = 0; i < formTable.getRowCount(); i++) {
                    for (int j = 0; j < formTable.getColumnCount(); j++) {
                        int finalJ = j;
                        int finalI = i;
                        Optional<FormTableCell> formTableCell1 = formTable.getCells().stream().filter(formTableCell ->
                            formTableCell.getRowIndex() == finalI && formTableCell.getColumnIndex() == finalJ).findFirst();
                        System.out.printf("%s || ", formTableCell1.get().getText());
                    }
                    System.out.println();
                }
            });
        });
    }
}
