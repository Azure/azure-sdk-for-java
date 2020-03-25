// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

@Fluent
public class ReceiptPageResult {
    private List<Element> elements;
    private List<ReceiptItem> receiptItems;
    private PageInfo pageInfo;
    private FieldValue<?> merchantName;
    private FieldValue<?> merchantAddress;
    private FieldValue<?> merchantPhoneNumber;
    private ReceiptType receiptType;
    private FieldValue<?> subtotal;
    private FieldValue<?> tax;
    private FieldValue<?> tip;
    private FieldValue<?> total;
    private FieldValue<?> transactionDate;
    private FieldValue<?> transactionTime;

    public ReceiptPageResult(final PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<Element> getElements() {
        return elements;
    }

    public ReceiptPageResult setElements(final List<Element> elements) {
        this.elements = elements;
        return this;
    }

    public List<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public ReceiptPageResult setReceiptItems(final List<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
        return this;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public ReceiptPageResult setReceiptType(final ReceiptType receiptType) {
        this.receiptType = receiptType;
        return this;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public ReceiptPageResult setPageInfo(final PageInfo pageInfo) {
        this.pageInfo = pageInfo;
        return this;
    }

    public FieldValue<?> getMerchantName() {
        return merchantName;
    }

    public ReceiptPageResult setMerchantName(final FieldValue<?> merchantName) {
        this.merchantName = merchantName;
        return this;
    }

    public FieldValue<?> getMerchantAddress() {
        return merchantAddress;
    }

    public ReceiptPageResult setMerchantAddress(final FieldValue<?> merchantAddress) {
        this.merchantAddress = merchantAddress;
        return this;
    }

    public FieldValue<?> getMerchantPhoneNumber() {
        return merchantPhoneNumber;
    }

    public ReceiptPageResult setMerchantPhoneNumber(final FieldValue<?> merchantPhoneNumber) {
        this.merchantPhoneNumber = merchantPhoneNumber;
        return this;
    }

    public FieldValue<?> getSubtotal() {
        return subtotal;
    }

    public ReceiptPageResult setSubtotal(final FieldValue<?> subtotal) {
        this.subtotal = subtotal;
        return this;
    }

    public FieldValue<?> getTax() {
        return tax;
    }

    public ReceiptPageResult setTax(final FieldValue<?> tax) {
        this.tax = tax;
        return this;
    }

    public FieldValue<?> getTip() {
        return tip;
    }

    public ReceiptPageResult setTip(final FieldValue<?> tip) {
        this.tip = tip;
        return this;
    }

    public FieldValue<?> getTotal() {
        return total;
    }

    public ReceiptPageResult setTotal(final FieldValue<?> total) {
        this.total = total;
        return this;
    }

    public FieldValue<?> getTransactionDate() {
        return transactionDate;
    }

    public ReceiptPageResult setTransactionDate(final FieldValue<?> transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public FieldValue<?> getTransactionTime() {
        return transactionTime;
    }

    public ReceiptPageResult setTransactionTime(final FieldValue<?> transactionTime) {
        this.transactionTime = transactionTime;
        return this;
    }
}
