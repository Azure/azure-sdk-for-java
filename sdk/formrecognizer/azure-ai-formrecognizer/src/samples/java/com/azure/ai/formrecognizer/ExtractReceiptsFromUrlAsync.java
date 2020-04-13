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

/**
 * Sample for extracting receipt information using file source URL.
 */
public class ExtractReceiptsFromUrlAsync {
    /**
     * Sample for extracting receipt information using file source URL.
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
        PollerFlux<OperationResult, IterableStream<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        IterableStream<RecognizedReceipt> receiptPageResults = analyzeReceiptPoller
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
            }).block();

        receiptPageResults.forEach(recognizedReceipt -> {
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
            System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
            System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
            System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
            System.out.printf("Merchant Phone Number %s%n", usReceipt.getMerchantPhoneNumber().getName());
            System.out.printf("Merchant Phone Number Value: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue());
            System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
            System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getFieldValue());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity() == null
                    ? "N/A" : receiptItem.getQuantity().getFieldValue());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getFieldValue());
                System.out.println();
            });

            // Page Information
            System.out.println("Page Information:");
            recognizedReceipt.getRecognizedForm().getPages().forEach(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
            });
        });
    }
}
