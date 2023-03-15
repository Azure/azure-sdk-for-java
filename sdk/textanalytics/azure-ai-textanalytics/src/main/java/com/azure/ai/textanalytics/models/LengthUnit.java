// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for LengthUnit. */
public final class LengthUnit extends ExpandableStringEnum<LengthUnit> {
    /** Static value Unspecified for LengthUnit. */
    public static final LengthUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Kilometer for LengthUnit. */
    public static final LengthUnit KILOMETER = fromString("Kilometer");

    /** Static value Hectometer for LengthUnit. */
    public static final LengthUnit HECTOMETER = fromString("Hectometer");

    /** Static value Decameter for LengthUnit. */
    public static final LengthUnit DECAMETER = fromString("Decameter");

    /** Static value Meter for LengthUnit. */
    public static final LengthUnit METER = fromString("Meter");

    /** Static value Decimeter for LengthUnit. */
    public static final LengthUnit DECIMETER = fromString("Decimeter");

    /** Static value Centimeter for LengthUnit. */
    public static final LengthUnit CENTIMETER = fromString("Centimeter");

    /** Static value Millimeter for LengthUnit. */
    public static final LengthUnit MILLIMETER = fromString("Millimeter");

    /** Static value Micrometer for LengthUnit. */
    public static final LengthUnit MICROMETER = fromString("Micrometer");

    /** Static value Nanometer for LengthUnit. */
    public static final LengthUnit NANOMETER = fromString("Nanometer");

    /** Static value Picometer for LengthUnit. */
    public static final LengthUnit PICOMETER = fromString("Picometer");

    /** Static value Mile for LengthUnit. */
    public static final LengthUnit MILE = fromString("Mile");

    /** Static value Yard for LengthUnit. */
    public static final LengthUnit YARD = fromString("Yard");

    /** Static value Inch for LengthUnit. */
    public static final LengthUnit INCH = fromString("Inch");

    /** Static value Foot for LengthUnit. */
    public static final LengthUnit FOOT = fromString("Foot");

    /** Static value LightYear for LengthUnit. */
    public static final LengthUnit LIGHT_YEAR = fromString("LightYear");

    /** Static value Pt for LengthUnit. */
    public static final LengthUnit PT = fromString("Pt");

    /**
     * Creates or finds a LengthUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding LengthUnit.
     */
    public static LengthUnit fromString(String name) {
        return fromString(name, LengthUnit.class);
    }
}
