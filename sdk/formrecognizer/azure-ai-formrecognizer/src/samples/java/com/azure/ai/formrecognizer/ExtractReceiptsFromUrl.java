// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

/**
 * Sample for extracting receipt information using file source URL.
 */
public class ExtractReceiptsFromUrl {
    /**
     * Sample for extracting receipt information using file source URL.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
        SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> analyzeReceiptPoller =
            client.beginExtractReceiptsFromUrl(receiptUrl);

        IterableStream<ExtractedReceipt> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        receiptPageResults.forEach(extractedReceiptItem -> {
            System.out.printf("Page Number %s%n", extractedReceiptItem.getPageMetadata().getPageNumber());
            System.out.printf("Merchant Name %s%n", extractedReceiptItem.getMerchantName().getText());
            System.out.printf("Merchant Name Value: %s%n", extractedReceiptItem.getMerchantName().getValue());
            System.out.printf("Merchant Address %s%n", extractedReceiptItem.getMerchantAddress().getText());
            System.out.printf("Merchant Address Value: %s%n", extractedReceiptItem.getMerchantAddress().getValue());
            System.out.printf("Merchant Phone Number %s%n", extractedReceiptItem.getMerchantPhoneNumber().getText());
            System.out.printf("Merchant Phone Number Value: %s%n", extractedReceiptItem.getMerchantPhoneNumber().getValue());
            System.out.printf("Total: %s%n", extractedReceiptItem.getTotal().getText());
            System.out.printf("Total Value: %s%n", extractedReceiptItem.getTotal().getValue());
            System.out.printf("Receipt Items: %n");
            extractedReceiptItem.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getText());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity() == null
                    ? "N/A" : receiptItem.getQuantity().getText());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
                System.out.println();
            });
        });
    }
}
