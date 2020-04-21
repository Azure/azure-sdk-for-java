// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for ReceiptItemType.
 */
public final class ReceiptItemType extends ExpandableStringEnum<ReceiptItemType> {

    /**
     * Static value Name for ReceiptItemType.
     */
    public static final ReceiptItemType NAME = fromString("Name");

    /**
     * Static value Quantity for ReceiptItemType.
     */
    public static final ReceiptItemType QUANTITY = fromString("Quantity");

    /**
     * Static value Price for ReceiptItemType.
     */
    public static final ReceiptItemType PRICE = fromString("Price");

    /**
     * Static value TotalPrice for ReceiptItemType.
     */
    public static final ReceiptItemType TOTAL_PRICE = fromString("TotalPrice");

    /**
     * Parses a serialized value to a {@code ReceiptItemType} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed ReceiptItemType object, or null if unable to parse.
     */
    public static ReceiptItemType fromString(String value) {
        return fromString(value, ReceiptItemType.class);
    }

    /**
     * @return known {@link ReceiptItemType} values.
     */
    public static Collection<ReceiptItemType> values() {
        return values(ReceiptItemType.class);
    }
}
