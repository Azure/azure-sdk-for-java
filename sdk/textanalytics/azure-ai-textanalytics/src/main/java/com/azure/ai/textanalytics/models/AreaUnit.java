// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/** Defines values for AreaUnit. */
public final class AreaUnit extends ExpandableStringEnum<AreaUnit> {
    /** Static value Unspecified for AreaUnit. */
    public static final AreaUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value SquareKilometer for AreaUnit. */
    public static final AreaUnit SQUARE_KILOMETER = fromString("SquareKilometer");

    /** Static value SquareHectometer for AreaUnit. */
    public static final AreaUnit SQUARE_HECTOMETER = fromString("SquareHectometer");

    /** Static value SquareDecameter for AreaUnit. */
    public static final AreaUnit SQUARE_DECAMETER = fromString("SquareDecameter");

    /** Static value SquareDecimeter for AreaUnit. */
    public static final AreaUnit SQUARE_DECIMETER = fromString("SquareDecimeter");

    /** Static value SquareMeter for AreaUnit. */
    public static final AreaUnit SQUARE_METER = fromString("SquareMeter");

    /** Static value SquareCentimeter for AreaUnit. */
    public static final AreaUnit SQUARE_CENTIMETER = fromString("SquareCentimeter");

    /** Static value SquareMillimeter for AreaUnit. */
    public static final AreaUnit SQUARE_MILLIMETER = fromString("SquareMillimeter");

    /** Static value SquareInch for AreaUnit. */
    public static final AreaUnit SQUARE_INCH = fromString("SquareInch");

    /** Static value SquareFoot for AreaUnit. */
    public static final AreaUnit SQUARE_FOOT = fromString("SquareFoot");

    /** Static value SquareMile for AreaUnit. */
    public static final AreaUnit SQUARE_MILE = fromString("SquareMile");

    /** Static value SquareYard for AreaUnit. */
    public static final AreaUnit SQUARE_YARD = fromString("SquareYard");

    /** Static value Acre for AreaUnit. */
    public static final AreaUnit ACRE = fromString("Acre");

    /**
     * Creates or finds a AreaUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AreaUnit.
     */
    @JsonCreator
    public static AreaUnit fromString(String name) {
        return fromString(name, AreaUnit.class);
    }
}
