// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

/**
 * The ReceiptItem model.
 */
@Fluent
public class ReceiptItem {

    /*
     * Name of the field value.
     */
    public FieldValue<?> name;

    /*
     * Quantity of the field value.
     */
    public FieldValue<?> quantity;

    /*
     * Total price of the field value.
     */
    public FieldValue<?> totalPrice;

    /**
     * Gets the name of the field value.
     *
     * @return The name of the field value.
     */
    public FieldValue<?> getName() {
        return name;
    }

    /**
     * Sets the name of the field value.
     *
     * @param name The name of the field value.
     *
     * @return The updated ReceiptItem object.
     */
    public ReceiptItem setName(final FieldValue<?> name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the quantity of the Receipt Item.
     *
     * @return the quantity of Receipt Item.
     */
    public FieldValue<?> getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the Receipt Item.
     *
     * @param quantity the quantity of the Receipt Item.
     *
     * @return The updated ReceiptItem object.
     */
    public ReceiptItem setQuantity(final FieldValue<?> quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Gets the total price of the Recipt Item.
     *
     * @return The total Price.
     */
    public FieldValue<?> getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets the total price of the Receipt Item.
     *
     * @param totalPrice the Total price of Receipt Item.
     *
     * @return The updated ReceiptItem object.
     */
    public ReceiptItem setTotalPrice(final FieldValue<?> totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }
}
