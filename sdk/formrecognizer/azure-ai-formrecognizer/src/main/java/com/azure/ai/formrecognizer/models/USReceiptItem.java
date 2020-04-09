// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The USReceiptItem model.
 */
@Immutable
public class USReceiptItem {

    /*
     * Name of the field value.
     */
    private final FieldValue<?> name;

    /*
     * Quantity of the field value.
     */
    private final FieldValue<?> quantity;

    /*
     * Price of the field value.
     */
    private final FieldValue<?> price;

    /*
     * Total price of the field value.
     */
    private final FieldValue<?> totalPrice;

    /**
     * Constructs a USReceiptItem object.
     *
     * @param name Name of the field value.
     * @param quantity quantity of the field value.
     * @param price price of the field value.
     * @param totalPrice Total price of the field value.
     */
    public USReceiptItem(final FieldValue<?> name, final FieldValue<?> quantity, final FieldValue<?> price,
                         final FieldValue<?> totalPrice) {
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
    public FieldValue<?> getName() {
        return name;
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
     * Gets the price of the Receipt Item.
     *
     * @return The total Price.
     */
    public FieldValue<?> getPrice() {
        return price;
    }

    /**
     * Gets the total price of the Receipt Item.
     *
     * @return The total Price.
     */
    public FieldValue<?> getTotalPrice() {
        return totalPrice;
    }
}
