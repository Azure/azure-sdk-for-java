// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

/**
 * Parameters for the predefined filters.
 */
public final class FilterParameters {

    private FilterParameters() {

    }

    /**
     * Percentage value of the returning true in the Percentage filter.
     */
    public static final String PERCENTAGE_FILTER_SETTING = "Value";

    /**
     * Property for the start of the Time Window Filter
     */
    public static final String TIME_WINDOW_FILTER_SETTING_START = "Start";

    /**
     * Property for the end of the Time Window Filter
     */
    public static final String TIME_WINDOW_FILTER_SETTING_END = "End";

    /**
     * Add-on recurrence rule allows the time window defined by Start and End to recur.
     * The rule specifies both how often the time window repeats and for how long.
     */
    public static final String TIME_WINDOW_FILTER_SETTING_RECURRENCE = "Recurrence";

}
