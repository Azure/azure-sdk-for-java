// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Async sample for recognizing content information from a document given through a URL.
 */
public class RecognizeContentAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args)  {
        // Instantiate a client that will be used to call the service.

        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        PollerFlux<OperationResult, IterableStream<FormPage>> recognizeLayoutPoller =
            client.beginRecognizeContentFromUrl("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/forms/layout1.jpg");

        Mono<IterableStream<FormPage>> layoutPageResults = recognizeLayoutPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        layoutPageResults.subscribe(formPages -> formPages.forEach(formPage -> {
            // Table information
            System.out.println("----Recognizing content ----");
            System.out.printf("Has width: %s and height: %s, measured with unit: %s%n", formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());
            formPage.getTables().forEach(formTable -> {
                System.out.printf("Table has %s rows and %s columns.%n", formTable.getRowCount(),
                    formTable.getColumnCount());
                formTable.getCells().forEach(formTableCell -> {
                    final StringBuilder boundingBoxStr = new StringBuilder();
                    if (formTableCell.getBoundingBox() != null) {
                        formTableCell.getBoundingBox().getPoints().forEach(point ->
                            boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                    }
                    System.out.printf("Cell has text %s, within bounding box %s.%n", formTableCell.getText(),
                        boundingBoxStr);
                });
                System.out.println();
            });
        }));

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
