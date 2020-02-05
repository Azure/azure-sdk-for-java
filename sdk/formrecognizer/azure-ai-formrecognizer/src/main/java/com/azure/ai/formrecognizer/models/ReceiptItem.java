// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public class ReceiptItem {
    public FieldValue name;
    public FieldValue quantity;
    public FieldValue totalPrice;

    public FieldValue getName() {
        return name;
    }

    public ReceiptItem setName(final FieldValue name) {
        this.name = name;
        return this;
    }

    public FieldValue getQuantity() {
        return quantity;
    }

    public ReceiptItem setQuantity(final FieldValue quantity) {
        this.quantity = quantity;
        return this;
    }

    public FieldValue getTotalPrice() {
        return totalPrice;
    }

    public ReceiptItem setTotalPrice(final FieldValue totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }
}
