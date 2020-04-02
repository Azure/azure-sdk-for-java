// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

/**
 * Sample for extracting receipt information using file source URL.
 */
public class ExtractPrebuiltReceiptAsync {
    /**
     * Sample for extracting receipt information using input stream.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String receiptUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
        PollerFlux<OperationResult, IterableStream<ExtractedReceipt>> analyzeReceiptPoller =
            client.beginExtractReceiptsFromUrl(receiptUrl);

        IterableStream<ExtractedReceipt> receiptPageResults = analyzeReceiptPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    System.out.println("polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus());
                    return Mono.empty();
                }
            }).block();

        receiptPageResults.forEach(extractedReceiptItem -> {
            System.out.printf("Page Number %s%n", extractedReceiptItem.getPageMetadata().getPageNumber());
            System.out.printf("Merchant Name %s%n", extractedReceiptItem.getMerchantName().getText());
            System.out.printf("Merchant Address %s%n", extractedReceiptItem.getMerchantAddress().getText());
            System.out.printf("Merchant Phone Number %s%n", extractedReceiptItem.getMerchantPhoneNumber().getText());
            System.out.printf("Total: %s%n", extractedReceiptItem.getTotal().getText());
            System.out.printf("Receipt Items: %n");
            extractedReceiptItem.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getText());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity().getText());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
                System.out.println();
            });
        });
    }
}
