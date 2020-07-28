// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for LengthUnit.
 */
@Immutable
public final class LengthUnit extends ExpandableStringEnum<LengthUnit> {
    /**
     * Static value pixel for LengthUnit.
     */
    public static final LengthUnit PIXEL = fromString("pixel");

    /**
     * Static value inch for LengthUnit.
     */
    public static final LengthUnit INCH = fromString("inch");

    /**
     * Parses a serialized value to a {@code LengthUnit} instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed LengthUnit object, or null if unable to parse.
     */
    public static LengthUnit fromString(String value) {
        return fromString(value, LengthUnit.class);
    }
}
