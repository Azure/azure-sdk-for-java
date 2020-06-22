// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormField;
import com.azure.core.annotation.Immutable;

/**
 * The USReceiptItem model.
 */
@Immutable
public final class USReceiptItem {

    /*
     * Name of the field value.
     */
    private final FormField<String> name;

    /*
     * Quantity of the field value.
     */
    private final FormField<Float> quantity;

    /*
     * Price of the field value.
     */
    private final FormField<Float> price;

    /*
     * Total price of the field value.
     */
    private final FormField<Float> totalPrice;

    /**
     * Constructs a USReceiptItem object.
     * @param name Name of the field value.
     * @param quantity quantity of the field value.
     * @param price price of the field value.
     * @param totalPrice Total price of the field value.
     */
    public USReceiptItem(final FormField<String> name, final FormField<Float> quantity, final FormField<Float> price,
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
