// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for the order in which recognized text lines are returned.
 */
public final class FormReadingOrder extends ExpandableStringEnum<FormReadingOrder> {

    /**
     * Static value BASIC for FormReadingOrder.
     * Set it to basic for the lines to be sorted top to bottom, left to right, although in certain cases
     * proximity is treated with higher priority.
     */
    public static final FormReadingOrder BASIC = fromString("basic");

    /**
     * Static value NATURAL for FormReadingOrder.
     * Set it to "natural" value for the algorithm to use positional information to keep nearby lines together.
     */
    public static final FormReadingOrder NATURAL = fromString("natural");

    /**
     * Parses a serialized value to a FormReadingOrder instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed FormReadingOrder object, or null if unable to parse.
     */
    public static FormReadingOrder fromString(String value) {
        return fromString(value, FormReadingOrder.class);
    }
}
