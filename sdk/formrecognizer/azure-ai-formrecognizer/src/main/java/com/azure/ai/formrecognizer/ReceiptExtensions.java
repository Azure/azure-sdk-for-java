// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.ai.formrecognizer.models.USReceiptItem;
import com.azure.ai.formrecognizer.models.USReceiptType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.models.ReceiptItemType.NAME;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.PRICE;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.QUANTITY;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.TOTAL_PRICE;

/**
 * The ReceiptExtensions class to allow users to convert a {@link RecognizedReceipt}
 * to a receiptLocale specific Receipt type.
 */
public final class ReceiptExtensions {

    private ReceiptExtensions() {
    }

    /**
     * Static method to convert an incoming receipt to a {@link USReceipt type}.
     *
     * @param receipt The {@link RecognizedReceipt recognized receipt}.
     *
     * @return The converted {@link USReceipt US locale receipt} type.
     */
    @SuppressWarnings("unchecked")
    public static USReceipt asUSReceipt(RecognizedReceipt receipt) {
        // add receipt fields
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

        for (Map.Entry<String, FormField<?>> entry : receipt.getRecognizedForm().getFields().entrySet()) {
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
        return new USReceipt(receipt.getReceiptLocale(), receipt.getRecognizedForm(), receiptItems, receiptType,
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
     * @return A list of {@link USReceiptItem}.
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
                if (QUANTITY.toString().equals(key)) {
                    quantity = (FormField<Float>) entry.getValue();
                } else if (NAME.toString().equals(key)) {
                    name = (FormField<String>) entry.getValue();
                } else if (PRICE.toString().equals(key)) {
                    price = (FormField<Float>) entry.getValue();
                } else if (TOTAL_PRICE.toString().equals(key)) {
                    totalPrice = (FormField<Float>) entry.getValue();
                }
            }
            receiptItemList.add(new USReceiptItem(name, quantity, price, totalPrice));
        }
        return receiptItemList;
    }
}
