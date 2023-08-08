// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for DataFeedMissingDataPointFillType. */
public final class DataFeedMissingDataPointFillType extends ExpandableStringEnum<DataFeedMissingDataPointFillType> {
    /** Static value SmartFilling for DataFeedMissingDataPointFillType. */
    public static final DataFeedMissingDataPointFillType SMART_FILLING = fromString("SmartFilling");

    /** Static value PreviousValue for DataFeedMissingDataPointFillType. */
    public static final DataFeedMissingDataPointFillType PREVIOUS_VALUE = fromString("PreviousValue");

    /** Static value CustomValue for DataFeedMissingDataPointFillType. */
    public static final DataFeedMissingDataPointFillType CUSTOM_VALUE = fromString("CustomValue");

    /** Static value NoFilling for DataFeedMissingDataPointFillType. */
    public static final DataFeedMissingDataPointFillType NO_FILLING = fromString("NoFilling");

    /**
     * Creates or finds a DataFeedMissingDataPointFillType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DataFeedMissingDataPointFillType.
     */
    public static DataFeedMissingDataPointFillType fromString(String name) {
        return fromString(name, DataFeedMissingDataPointFillType.class);
    }
}
