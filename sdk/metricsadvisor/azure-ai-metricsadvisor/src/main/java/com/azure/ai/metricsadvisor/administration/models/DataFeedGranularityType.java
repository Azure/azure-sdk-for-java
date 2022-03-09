// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The DataFeedGranularityType model
 **/
public final class DataFeedGranularityType extends ExpandableStringEnum<DataFeedGranularityType> {
    /**
     * Static value Yearly for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType YEARLY = fromString("Yearly");

    /**
     * Static value Monthly for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType MONTHLY = fromString("Monthly");

    /**
     * Static value Weekly for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType WEEKLY = fromString("Weekly");

    /**
     * Static value Daily for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType DAILY = fromString("Daily");

    /**
     * Static value Hourly for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType HOURLY = fromString("Hourly");

    /**
     * Static value Minutely for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType PER_MINUTE = fromString("Minutely");

    /**
     * Static value Custom for DataFeedGranularityType.
     */
    public static final DataFeedGranularityType CUSTOM = fromString("Custom");

    /**
     * Creates or finds a DataFeedGranularityType from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding DataFeedGranularityType.
     */
    public static DataFeedGranularityType fromString(String name) {
        return fromString(name, DataFeedGranularityType.class);
    }

    /**
     * @return known DataFeedGranularityType values.
     */
    public static Collection<DataFeedGranularityType> values() {
        return values(DataFeedGranularityType.class);
    }
}
