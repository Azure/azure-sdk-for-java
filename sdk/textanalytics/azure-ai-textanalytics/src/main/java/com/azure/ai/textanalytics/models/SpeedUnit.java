// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for SpeedUnit. */
public final class SpeedUnit extends ExpandableStringEnum<SpeedUnit> {
    /** Static value Unspecified for SpeedUnit. */
    public static final SpeedUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value MetersPerSecond for SpeedUnit. */
    public static final SpeedUnit METERS_PER_SECOND = fromString("MetersPerSecond");

    /** Static value KilometersPerHour for SpeedUnit. */
    public static final SpeedUnit KILOMETERS_PER_HOUR = fromString("KilometersPerHour");

    /** Static value KilometersPerMinute for SpeedUnit. */
    public static final SpeedUnit KILOMETERS_PER_MINUTE = fromString("KilometersPerMinute");

    /** Static value KilometersPerSecond for SpeedUnit. */
    public static final SpeedUnit KILOMETERS_PER_SECOND = fromString("KilometersPerSecond");

    /** Static value MilesPerHour for SpeedUnit. */
    public static final SpeedUnit MILES_PER_HOUR = fromString("MilesPerHour");

    /** Static value Knot for SpeedUnit. */
    public static final SpeedUnit KNOT = fromString("Knot");

    /** Static value FootPerSecond for SpeedUnit. */
    public static final SpeedUnit FOOT_PER_SECOND = fromString("FootPerSecond");

    /** Static value FootPerMinute for SpeedUnit. */
    public static final SpeedUnit FOOT_PER_MINUTE = fromString("FootPerMinute");

    /** Static value YardsPerMinute for SpeedUnit. */
    public static final SpeedUnit YARDS_PER_MINUTE = fromString("YardsPerMinute");

    /** Static value YardsPerSecond for SpeedUnit. */
    public static final SpeedUnit YARDS_PER_SECOND = fromString("YardsPerSecond");

    /** Static value MetersPerMillisecond for SpeedUnit. */
    public static final SpeedUnit METERS_PER_MILLISECOND = fromString("MetersPerMillisecond");

    /** Static value CentimetersPerMillisecond for SpeedUnit. */
    public static final SpeedUnit CENTIMETERS_PER_MILLISECOND = fromString("CentimetersPerMillisecond");

    /** Static value KilometersPerMillisecond for SpeedUnit. */
    public static final SpeedUnit KILOMETERS_PER_MILLISECOND = fromString("KilometersPerMillisecond");

    /**
     * Creates or finds a SpeedUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SpeedUnit.
     */
    public static SpeedUnit fromString(String name) {
        return fromString(name, SpeedUnit.class);
    }
}
