// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.annotation.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Receipt model.
 */
public final class Receipt {

    /**
     * List of recognized field items.
     */
    private List<ReceiptItem> receiptItems;

    /**
     * Recognized receipt type information.
     */
    private ReceiptType receiptType;

    /**
     * Recognized field merchant name.
     */
    private FormField<String> merchantName;

    /**
     * Recognized field merchant address.
     */
    private FormField<String> merchantAddress;

    /**
     * Recognized field merchant phone number.
     */
    private FormField<String> merchantPhoneNumber;

    /**
     * Recognized field subtotal.
     */
    private FormField<Float> subtotal;

    /**
     * Recognized field tax.
     */
    private FormField<Float> tax;

    /**
     * Recognized field tip.
     */
    private FormField<Float> tip;

    /**
     * Recognized field total.
     */
    private FormField<Float> total;

    /**
     * Recognized field transaction date.
     */
    private FormField<LocalDate> transactionDate;

    /**
     * Recognized field transaction time.
     */
    private FormField<LocalTime> transactionTime;

    @SuppressWarnings("unchecked")
    public Receipt(RecognizedForm recognizedForm) {
        for (Map.Entry<String, FormField<?>> entry : recognizedForm.getFields().entrySet()) {
            String key = entry.getKey();
            FormField<?> fieldValue = entry.getValue();
            switch (key) {
                case "ReceiptType":
                    receiptType = new ReceiptType(((FormField<String>) fieldValue).getValue(),
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
                    receiptItems = Collections.unmodifiableList(toReceiptItems(fieldValue));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get the itemized fields in the Recognized Receipt.
     *
     * @return the unmodifiable list of itemized fields on the receipt.
     */
    public List<ReceiptItem> getReceiptItems() {
        return this.receiptItems;
    }

    /**
     * Get the type of Recognized Receipt.
     *
     * @return the type of Recognized Receipt.
     */
    public ReceiptType getReceiptType() {
        return this.receiptType;
    }

    /**
     * Get the merchant name field.
     *
     * @return the merchantName value.
     */
    public FormField<String> getMerchantName() {
        return this.merchantName;
    }

    /**
     * Get the merchant address field.
     *
     * @return the merchantAddress value.
     */
    public FormField<String> getMerchantAddress() {
        return this.merchantAddress;
    }

    /**
     * Get the merchant Phone number field.
     *
     * @return the merchantPhoneNumber value.
     */
    public FormField<String> getMerchantPhoneNumber() {
        return this.merchantPhoneNumber;
    }

    /**
     * Get the subtotal field.
     *
     * @return the subtotal value.
     */
    public FormField<Float> getSubtotal() {
        return this.subtotal;
    }

    /**
     * Get the tax field.
     *
     * @return the tax value.
     */
    public FormField<Float> getTax() {
        return this.tax;
    }

    /**
     * Get the tip field.
     *
     * @return the tip value.
     */
    public FormField<Float> getTip() {
        return this.tip;
    }

    /**
     * Get the Total field.
     *
     * @return the total value.
     */
    public FormField<Float> getTotal() {
        return this.total;
    }

    /**
     * Get the Transaction date field.
     *
     * @return the transactionDate value.
     */
    public FormField<LocalDate> getTransactionDate() {
        return this.transactionDate;
    }

    /**
     * Get the transaction time field.
     *
     * @return the transactionTime value.
     */
    public FormField<LocalTime> getTransactionTime() {
        return this.transactionTime;
    }

    /**
     * Helper method to convert the service level
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray() value items}
     * to SDK level {@link ReceiptItem receipt items}.
     *
     * @param fieldValueItems The strongly typed field values.
     * b
     *
     * @return An unmodifiable list of {@link ReceiptItem}.
     */
    @SuppressWarnings("unchecked")
    private static List<ReceiptItem> toReceiptItems(FormField<?> fieldValueItems) {
        List<FormField<?>> fieldValueArray = (List<FormField<?>>) fieldValueItems.getValue();
        List<ReceiptItem> receiptItemList = new ArrayList<>();

        for (FormField<?> eachFieldValue : fieldValueArray) {
            Map<String, FormField<?>> objectValue = ((Map<String, FormField<?>>) (eachFieldValue.getValue()));
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
            receiptItemList.add(new ReceiptItem(name, quantity, price, totalPrice));
        }
        return Collections.unmodifiableList(receiptItemList);
    }

    /**
     * The USReceiptType model.
     */
    @Immutable
    public static final class ReceiptType {
        private final String type;
        private final float confidence;

        /**
         * Constructs a Receipt Type.
         *
         * @param type The type of the receipt.
         * @param confidence The confidence score.
         */
        public ReceiptType(final String type, final float confidence) {
            this.type = type;
            this.confidence = confidence;
        }

        /**
         * Gets the type of the receipt.
         *
         * @return The type of the receipt.
         */
        public String getType() {
            return this.type;
        }

        /**
         * Gets the confidence score of the detected type of the receipt.
         *
         * @return The confidence score of the detected type of the receipt.
         */
        public float getConfidence() {
            return this.confidence;
        }
    }

    /**
     * The ReceiptItem model.
     */
    @Immutable
    public static final class ReceiptItem {
        private final FormField<String> name;
        private final FormField<Float> quantity;
        private final FormField<Float> price;
        private final FormField<Float> totalPrice;

        /**
         * Constructs a ReceiptItem object.
         *
         * @param name Name of the field value.
         * @param quantity quantity of the field value.
         * @param price price of the field value.
         * @param totalPrice Total price of the field value.
         */
        public ReceiptItem(final FormField<String> name, final FormField<Float> quantity, final FormField<Float> price,
            final FormField<Float> totalPrice) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.totalPrice = totalPrice;
        }

        /**
         * Gets the name of the field value.
         *
         * @return The name of the field value.
         */
        public FormField<String> getName() {
            return name;
        }

        /**
         * Gets the quantity of the Receipt Item.
         *
         * @return the quantity of Receipt Item.
         */
        public FormField<Float> getQuantity() {
            return quantity;
        }

        /**
         * Gets the price of the Receipt Item.
         *
         * @return The total Price.
         */
        public FormField<Float> getPrice() {
            return price;
        }

        /**
         * Gets the total price of the Receipt Item.
         *
         * @return The total Price.
         */
        public FormField<Float> getTotalPrice() {
            return totalPrice;
        }
    }
}
