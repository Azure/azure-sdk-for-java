// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

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
     * Sample for recognizing US receipt information using file source URL.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException from reading file.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginRecognizeReceipts(targetStream, sourceFile.length(), FormContentType.APPLICATION_PDF);

        IterableStream<RecognizedReceipt> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        receiptPageResults.forEach(recognizedReceipt -> {
            System.out.println("----------- Recognized Receipt -----------");
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name: %s, confidence: %s%n", usReceipt.getMerchantName().getFieldValue(), usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
            System.out.printf("Merchant Address: %s, confidence: %s%n", usReceipt.getMerchantAddress().getFieldValue(), usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s%n", usReceipt.getMerchantPhoneNumber().getName());
            System.out.printf("Merchant Phone Number: %s, confidence: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
            System.out.printf("Total: %s, confidence: %s%n", usReceipt.getTotal().getFieldValue(), usReceipt.getTotal().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s, confidence: %s%n", receiptItem.getName() == null
                    ? "N/A" : receiptItem.getName().getFieldValue(), receiptItem.getName().getConfidence());
                System.out.printf("Quantity: %s, confidence: %s%n", receiptItem.getQuantity() == null
                    ? "N/A" : receiptItem.getQuantity().getFieldValue(), receiptItem.getQuantity().getConfidence());
                System.out.printf("Price: %s, confidence: %s%n", receiptItem.getPrice() == null
                    ? "N/A" : receiptItem.getPrice().getFieldValue(), receiptItem.getPrice().getConfidence());
                System.out.printf("Total Price: %s, confidence: %s%n", receiptItem.getTotalPrice() == null
                    ? "N/A" : receiptItem.getTotalPrice(), receiptItem.getTotalPrice().getConfidence());
                System.out.println();
            });
            System.out.print("-----------------------------------");
        });
    }
}
