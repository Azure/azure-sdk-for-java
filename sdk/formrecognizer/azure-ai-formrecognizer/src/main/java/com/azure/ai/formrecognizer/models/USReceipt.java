// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The USReceipt model.
 */
@Immutable
public class USReceipt extends RecognizedReceipt {

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
    private final FormField merchantName;

    /**
     * Recognized field merchant address.
     */
    private final FormField merchantAddress;

    /**
     * Recognized field merchant phone number.
     */
    private final FormField merchantPhoneNumber;

    /**
     * Recognized field subtotal.
     */
    private final FormField subtotal;

    /**
     * Recognized field tax.
     */
    private final FormField tax;

    /**
     * Recognized field tip.
     */
    private final FormField tip;

    /**
     * Recognized field total.
     */
    private final FormField total;

    /**
     * Recognized field transaction date.
     */
    private final FormField transactionDate;

    /**
     * Recognized field transaction time.
     */
    private final FormField transactionTime;

    /**
     * Constructs a USReceipt object.
     *
     * @param receiptLocale The locale information for the recognized Receipt.
     * @param recognizedForm The recognized form.
     * @param receiptItems List of recognized field items.
     * @param receiptType Recognized receipt type information.
     * @param merchantName Recognized field merchant name.
     * @param merchantAddress Recognized field merchant address.
     * @param merchantPhoneNumber Recognized field merchant phone number.
     * @param subtotal Recognized field subtotal.
     * @param tax Recognized field tac.
     * @param tip Recognized field tip.
     * @param total Recognized field total.
     * @param transactionDate Recognized field transaction date.
     * @param transactionTime Recognized field transaction time.
     */
    public USReceipt(String receiptLocale, RecognizedForm recognizedForm, final List<USReceiptItem> receiptItems,
                     final USReceiptType receiptType, final FormField merchantName, final FormField merchantAddress,
                     final FormField merchantPhoneNumber, final FormField subtotal, final FormField tax,
                     final FormField tip, final FormField total, final FormField transactionDate,
                     final FormField transactionTime) {
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
     * Get the receiptItems property: Receipt Items.
     *
     * @return the list of recognized receipt items.
     */
    public List<USReceiptItem> getReceiptItems() {
        return this.receiptItems;
    }

    /**
     * Get the receiptType property: Receipt type.
     *
     * @return the Receipt type value.
     */
    public USReceiptType getReceiptType() {
        return this.receiptType;
    }

    /**
     * Get the merchantName property: Merchant name field.
     *
     * @return the merchantName value.
     */
    public FormField getMerchantName() {
        return this.merchantName;
    }


    /**
     * Get the merchantAddress property: Merchant address field.
     *
     * @return the merchantAddress value.
     */
    public FormField getMerchantAddress() {
        return this.merchantAddress;
    }

    /**
     * Get the merchantPhoneNumber property: Merchant Phone number field.
     *
     * @return the merchantPhoneNumber value.
     */
    public FormField getMerchantPhoneNumber() {
        return this.merchantPhoneNumber;
    }

    /**
     * Get the subtotal property: The subtotal field.
     *
     * @return the subtotal value.
     */
    public FormField getSubtotal() {
        return this.subtotal;
    }

    /**
     * Get the tax property: The tax field.
     *
     * @return the tax value.
     */
    public FormField getTax() {
        return this.tax;
    }

    /**
     * Get the tip property: The tip field.
     *
     * @return the tip value.
     */
    public FormField getTip() {
        return this.tip;
    }

    /**
     * Get the total property: The Total field.
     *
     * @return the total value.
     */
    public FormField getTotal() {
        return this.total;
    }

    /**
     * Get the transactionDate property: The TransactionDate field.
     *
     * @return the transactionDate value.
     */
    public FormField getTransactionDate() {
        return this.transactionDate;
    }

    /**
     * Get the transactionTime property: The TransactionTime field.
     *
     * @return the transactionTime value.
     */
    public FormField getTransactionTime() {
        return this.transactionTime;
    }
}
