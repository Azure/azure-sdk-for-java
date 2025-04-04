// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.costmanagement.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The time frame for pulling data for the forecast.
 */
public final class ForecastTimeframe extends ExpandableStringEnum<ForecastTimeframe> {
    /**
     * Static value Custom for ForecastTimeframe.
     */
    public static final ForecastTimeframe CUSTOM = fromString("Custom");

    /**
     * Creates a new instance of ForecastTimeframe value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ForecastTimeframe() {
    }

    /**
     * Creates or finds a ForecastTimeframe from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding ForecastTimeframe.
     */
    public static ForecastTimeframe fromString(String name) {
        return fromString(name, ForecastTimeframe.class);
    }

    /**
     * Gets known ForecastTimeframe values.
     * 
     * @return known ForecastTimeframe values.
     */
    public static Collection<ForecastTimeframe> values() {
        return values(ForecastTimeframe.class);
    }
}
