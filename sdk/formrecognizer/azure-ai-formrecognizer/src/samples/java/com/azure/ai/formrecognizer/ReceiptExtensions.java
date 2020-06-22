// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizedForm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The ReceiptExtensions class to allow users to convert a {@link RecognizedForm}
 * to a US Receipt type.
 */
public final class ReceiptExtensions {

    private ReceiptExtensions() {
    }

    /**
     * Static method to convert an incoming recognized form to a {@link USReceipt type}.
     *
     * @param recognizedForm The {@link RecognizedForm recognized receipt}.
     *
     * @return The converted {@link USReceipt US locale receipt} type.
     */
    @SuppressWarnings("unchecked")
    public static USReceipt asUSReceipt(RecognizedForm recognizedForm) {
        USReceiptType receiptType = null;
        FormField<String> merchantName = null;
        FormField<String> merchantAddress = null;
        FormField<String> merchantPhoneNumber = null;
        FormField<Float> subtotal = null;
        FormField<Float> tax = null;
        FormField<Float> tip = null;
        FormField<Float> total = null;
        FormField<LocalDate> transactionDate = null;
        FormField<LocalTime> transactionTime = null;
        List<USReceiptItem> receiptItems = null;

        for (Map.Entry<String, FormField<?>> entry : recognizedForm.getFields().entrySet()) {
            String key = entry.getKey();
            FormField<?> fieldValue = entry.getValue();
            switch (key) {
                case "ReceiptType":
                    receiptType = new USReceiptType(((FormField<String>) fieldValue).getFieldValue(),
                        fieldValue.getConfidence());
                    break;
                case "MerchantName":
                    merchantName = (FormField<String>) fieldValue;
                    break;
                case "MerchantAddress":
                    merchantAddress = (FormField<String>) fieldValue;
                    break;
                case "MerchantPhoneNumber":
                    merchantPhoneNumber = (FormField<String>) fieldValue;
                    break;
                case "Subtotal":
                    subtotal = (FormField<Float>) fieldValue;
                    break;
                case "Tax":
                    tax = (FormField<Float>) fieldValue;
                    break;
                case "Tip":
                    tip = (FormField<Float>) fieldValue;
                    break;
                case "Total":
                    total = (FormField<Float>) fieldValue;
                    break;
                case "TransactionDate":
                    transactionDate = (FormField<LocalDate>) fieldValue;
                    break;
                case "TransactionTime":
                    transactionTime = (FormField<LocalTime>) fieldValue;
                    break;
                case "Items":
                    receiptItems = toReceiptItems(fieldValue);
                    break;
                default:
                    break;
            }
        }
        return new USReceipt(receiptItems, receiptType,
            merchantName, merchantAddress, merchantPhoneNumber, subtotal, tax, tip, total, transactionDate,
            transactionTime);
    }

    /**
     * Helper method to convert the service level
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray() value items}
     * to SDK level {@link USReceiptItem receipt items}.
     *
     * @param fieldValueItems The strongly typed field values.
     *
     * @return An unmodifiable list of {@link USReceiptItem}.
     */
    @SuppressWarnings("unchecked")
    private static List<USReceiptItem> toReceiptItems(FormField<?> fieldValueItems) {
        List<FormField<?>> fieldValueArray = (List<FormField<?>>) fieldValueItems.getFieldValue();
        List<USReceiptItem> receiptItemList = new ArrayList<>();

        for (FormField<?> eachFieldValue : fieldValueArray) {
            Map<String, FormField<?>> objectValue = ((Map<String, FormField<?>>) (eachFieldValue.getFieldValue()));
            FormField<String> name = null;
            FormField<Float> quantity = null;
            FormField<Float> price = null;
            FormField<Float> totalPrice = null;
            for (Map.Entry<String, FormField<?>> entry : objectValue.entrySet()) {
                String key = entry.getKey();
                if ("Quantity".equals(key)) {
                    quantity = (FormField<Float>) entry.getValue();
                } else if ("Name".equals(key)) {
                    name = (FormField<String>) entry.getValue();
                } else if ("Price".equals(key)) {
                    price = (FormField<Float>) entry.getValue();
                } else if ("Total Price".equals(key)) {
                    totalPrice = (FormField<Float>) entry.getValue();
                }
            }
            receiptItemList.add(new USReceiptItem(name, quantity, price, totalPrice));
        }
        return Collections.unmodifiableList(receiptItemList);
    }
}
