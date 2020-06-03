// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * The USReceipt model.
 */
@Immutable
public final class USReceipt extends RecognizedReceipt {

    /**
     * List of recognized field items.
     */
    private final List<USReceiptItem> receiptItems;

    /**
     * Recognized receipt type information.
     */
    private final USReceiptType receiptType;

    /**
     * Recognized field merchant name.
     */
    private final FormField<String> merchantName;

    /**
     * Recognized field merchant address.
     */
    private final FormField<String> merchantAddress;

    /**
     * Recognized field merchant phone number.
     */
    private final FormField<String> merchantPhoneNumber;

    /**
     * Recognized field subtotal.
     */
    private final FormField<Float> subtotal;

    /**
     * Recognized field tax.
     */
    private final FormField<Float> tax;

    /**
     * Recognized field tip.
     */
    private final FormField<Float> tip;

    /**
     * Recognized field total.
     */
    private final FormField<Float> total;

    /**
     * Recognized field transaction date.
     */
    private final FormField<LocalDate> transactionDate;

    /**
     * Recognized field transaction time.
     */
    private final FormField<LocalTime> transactionTime;

    /**
     * Constructs a USReceipt object.
     *  @param receiptLocale The locale information for the recognized Receipt.
     * @param recognizedForm The recognized form.
     * @param receiptItems List of recognized field items.
     * @param receiptType Recognized receipt type information.
     * @param merchantName Recognized field merchant name.
     * @param merchantAddress Recognized field merchant address.
     * @param merchantPhoneNumber Recognized field merchant phone number.
     * @param subtotal Recognized field subtotal.
     * @param tax Recognized field tax.
     * @param tip Recognized field tip.
     * @param total Recognized field total.
     * @param transactionDate Recognized field transaction date.
     * @param transactionTime Recognized field transaction time.
     */
    public USReceipt(String receiptLocale, RecognizedForm recognizedForm,
        final List<USReceiptItem> receiptItems,
        final USReceiptType receiptType, final FormField<String> merchantName,
        final FormField<String> merchantAddress,
        final FormField<String> merchantPhoneNumber, final FormField<Float> subtotal,
        final FormField<Float> tax,
        final FormField<Float> tip, final FormField<Float> total,
        final FormField<LocalDate> transactionDate,
        final FormField<LocalTime> transactionTime) {
        super(receiptLocale, recognizedForm);
        this.receiptItems = receiptItems;
        this.receiptType = receiptType;
        this.merchantName = merchantName;
        this.merchantAddress = merchantAddress;
        this.merchantPhoneNumber = merchantPhoneNumber;
        this.subtotal = subtotal;
        this.tax = tax;
        this.tip = tip;
        this.total = total;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReceiptLocale() {
        return super.getReceiptLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecognizedForm getRecognizedForm() {
        return super.getRecognizedForm();
    }

    /**
     * Get the itemized fields in the Recognized Receipt.
     *
     * @return the list itemized fields.
     */
    public List<USReceiptItem> getReceiptItems() {
        return this.receiptItems;
    }

    /**
     * Get the type of Recognized Receipt.
     *
     * @return the type of Recognized Receipt.
     */
    public USReceiptType getReceiptType() {
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
}
