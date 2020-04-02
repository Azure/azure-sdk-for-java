// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Sample for extracting receipt information using input stream.
 */
public class ExtractPrebuiltReceiptSync {

    /**
     * Sample for extracting receipt information using input stream.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("///contoso-allinone.jpg");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> analyzeReceiptPoller =
            client.beginExtractReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_PNG, true,
                Duration.ofSeconds(5));

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
