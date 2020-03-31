// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.util.List;
import java.util.Map;

/**
 * The ExtractedReceipt model.
 */
@Fluent
public class ExtractedReceipt {

    /**
     * Page information.
     */
    private final PageMetadata pageMetadata;

    /**
     * The first and last page of the input receipt.
     */
    private final PageRange pageRange;

    /**
     * List of extracted fields.
     */
    private List<ReceiptItem> receiptItems;

    /**
     * Extracted receipt type information.
     */
    private ReceiptType receiptType;

    /**
     * Extracted field merchant name.
     */
    private FieldValue<?> merchantName;

    /**
     * Extracted field merchant address.
     */
    private FieldValue<?> merchantAddress;

    /**
     * Extracted field merchant phone number.
     */
    private FieldValue<?> merchantPhoneNumber;

    /**
     * Extracted field subtotal.
     */
    private FieldValue<?> subtotal;

    /**
     * Extracted field tax.
     */
    private FieldValue<?> tax;

    /**
     * Extracted field tip.
     */
    private FieldValue<?> tip;

    /**
     * Extracted field total.
     */
    private FieldValue<?> total;

    /**
     * Extracted field transaction date.
     */
    private FieldValue<?> transactionDate;

    /**
     * Extracted field transaction time.
     */
    private FieldValue<?> transactionTime;

    /**
     * Map of Extracted fields.
     */
    private Map<String, FieldValue<?>> extractedFields;

    /**
     * Constructs an ExtractedReceipt.
     *
     * @param pageMetadata The Page info of the extracted receipt.
     * @param pageRange The page range of the extracted receipt
     */
    public ExtractedReceipt(final PageMetadata pageMetadata, PageRange pageRange) {
        this.pageMetadata = pageMetadata;
        this.pageRange = pageRange;
    }

    /**
     * Gets the page information.
     *
     * @return The page information.
     */
    public PageMetadata getPageMetadata() {
        return this.pageMetadata;
    }

    /**
     * Gets the page range information of the extracted receipt.
     *
     * @return The page range information of the extracted receipt.
     */
    public PageRange getPageRange() {
        return this.pageRange;
    }

    /**
     * Gets the list of extracted field values.
     *
     * @return The list of extracted field values.
     */
    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    /**
     * Sets the list of extracted field values.
     *
     * @param receiptItems The list of extracted field values.
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setReceiptItems(final List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
        return this;
    }

    /**
     * Gets the extracted receipt type.
     *
     * @return The extracted receipt type.
     */
    public ReceiptType getReceiptType() {
        return receiptType;
    }

    /**
     * Sets the extracted receipt type.
     *
     * @param receiptType The extracted receipt type.
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setReceiptType(final ReceiptType receiptType) {
        this.receiptType = receiptType;
        return this;
    }


    /**
     * Gets the extracted merchant name {@link FieldValue}
     *
     * @return The extracted merchant name {@link FieldValue}
     */
    public FieldValue<?> getMerchantName() {
        return merchantName;
    }

    /**
     * Sets the extracted merchant name {@link FieldValue}
     *
     * @param merchantName The extracted merchant name {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setMerchantName(final FieldValue<?> merchantName) {
        this.merchantName = merchantName;
        return this;
    }

    /**
     * Sets the extracted merchant address {@link FieldValue}
     *
     * @return The extracted merchant address {@link FieldValue}
     */
    public FieldValue<?> getMerchantAddress() {
        return merchantAddress;
    }

    /**
     * Sets the extracted merchant address {@link FieldValue}
     *
     * @param merchantAddress The extracted merchant address {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setMerchantAddress(final FieldValue<?> merchantAddress) {
        this.merchantAddress = merchantAddress;
        return this;
    }

    /**
     * Sets the extracted merchant phone number {@link FieldValue}
     *
     * @return The extracted merchant phone number {@link FieldValue}
     */
    public FieldValue<?> getMerchantPhoneNumber() {
        return merchantPhoneNumber;
    }

    /**
     * Sets the extracted merchant phone number {@link FieldValue}
     *
     * @param merchantPhoneNumber The extracted merchant phone number {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setMerchantPhoneNumber(final FieldValue<?> merchantPhoneNumber) {
        this.merchantPhoneNumber = merchantPhoneNumber;
        return this;
    }

    /**
     * Gets the extracted subtotal {@link FieldValue}
     *
     * @return The extracted subtotal {@link FieldValue}
     */
    public FieldValue<?> getSubtotal() {
        return subtotal;
    }

    /**
     * Sets the extracted subtotal {@link FieldValue}
     *
     * @param subtotal The extracted subtotal {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setSubtotal(final FieldValue<?> subtotal) {
        this.subtotal = subtotal;
        return this;
    }

    /**
     * Sets the extracted tax {@link FieldValue}
     *
     * @return The extracted tax {@link FieldValue}
     */
    public FieldValue<?> getTax() {
        return tax;
    }

    /**
     * Sets the extracted tax {@link FieldValue}
     *
     * @param tax The extracted tax {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setTax(final FieldValue<?> tax) {
        this.tax = tax;
        return this;
    }

    /**
     * Gets the extracted tip {@link FieldValue}
     *
     * @return The extracted tip {@link FieldValue}
     */
    public FieldValue<?> getTip() {
        return tip;
    }

    /**
     * Sets the extracted tip {@link FieldValue}
     *
     * @param tip The extracted tip {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setTip(final FieldValue<?> tip) {
        this.tip = tip;
        return this;
    }

    /**
     * Gets the extracted total {@link FieldValue}
     *
     * @return The extracted total {@link FieldValue}
     */
    public FieldValue<?> getTotal() {
        return total;
    }

    /**
     * Sets the extracted total {@link FieldValue}
     *
     * @param total The extracted total {@link FieldValue}
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setTotal(final FieldValue<?> total) {
        this.total = total;
        return this;
    }

    /**
     * Gets the extracted transaction Date.
     *
     * @return The extracted transaction Date.
     */
    public FieldValue<?> getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the extracted transaction Date.
     *
     * @param transactionDate The extracted transaction Date.
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setTransactionDate(final FieldValue<?> transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    /**
     * Gets the extracted transaction time.
     *
     * @return The extracted transaction time.
     */
    public FieldValue<?> getTransactionTime() {
        return transactionTime;
    }

    /**
     * Sets the extracted transaction time.
     *
     * @param transactionTime The extracted transaction time.
     *
     * @return The updated ExtractedReceipt object.
     */
    public ExtractedReceipt setTransactionTime(final FieldValue<?> transactionTime) {
        this.transactionTime = transactionTime;
        return this;
    }

    /**
     * Gets the map of extracted fields.
     *
     * @return The map of extracted fields.
     */
    public Map<String, FieldValue<?>> getExtractedFields() {
        return this.extractedFields;
    }

    /**
     * Sets the map of extracted fields on the receipt object.
     *
     * @param extractedFields The map of extracted fields of the receipt.
     *
     * @return The updated {@link ExtractedReceipt} object.
     */
    public ExtractedReceipt setExtractedFields(final Map<String, FieldValue<?>> extractedFields) {
        this.extractedFields = extractedFields;
        return this;
    }
}
