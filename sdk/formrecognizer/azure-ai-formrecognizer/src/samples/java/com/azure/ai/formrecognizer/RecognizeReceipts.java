// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Sample for recognizing US receipt information using file source URL.
 */
public class RecognizeReceipts {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException from reading file.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/"
            + "receipts/contoso-allinone.jpg");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginRecognizeReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG);

        IterableStream<RecognizedReceipt> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        receiptPageResults.forEach(recognizedReceipt -> {
            System.out.println("----------- Recognized Receipt -----------");
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name: %s, confidence: %.2f%n", usReceipt.getMerchantName().getFieldValue(), usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address: %s, confidence: %.2f%n", usReceipt.getMerchantAddress().getName(), usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s, confidence: %.2f%n", usReceipt.getMerchantPhoneNumber().getFieldValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %s confidence: %.2f%n", usReceipt.getTotal().getName(), usReceipt.getTotal().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                if (receiptItem.getName() != null) {
                    System.out.printf("Name: %s, confidence: %.2fs%n", receiptItem.getName().getFieldValue(), receiptItem.getName().getConfidence());
                }
                if (receiptItem.getQuantity() != null) {
                    System.out.printf("Quantity: %s, confidence: %.2f%n", receiptItem.getQuantity().getFieldValue(), receiptItem.getQuantity().getConfidence());
                }
                if (receiptItem.getPrice() != null) {
                    System.out.printf("Price: %s, confidence: %.2f%n", receiptItem.getPrice().getFieldValue(), receiptItem.getPrice().getConfidence());
                }
                if (receiptItem.getTotalPrice() != null) {
                    System.out.printf("Total Price: %s, confidence: %.2f%n", receiptItem.getTotalPrice().getFieldValue(), receiptItem.getTotalPrice().getConfidence());
                }
            });
            System.out.print("-----------------------------------");
        });
    }
}
