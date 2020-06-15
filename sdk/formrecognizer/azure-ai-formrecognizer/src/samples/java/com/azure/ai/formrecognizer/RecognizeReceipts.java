// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Sample for recognizing US receipt information using file source URL.
 */
public class RecognizeReceipts {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     *
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

        SyncPoller<OperationResult, List<RecognizedReceipt>> analyzeReceiptPoller =
            client.beginRecognizeReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG);

        List<RecognizedReceipt> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedReceipt recognizedReceipt = receiptPageResults.get(i);
            Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
            FormField merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField != null) {
                if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantNameField.getFieldValue().asString(),
                        merchantNameField.getConfidence());
                }
            }
            FormField merchantAddressField = recognizedFields.get("MerchantAddress");
            if (merchantAddressField != null) {
                if (merchantAddressField.getFieldValue().getType() == FieldValueType.STRING) {
                    System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                        merchantAddressField.getFieldValue().asString(),
                        merchantAddressField.getConfidence());
                }
            }
            FormField transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField != null) {
                if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDateField.getFieldValue().asDate(),
                        transactionDateField.getConfidence());
                }
            }
            FormField receiptItemsField = recognizedFields.get("Items");
            if (receiptItemsField != null) {
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                    List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                            receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                                if (key.equals("Name")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.STRING) {
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                            formField.getFieldValue().asString(),
                                            formField.getConfidence());
                                    }
                                }
                                if (key.equals("Quantity")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            formField.getFieldValue().asInteger(), formField.getConfidence());
                                    }
                                }
                                if (key.equals("Price")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.FLOAT) {
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            formField.getFieldValue().asFloat(),
                                            formField.getConfidence());
                                    }
                                }
                                if (key.equals("TotalPrice")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.FLOAT) {
                                        System.out.printf("Total Price: %f, confidence: %.2f%n",
                                            formField.getFieldValue().asFloat(),
                                            formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            }
            System.out.print("-----------------------------------");
        }
    }
}
