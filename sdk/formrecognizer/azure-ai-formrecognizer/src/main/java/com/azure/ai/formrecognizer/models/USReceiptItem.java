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
    private final StringValue name;

    /*
     * Quantity of the field value.
     */
    private final FloatValue quantity;

    /*
     * Price of the field value.
     */
    private final FloatValue price;

    /*
     * Total price of the field value.
     */
    private final FloatValue totalPrice;

    /**
     * Constructs a USReceiptItem object.
     * @param name Name of the field value.
     * @param quantity quantity of the field value.
     * @param price price of the field value.
     * @param totalPrice Total price of the field value.
     */
    public USReceiptItem(final StringValue name, final FloatValue quantity, final FloatValue price,
                         final FloatValue totalPrice) {
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
    public StringValue getName() {
        return name;
    }

    /**
     * Gets the quantity of the Receipt Item.
     *
     * @return the quantity of Receipt Item.
     */
    public FloatValue getQuantity() {
        return quantity;
    }

    /**
     * Gets the price of the Receipt Item.
     *
     * @return The total Price.
     */
    public FloatValue getPrice() {
        return price;
    }

    /**
     * Gets the total price of the Receipt Item.
     *
     * @return The total Price.
     */
    public FloatValue getTotalPrice() {
        return totalPrice;
    }
}
