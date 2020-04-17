// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/*
 * Sample for recognizing fields from custom forms.
 */
public class AnalyzeForm {

    /**
     * Main method to invoke this demo to analyze custom forms to extract information.
     *
     * @param args Unused arguments to the program.
     *
     */
    public static void main(String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";
        PollerFlux<OperationResult, IterableStream<RecognizedForm>> trainingPoller = client.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId);

        IterableStream<RecognizedForm> recognizedForms = trainingPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            }).block();

        System.out.println("Page Metadata: ");
        recognizedForms.forEach(recognizedForm -> {
            recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
            });

            // Page Information
            recognizedForm.getPages().forEach(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                System.out.println();
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable -> {
                    for (int i = 0; i < formTable.getRowCount(); i++) {
                        for (int j = 0; j < formTable.getColumnCount(); j++) {
                            int finalJ = j;
                            int finalI = i;
                            Optional<FormTableCell> p = formTable.getCells().stream().filter(formTableCell -> formTableCell.getRowIndex() == finalI && formTableCell.getColumnIndex() == finalJ).findFirst();
                            if (p.isPresent()) {
                                System.out.printf("%s || ", p.get().getText());
                            } else {
                                System.out.printf(" ");
                            }
                        }
                        System.out.println();
                    }
                });
            });
        });
    }
}
