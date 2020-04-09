// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

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

    /**
     * Static method to convert an incoming receipt to a {@link USReceipt type}.
     *
     * @param receipt The {@link RecognizedReceipt recognized receipt}.
     *
     * @return The converted {@link USReceipt US locale receipt} type/
     */
    public static USReceipt asUSReceipt(RecognizedReceipt receipt) {
        // add receipt fields
        USReceiptType receiptType = null;
        FieldValue<?> merchantName = null;
        FieldValue<?> merchantAddress = null;
        FieldValue<?> merchantPhoneNumber = null;
        FieldValue<?> subtotal = null;
        FieldValue<?> tax = null;
        FieldValue<?> tip = null;
        FieldValue<?> total = null;
        FieldValue<?> transactionDate = null;
        FieldValue<?> transactionTime = null;
        List<USReceiptItem> receiptItems = null;

        for (Map.Entry<String, FieldValue<?>> entry : receipt.getRecognizedForm().getFields().entrySet()) {
            String key = entry.getKey();
            FieldValue<?> fieldValue = entry.getValue();
            switch (key) {
                case "ReceiptType":
                    receiptType = new USReceiptType(fieldValue.getText(), 0f);
                    // TODO: update confidence
                    break;
                case "MerchantName":
                    merchantName = fieldValue;
                    break;
                case "MerchantAddress":
                    merchantAddress = fieldValue;
                    break;
                case "MerchantPhoneNumber":
                    merchantPhoneNumber = fieldValue;
                    break;
                case "Subtotal":
                    subtotal = fieldValue;
                    break;
                case "Tax":
                    tax = fieldValue;
                    break;
                case "Tip":
                    tip = fieldValue;
                    break;
                case "Total":
                    total = fieldValue;
                    break;
                case "TransactionDate":
                    transactionDate = fieldValue;
                    break;
                case "TransactionTime":
                    transactionTime = fieldValue;
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
    private static List<USReceiptItem> toReceiptItems(
        FieldValue<?> fieldValueItems) {
        ArrayValue arrayValue = (ArrayValue) (fieldValueItems);
        List<USReceiptItem> receiptItemList = new ArrayList<>();
        FieldValue<?> name = null;
        FieldValue<?> quantity = null;
        FieldValue<?> price = null;
        FieldValue<?> totalPrice = null;
        USReceiptItem receiptItem = null;

        for (FieldValue<?> eachFieldValue : arrayValue.getValue()) {
            ObjectValue objectValue = ((ObjectValue) (eachFieldValue));
            for (Map.Entry<String, FieldValue<?>> entry : objectValue.getValue().entrySet()) {
                String key = entry.getKey();
                FieldValue<?> fieldValue = entry.getValue();
                if (QUANTITY.toString().equals(key)) {
                    quantity = fieldValue;
                } else if (NAME.toString().equals(key)) {
                    name = fieldValue;
                } else if (PRICE.toString().equals(key)) {
                    price = fieldValue;
                } else if (TOTAL_PRICE.toString().equals(key)) {
                    totalPrice = fieldValue;
                }
                receiptItem = new USReceiptItem(name, quantity, price, totalPrice);
            }
            receiptItemList.add(receiptItem);
        }
        return receiptItemList;
    }
}
