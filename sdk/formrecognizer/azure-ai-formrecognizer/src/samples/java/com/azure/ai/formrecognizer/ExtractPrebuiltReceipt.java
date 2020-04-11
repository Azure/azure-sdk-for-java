// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptExtensions;
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
import java.time.Duration;

/**
 * Sample for extracting receipt information using input stream.
 */
public class ExtractPrebuiltReceipt {

    /**
     * Sample for extracting receipt information using input stream.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("azure-ai-formrecognizer/src/test/resources/sample-files/contoso-allinone.jpg");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginExtractReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG, true,
                Duration.ofSeconds(5));

        IterableStream<RecognizedReceipt> receiptPageResults = analyzeReceiptPoller.getFinalResult();

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
