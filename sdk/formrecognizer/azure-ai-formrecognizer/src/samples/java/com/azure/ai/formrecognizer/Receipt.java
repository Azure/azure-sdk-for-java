// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldData;
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
 * Represents a receipt recognized from the input document and provides strongly-typed properties
 * for accessing common fields present in recognized receipts.
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
    private TypedFormField<String> merchantName;

    /**
     * Recognized field merchant address.
     */
    private TypedFormField<String> merchantAddress;

    /**
     * Recognized field merchant phone number.
     */
    private TypedFormField<String> merchantPhoneNumber;

    /**
     * Recognized field subtotal.
     */
    private TypedFormField<Double> subtotal;

    /**
     * Recognized field tax.
     */
    private TypedFormField<Double> tax;

    /**
     * Recognized field tip.
     */
    private TypedFormField<Double> tip;

    /**
     * Recognized field total.
     */
    private TypedFormField<Double> total;

    /**
     * Recognized field transaction date.
     */
    private TypedFormField<LocalDate> transactionDate;

    /**
     * Recognized field transaction time.
     */
    private TypedFormField<LocalTime> transactionTime;

    /**
     * Constructs a receipt object from the provided recognized form.
     * @param recognizedForm the recognized form object.
     */
    public Receipt(RecognizedForm recognizedForm) {
        for (Map.Entry<String, FormField> entry : recognizedForm.getFields().entrySet()) {
            String key = entry.getKey();
            FormField formField = entry.getValue();
            switch (key) {
                case "ReceiptType":
                    receiptType = new ReceiptType(formField.getValue().asString(), formField.getConfidence());
                    break;
                case "MerchantName":
                    merchantName = new TypedFormField<>(formField, String.class);
                    break;
                case "MerchantAddress":
                    merchantAddress = new TypedFormField<>(formField, String.class);
                    break;
                case "MerchantPhoneNumber":
                    merchantPhoneNumber = new TypedFormField<>(formField, String.class);
                    break;
                case "Subtotal":
                    subtotal = new TypedFormField<>(formField, Double.class);
                    break;
                case "Tax":
                    tax = new TypedFormField<>(formField, Double.class);
                    break;
                case "Tip":
                    tip = new TypedFormField<>(formField, Double.class);
                    break;
                case "Total":
                    total = new TypedFormField<>(formField, Double.class);
                    break;
                case "TransactionDate":
                    transactionDate = new TypedFormField<>(formField, LocalDate.class);
                    break;
                case "TransactionTime":
                    transactionTime = new TypedFormField<>(formField, LocalTime.class);
                    break;
                case "Items":
                    receiptItems = toReceiptItems(formField);
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
    public TypedFormField<String> getMerchantName() {
        return this.merchantName;
    }

    /**
     * Get the merchant address field.
     *
     * @return the merchantAddress value.
     */
    public TypedFormField<String> getMerchantAddress() {
        return this.merchantAddress;
    }

    /**
     * Get the merchant Phone number field.
     *
     * @return the merchantPhoneNumber value.
     */
    public TypedFormField<String> getMerchantPhoneNumber() {
        return this.merchantPhoneNumber;
    }

    /**
     * Get the subtotal field.
     *
     * @return the subtotal value.
     */
    public TypedFormField<Double> getSubtotal() {
        return this.subtotal;
    }

    /**
     * Get the tax field.
     *
     * @return the tax value.
     */
    public TypedFormField<Double> getTax() {
        return this.tax;
    }

    /**
     * Get the tip field.
     *
     * @return the tip value.
     */
    public TypedFormField<Double> getTip() {
        return this.tip;
    }

    /**
     * Get the Total field.
     *
     * @return the total value.
     */
    public TypedFormField<Double> getTotal() {
        return this.total;
    }

    /**
     * Get the Transaction date field.
     *
     * @return the transactionDate value.
     */
    public TypedFormField<LocalDate> getTransactionDate() {
        return this.transactionDate;
    }

    /**
     * Get the transaction time field.
     *
     * @return the transactionTime value.
     */
    public TypedFormField<LocalTime> getTransactionTime() {
        return this.transactionTime;
    }

    /**
     * Helper method to convert the recognized itemized data to {@link ReceiptItem receipt items}.
     *
     * @param fieldValueItems The recognized itemized receipt data.
     *
     * @return An unmodifiable list of {@link ReceiptItem}.
     */
    private static List<ReceiptItem> toReceiptItems(FormField fieldValueItems) {
        List<FormField> fieldValueArray = fieldValueItems.getValue().asList();
        List<ReceiptItem> receiptItemList = new ArrayList<>();

        for (FormField eachFieldValue : fieldValueArray) {
            Map<String, FormField> objectValue = eachFieldValue.getValue().asMap();
            TypedFormField<String> name = null;
            TypedFormField<Double> quantity = null;
            TypedFormField<Double> price = null;
            TypedFormField<Double> totalPrice = null;
            for (Map.Entry<String, FormField> entry : objectValue.entrySet()) {
                String key = entry.getKey();
                if ("Quantity".equals(key)) {
                    quantity = new TypedFormField<>(entry.getValue(), Double.class);
                } else if ("Name".equals(key)) {
                    name = new TypedFormField<>(entry.getValue(), String.class);
                } else if ("Price".equals(key)) {
                    price = new TypedFormField<>(entry.getValue(), Double.class);
                } else if ("Total Price".equals(key)) {
                    totalPrice = new TypedFormField<>(entry.getValue(), Double.class);
                }
            }
            receiptItemList.add(new ReceiptItem(name, quantity, price, totalPrice));
        }
        return Collections.unmodifiableList(receiptItemList);
    }

    /**
     * The strongly typed FormField representation model.
     *
     * @param <T> The type of value returned from the service call.
     */
    public static class TypedFormField<T> {
        private final FormField formField;
        private final Class<T> type;

        /**
         * Constructs a TypedFormField object.
         *
         * @param formField the SDK returned FormField object.
         * @param type The type of the field value returned from the service call.
         */
        public TypedFormField(FormField formField, Class<T> type) {
            this.formField = formField;
            this.type = type;
        }

        /**
         * Get the strongly typed value of the recognized field.
         *
         * @return the strongly typed value of the recognized field.
         * @throws IllegalStateException when a type mismatch occurs.
         */
        @SuppressWarnings("unchecked")
        public T getValue() {
            switch (formField.getValue().getValueType()) {
                case STRING:
                    if (type.isAssignableFrom(String.class)) {
                        return (T) formField.getValue().asString();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case DATE:
                    if (type.isAssignableFrom(LocalDate.class)) {
                        return (T) formField.getValue().asDate();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case TIME:
                    if (type.isAssignableFrom(LocalTime.class)) {
                        return (T) formField.getValue().asTime();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case PHONE_NUMBER:
                    if (type.isAssignableFrom(String.class)) {
                        return (T) formField.getValue().asPhoneNumber();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case FLOAT:
                    if (type.isAssignableFrom(Double.class)) {
                        return (T) formField.getValue().asFloat();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case LONG:
                    if (type.isAssignableFrom(Long.class)) {
                        return (T) formField.getValue().asLong();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case LIST:
                    if (type.isAssignableFrom(List.class)) {
                        return (T) formField.getValue().asList();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                case MAP:
                    if (type.isAssignableFrom(Map.class)) {
                        return (T) formField.getValue().asMap();
                    } else {
                        throw new IllegalStateException("Type mismatch error occurred.");
                    }
                default:
                    throw new IllegalStateException("Unexpected type value: " + formField.getValue().getValueType());
            }
        }

        /**
         * Get the estimated confidence value of the recognized field.
         *
         * @return the confidence value.
         */
        public float getConfidence() {
            return this.formField.getConfidence();
        }

        /**
         * Get the text, bounding box, and field elements for the field label.
         *
         * @return the text, bounding box, and field elements for the field value.
         */
        public FieldData getLabelData() {
            return this.formField.getLabelData();
        }

        /**
         * Get the name of the field in the provided document.
         *
         * @return the name of field or label.
         */
        public String getName() {
            return this.formField.getName();
        }

        /**
         * Get the text, bounding box, and field elements for the field value.
         *
         * @return the text, bounding box, and field elements for the field value.
         */
        public FieldData getValueData() {
            return this.formField.getValueData();
        }
    }

    /**
     * The ReceiptType model.
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
        private final TypedFormField<String> name;
        private final TypedFormField<Double> quantity;
        private final TypedFormField<Double> price;
        private final TypedFormField<Double> totalPrice;

        /**
         * Constructs a ReceiptItem object.
         *
         * @param name Name of the field value.
         * @param quantity quantity of the field value.
         * @param price price of the field value.
         * @param totalPrice Total price of the field value.
         */
        public ReceiptItem(final TypedFormField<String> name, final TypedFormField<Double> quantity,
            final TypedFormField<Double> price,
            final TypedFormField<Double> totalPrice) {
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
        public TypedFormField<String> getName() {
            return name;
        }

        /**
         * Gets the quantity of the Receipt Item.
         *
         * @return the quantity of Receipt Item.
         */
        public TypedFormField<Double> getQuantity() {
            return quantity;
        }

        /**
         * Gets the price of the Receipt Item.
         *
         * @return The total Price.
         */
        public TypedFormField<Double> getPrice() {
            return price;
        }

        /**
         * Gets the total price of the Receipt Item.
         *
         * @return The total Price.
         */
        public TypedFormField<Double> getTotalPrice() {
            return totalPrice;
        }
    }
}
