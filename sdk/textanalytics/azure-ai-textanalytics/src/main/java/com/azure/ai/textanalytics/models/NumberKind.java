// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for NumberKind. */
public final class NumberKind extends ExpandableStringEnum<NumberKind> {
    /** Static value Integer for NumberKind. */
    public static final NumberKind INTEGER = fromString("Integer");

    /** Static value Decimal for NumberKind. */
    public static final NumberKind DECIMAL = fromString("Decimal");

    /** Static value Power for NumberKind. */
    public static final NumberKind POWER = fromString("Power");

    /** Static value Fraction for NumberKind. */
    public static final NumberKind FRACTION = fromString("Fraction");

    /** Static value Percent for NumberKind. */
    public static final NumberKind PERCENT = fromString("Percent");

    /** Static value Unspecified for NumberKind. */
    public static final NumberKind UNSPECIFIED = fromString("Unspecified");

    /**
     * Creates or finds a NumberKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding NumberKind.
     */
    public static NumberKind fromString(String name) {
        return fromString(name, NumberKind.class);
    }
}
