// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for DimensionUnit.
 */
public final class DimensionUnit extends ExpandableStringEnum<DimensionUnit> {
    /**
     * Static value pixel for DimensionUnit.
     */
    public static final DimensionUnit PIXEL = fromString("pixel");

    /**
     * Static value inch for DimensionUnit.
     */
    public static final DimensionUnit INCH = fromString("inch");

    /**
     * Parses a serialized value to a {@code DimensionUnit} instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DimensionUnit object, or null if unable to parse.
     */
    public static DimensionUnit fromString(String value) {
        return fromString(value, DimensionUnit.class);
    }

    /**
     * @return known {@link DimensionUnit} values.
     */
    public static Collection<DimensionUnit> values() {
        return values(DimensionUnit.class);
    }
}
