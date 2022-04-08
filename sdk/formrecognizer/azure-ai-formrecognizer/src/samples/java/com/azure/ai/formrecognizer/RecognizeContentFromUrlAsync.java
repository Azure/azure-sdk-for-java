// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
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
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        PollerFlux<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
            client.beginRecognizeContentFromUrl(
                "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer/"
                    + "azure-ai-formrecognizer/src/samples/resources/sample-forms/forms/selectionMarkForm.pdf");

        Mono<List<FormPage>> contentPageResults = recognizeContentPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
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

                System.out.printf("Page has width: %f and height: %f, measured with unit: %s%n", formPage.getWidth(),
                    formPage.getHeight(),
                    formPage.getUnit());

                // Table information
                final List<FormTable> tables = formPage.getTables();
                for (int i1 = 0; i1 < tables.size(); i1++) {
                    final FormTable formTable = tables.get(i1);
                    System.out.printf("Table %d has %d rows and %d columns.%n", i1, formTable.getRowCount(),
                        formTable.getColumnCount());
                    formTable.getCells().forEach(formTableCell ->
                        System.out.printf("Cell has text '%s', within bounding box %s.%n", formTableCell.getText(),
                            formTableCell.getBoundingBox().toString()));
                    System.out.println();
                }

                // Selection Mark
                for (FormSelectionMark selectionMark : formPage.getSelectionMarks()) {
                    System.out.printf(
                        "Page: %s, Selection mark is %s within bounding box %s has a confidence score %.2f.%n",
                        selectionMark.getPageNumber(),
                        selectionMark.getState(),
                        selectionMark.getBoundingBox().toString(),
                        selectionMark.getConfidence());
                }

                // Lines
                formPage.getLines().forEach(formLine -> {
                    if (formLine.getAppearance() != null) {
                        System.out.printf(
                            "Line %s consists of %d words and has a text style %s with a confidence score of %.2f.%n",
                            formLine.getText(), formLine.getWords().size(),
                            formLine.getAppearance().getStyleName(),
                            formLine.getAppearance().getStyleConfidence());
                    }
                });
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
