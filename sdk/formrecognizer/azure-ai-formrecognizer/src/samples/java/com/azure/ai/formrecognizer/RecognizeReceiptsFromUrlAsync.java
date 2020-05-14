// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Async sample for recognizing US receipt information using file source URL.
 */
public class RecognizeReceiptsFromUrlAsync {

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

        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/receipts/contoso-allinone.jpg";
        PollerFlux<OperationResult, IterableStream<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        Mono<IterableStream<RecognizedReceipt>> receiptPageResults = analyzeReceiptPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        receiptPageResults.subscribe(recognizedReceipts -> recognizedReceipts.forEach(recognizedReceipt -> {
            System.out.println("----------- Recognized Receipt -----------");
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name: %s, confidence: %.2f%n", usReceipt.getMerchantName().getFieldValue(),
                usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address: %s, confidence: %.2f%n", usReceipt.getMerchantAddress().getName(),
                usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                usReceipt.getMerchantPhoneNumber().getFieldValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %s confidence: %.2f%n", usReceipt.getTotal().getName(),
                usReceipt.getTotal().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                if (receiptItem.getName() != null) {
                    System.out.printf("Name: %s, confidence: %.2f%n", receiptItem.getName().getFieldValue(),
                        receiptItem.getName().getConfidence());
                }
                if (receiptItem.getQuantity() != null) {
                    System.out.printf("Quantity: %s, confidence: %.2f%n", receiptItem.getQuantity().getFieldValue(),
                        receiptItem.getQuantity().getConfidence());
                }
                if (receiptItem.getPrice() != null) {
                    System.out.printf("Price: %s, confidence: %.2f%n", receiptItem.getPrice().getFieldValue(),
                        receiptItem.getPrice().getConfidence());
                }
                if (receiptItem.getTotalPrice() != null) {
                    System.out.printf("Total Price: %s, confidence: %.2f%n",
                        receiptItem.getTotalPrice().getFieldValue(), receiptItem.getTotalPrice().getConfidence());
                }
            });
            System.out.print("-----------------------------------");
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
