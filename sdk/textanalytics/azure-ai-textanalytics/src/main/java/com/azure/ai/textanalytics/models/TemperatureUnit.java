// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for TemperatureUnit. */
public final class TemperatureUnit extends ExpandableStringEnum<TemperatureUnit> {
    /** Static value Unspecified for TemperatureUnit. */
    public static final TemperatureUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Fahrenheit for TemperatureUnit. */
    public static final TemperatureUnit FAHRENHEIT = fromString("Fahrenheit");

    /** Static value Kelvin for TemperatureUnit. */
    public static final TemperatureUnit KELVIN = fromString("Kelvin");

    /** Static value Rankine for TemperatureUnit. */
    public static final TemperatureUnit RANKINE = fromString("Rankine");

    /** Static value Celsius for TemperatureUnit. */
    public static final TemperatureUnit CELSIUS = fromString("Celsius");

    /**
     * Creates or finds a TemperatureUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding TemperatureUnit.
     */
    public static TemperatureUnit fromString(String name) {
        return fromString(name, TemperatureUnit.class);
    }
}
