// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

/**
 * The USReceiptItem model.
 */
@Fluent
public class USReceiptItem {

    /*
     * Name of the field value.
     */
    private FieldValue<?> name;

    /*
     * Quantity of the field value.
     */
    private FieldValue<?> quantity;

    /*
     * Price of the field value.
     */
    private FieldValue<?> price;

    /*
     * Total price of the field value.
     */
    private FieldValue<?> totalPrice;

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
     * Sets the name of the field value.
     *
     * @param name The name of the field value.
     *
     * @return The updated USReceiptItem object.
     */
    public USReceiptItem setName(final FieldValue<?> name) {
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
     * @return The updated USReceiptItem object.
     */
    public USReceiptItem setQuantity(final FieldValue<?> quantity) {
        this.quantity = quantity;
        return this;
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
     * Sets the total price of the Receipt Item.
     *
     * @param price the price of Receipt Item.
     *
     * @return The updated USReceiptItem object.
     */
    public USReceiptItem setPrice(final FieldValue<?> price) {
        this.price = price;
        return this;
    }

    /**
     * Gets the total price of the Receipt Item.
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
     * @return The updated USReceiptItem object.
     */
    public USReceiptItem setTotalPrice(final FieldValue<?> totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }
}
