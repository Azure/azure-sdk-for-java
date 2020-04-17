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
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for recognizing US receipt information using file source URL.
 */
public class RecognizeReceiptsFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources/sample-files/contoso-allinone.jpg";
        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> recognizeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        IterableStream<RecognizedReceipt> receiptPageResults = recognizeReceiptPoller.getFinalResult();

        receiptPageResults.forEach(recognizedReceipt -> {
            System.out.println("----------- Recognized Receipt -----------");
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name: %s, confidence: %s%n", usReceipt.getMerchantName().getFieldValue(), usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address: %s, confidence: %s%n", usReceipt.getMerchantAddress().getName(), usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s, confidence: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %s confidence: %s%n", usReceipt.getTotal().getName(), usReceipt.getTotal().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                if (receiptItem.getName() != null) {
                    System.out.printf("Name: %s, confidence: %s%n", receiptItem.getName().getFieldValue(), receiptItem.getName().getConfidence());
                }
                if (receiptItem.getQuantity() != null) {
                    System.out.printf("Quantity: %s, confidence: %s%n", receiptItem.getQuantity().getFieldValue(), receiptItem.getQuantity().getConfidence());
                }
                if (receiptItem.getPrice() != null) {
                    System.out.printf("Price: %s, confidence: %s%n", receiptItem.getPrice().getFieldValue(), receiptItem.getPrice().getConfidence());
                }
                if (receiptItem.getTotalPrice() != null) {
                    System.out.printf("Total Price: %s, confidence: %s%n", receiptItem.getTotalPrice().getFieldValue(), receiptItem.getTotalPrice().getConfidence());
                }
            });
            System.out.print("-----------------------------------");
        });
    }
}
