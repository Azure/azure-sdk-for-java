// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for ReceiptItemKey.
 */
public final class ReceiptItemKey extends ExpandableStringEnum<ReceiptItemKey> {
    /**
     * Static value Name for ReceiptItemKey.
     */
    public static final ReceiptItemKey NAME = fromString("Name");

    /**
     * Static value Quantity for ReceiptItemKey.
     */
    public static final ReceiptItemKey QUANTITY = fromString("Quantity");

    /**
     * Static value Price for ReceiptItemKey.
     */
    public static final ReceiptItemKey PRICE = fromString("Price");

    /**
     * Static value TotalPrice for ReceiptItemKey.
     */
    public static final ReceiptItemKey TOTAL_PRICE = fromString("TotalPrice");

    /**
     * Parses a serialized value to a {@code ReceiptItemKey} instance.
     *
     * @param value the serialized value to parse.
     *
     * @return the parsed ReceiptItemKey object, or null if unable to parse.
     */
    @JsonCreator
    public static ReceiptItemKey fromString(String value) {
        return fromString(value, ReceiptItemKey.class);
    }

    /**
     * @return known {@link ReceiptItemKey} values.
     */
    public static Collection<ReceiptItemKey> values() {
        return values(ReceiptItemKey.class);
    }
}
