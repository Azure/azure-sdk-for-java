// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerApiKeyCredential;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptPageResult;
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
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        final FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new FormRecognizerApiKeyCredential("0679e81143284edb941ed00628dcb689"))
            .endpoint("https://form-recognizer-canary.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("/C:/Users/savaity/Downloads/fr-training-with-labels/contoso-receipt.png");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        SyncPoller<OperationResult, IterableStream<ReceiptPageResult>> analyzeReceiptPoller =
            client.beginExtractReceipt(targetStream, sourceFile.length(), FormContentType.IMAGE_PNG, true,
                Duration.ofSeconds(4));

        IterableStream<ReceiptPageResult> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        for (ReceiptPageResult receiptPageResultItem : receiptPageResults) {
            System.out.printf("Page Number %s%n", receiptPageResultItem.getPageInfo().getPageNumber());
            System.out.printf("Merchant Name %s%n", receiptPageResultItem.getMerchantName().getText());
            System.out.printf("Merchant Address %s%n", receiptPageResultItem.getMerchantAddress().getText());
            System.out.printf("Merchant Phone Number %s%n", receiptPageResultItem.getMerchantPhoneNumber().getText());
            System.out.printf("Total: %s%n", receiptPageResultItem.getTotal().getText());
            System.out.printf("Receipt Items: %n");
            receiptPageResultItem.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getText());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity() == null
                    ? "N/A" : receiptItem.getQuantity().getText());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
                System.out.println();
            });
        }
    }
}
