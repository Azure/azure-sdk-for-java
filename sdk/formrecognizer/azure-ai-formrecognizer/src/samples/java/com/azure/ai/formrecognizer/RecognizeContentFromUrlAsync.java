// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for recognizing content information from a document given through a URL.
 */
public class RecognizeContentFromUrlAsync {

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

        PollerFlux<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
            client.beginRecognizeContentFromUrl("https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/forms/layout1.jpg");

        Mono<List<FormPage>> contentPageResults = recognizeContentPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        contentPageResults.subscribe(formPages -> {
            for (int i = 0; i < formPages.size(); i++) {
                final FormPage formPage = formPages.get(i);
                System.out.printf("---- Recognized content info for page %d ----%n", i);
                System.out.printf("Has width: %f and height: %f, measured with unit: %s%n", formPage.getWidth(),
                    formPage.getHeight(),
                    formPage.getUnit());
                // Table information
                final List<FormTable> tables = formPage.getTables();
                for (int i1 = 0; i1 < tables.size(); i1++) {
                    final FormTable formTable = tables.get(i1);
                    System.out.printf("Table %d has %d rows and %d columns.%n", i1, formTable.getRowCount(),
                        formTable.getColumnCount());
                    formTable.getCells().forEach(formTableCell -> {
                        final StringBuilder boundingBoxStr = new StringBuilder();
                        if (formTableCell.getBoundingBox() != null) {
                            formTableCell.getBoundingBox().getPoints().forEach(point ->
                                boundingBoxStr.append(String.format("[%.2f, %.2f]", point.getX(), point.getY())));
                        }
                        System.out.printf("Cell has text '%s', within bounding box %s.%n", formTableCell.getText(),
                            boundingBoxStr);
                    });
                    System.out.println();
                }
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
