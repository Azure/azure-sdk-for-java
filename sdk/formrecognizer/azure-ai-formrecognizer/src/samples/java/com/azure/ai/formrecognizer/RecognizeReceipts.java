// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
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
    @SuppressWarnings("unchecked")
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
            client.beginRecognizeReceipts(targetStream, sourceFile.length());

        List<RecognizedForm> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedForm recognizedReceipt = receiptPageResults.get(i);
            Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
            FormField<?> merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField != null) {
                Object merchantNameFieldValue = merchantNameField.getFieldValue();
                if (merchantNameFieldValue instanceof String) {
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantNameFieldValue, merchantNameField.getConfidence());
                }
            }

            FormField<?> merchantAddressField = recognizedFields.get("MerchantAddress");
            if (merchantAddressField != null) {
                Object merchantAddressFieldValue = merchantAddressField.getFieldValue();
                if (merchantAddressFieldValue instanceof String) {
                    System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                        merchantAddressFieldValue, merchantAddressField.getConfidence());
                }
            }

            FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField != null) {
                Object transactionDateFieldValue = transactionDateField.getFieldValue();
                if (transactionDateFieldValue instanceof LocalDate) {
                    LocalDate transactionDate = (LocalDate) transactionDateFieldValue;
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
            }

            FormField<?> receiptItemsField = recognizedFields.get("Items");
            if (receiptItemsField != null) {
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getFieldValue() instanceof List) {
                    List<FormField<?>> receiptItems = (List<FormField<?>>) receiptItemsField.getFieldValue();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getFieldValue() instanceof Map) {
                            ((Map<String, FormField<?>>) receiptItem.getFieldValue()).forEach((key, formField) -> {
                                if ("Name".equals(key)) {
                                    if (formField.getFieldValue() instanceof String) {
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                            formField.getFieldValue(),
                                            formField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (formField.getFieldValue() instanceof Integer) {
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            formField.getFieldValue(), formField.getConfidence());
                                    }
                                }
                                if ("Price".equals(key)) {
                                    if (formField.getFieldValue() instanceof Float) {
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            formField.getFieldValue(),
                                            formField.getConfidence());
                                    }
                                }
                                if ("TotalPrice".equals(key)) {
                                    if (formField.getFieldValue() instanceof Float) {
                                        System.out.printf("Total Price: %f, confidence: %.2f%n",
                                            formField.getFieldValue(),
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
