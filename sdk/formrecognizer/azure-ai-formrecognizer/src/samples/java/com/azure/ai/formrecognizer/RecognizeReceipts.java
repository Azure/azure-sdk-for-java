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
 * Sample for recognizing commonly found US receipt fields from a local file input stream.
 * For a suggested approach to extracting information from receipts, see StronglyTypedRecognizedForm.java.
 * See fields found on a receipt here:
 * https://aka.ms/azsdk/python/formrecognizer/receiptfields
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
            client.beginRecognizeReceipts(targetStream, sourceFile.length());

        List<RecognizedForm> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedForm recognizedReceipt = receiptPageResults.get(i);
            Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
            FormField<?> merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField != null) {
                if (FieldValueType.STRING.equals(merchantNameField.getValueType())) {
                    String merchantName = FieldValueType.STRING.cast(merchantNameField);
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
            }

            FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
            if (merchantPhoneNumberField != null) {
                if (FieldValueType.PHONE_NUMBER.equals(merchantNameField.getValueType())) {
                    String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                    System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                        merchantAddress, merchantPhoneNumberField.getConfidence());
                }
            }

            FormField<?> merchantAddressField = recognizedFields.get("MerchantAddress");
            if (merchantAddressField != null) {
                if (FieldValueType.STRING.equals(merchantNameField.getValueType())) {
                    String merchantAddress = FieldValueType.STRING.cast(merchantAddressField);
                    System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                        merchantAddress, merchantAddressField.getConfidence());
                }
            }

            FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField != null) {
                if (FieldValueType.DATE.equals(transactionDateField.getValueType())) {
                    LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
            }

            FormField<?> receiptItemsField = recognizedFields.get("Items");
            if (receiptItemsField != null) {
                System.out.printf("Receipt Items: %n");
                if (FieldValueType.LIST.equals(receiptItemsField.getValueType())) {
                    List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                    receiptItems.forEach(receiptItem -> {
                        if (FieldValueType.MAP.equals(receiptItem.getValueType())) {
                            Map<String, FormField<?>> formFieldMap = FieldValueType.MAP.cast(receiptItem);
                            formFieldMap.forEach((key, formField) -> {
                                if ("Name".equals(key)) {
                                    if (FieldValueType.STRING.equals(formField.getValueType())) {
                                        String name = FieldValueType.STRING.cast(formField);
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                            name, formField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (FieldValueType.FLOAT.equals(formField.getValueType())) {
                                        Float quantity = FieldValueType.FLOAT.cast(formField);
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
                                    }
                                }
                                if ("Price".equals(key)) {
                                    if (FieldValueType.FLOAT.equals(formField.getValueType())) {
                                        Float price = FieldValueType.FLOAT.cast(formField);
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            price, formField.getConfidence());
                                    }
                                }
                                if ("TotalPrice".equals(key)) {
                                    if (FieldValueType.FLOAT.equals(formField.getValueType())) {
                                        Float totalPrice = FieldValueType.FLOAT.cast(formField);
                                        System.out.printf("Total Price: %f, confidence: %.2f%n",
                                            totalPrice, formField.getConfidence());
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
