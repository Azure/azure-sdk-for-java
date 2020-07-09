// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sample for recognizing commonly found US receipt fields from a file source URL. For a suggested approach to
 * extracting information from receipts, see StronglyTypedRecognizedForm.java.
 * See fields found on a receipt here:
 * https://aka.ms/azsdk/python/formrecognizer/receiptfields
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
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer"
            + "/azure-ai-formrecognizer/src/samples/java/sample-forms/receipts/contoso-allinone.jpg";
        SyncPoller<OperationResult, List<RecognizedForm>> recognizeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        List<RecognizedForm> receiptPageResults = recognizeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedForm recognizedForm = receiptPageResults.get(i);
            Map<String, FormField<?>> recognizedFields = recognizedForm.getFields();
            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
            FormField<?> merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField != null) {
                if (FieldValueType.STRING == merchantNameField.getValueType()) {
                    String merchantName = FieldValueType.STRING.cast(merchantNameField);
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
            }

            FormField<?> merchantAddressField = recognizedFields.get("MerchantAddress");
            if (merchantAddressField != null) {
                if (FieldValueType.STRING == merchantAddressField.getValueType()) {
                    String merchantAddress = FieldValueType.STRING.cast(merchantAddressField);
                    System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                        merchantAddress, merchantAddressField.getConfidence());
                }
            }

            FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
            if (merchantPhoneNumberField != null) {
                if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValueType()) {
                    String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                    System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                        merchantAddress, merchantPhoneNumberField.getConfidence());
                }
            }

            FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField != null) {
                if (FieldValueType.DATE == transactionDateField.getValueType()) {
                    LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
            }

            FormField<?> receiptItemsField = recognizedFields.get("Items");
            if (receiptItemsField != null) {
                System.out.printf("Receipt Items: %n");
                if (FieldValueType.LIST == receiptItemsField.getValueType()) {
                    List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                    receiptItems.forEach(receiptItem -> {
                        if (FieldValueType.MAP == receiptItem.getValueType()) {
                            Map<String, FormField<?>> formFieldMap = FieldValueType.MAP.cast(receiptItem);
                            formFieldMap.forEach((key, formField) -> {
                                if ("Name".equals(key)) {
                                    if (FieldValueType.STRING == formField.getValueType()) {
                                        String name = FieldValueType.STRING.cast(formField);
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                            name, formField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (FieldValueType.DOUBLE == formField.getValueType()) {
                                        Float quantity = FieldValueType.DOUBLE.cast(formField);
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
                                    }
                                }
                                if ("Price".equals(key)) {
                                    if (FieldValueType.DOUBLE == formField.getValueType()) {
                                        Float price = FieldValueType.DOUBLE.cast(formField);
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            price, formField.getConfidence());
                                    }
                                }
                                if ("TotalPrice".equals(key)) {
                                    if (FieldValueType.DOUBLE == formField.getValueType()) {
                                        Float totalPrice = FieldValueType.DOUBLE.cast(formField);
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
