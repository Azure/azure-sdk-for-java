// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DataSourceMissingDataPointFillType. */
public final class DataSourceMissingDataPointFillType extends ExpandableStringEnum<DataSourceMissingDataPointFillType> {
    /** Static value SmartFilling for DataSourceMissingDataPointFillType. */
    public static final DataSourceMissingDataPointFillType SMART_FILLING = fromString("SmartFilling");

    /** Static value PreviousValue for DataSourceMissingDataPointFillType. */
    public static final DataSourceMissingDataPointFillType PREVIOUS_VALUE = fromString("PreviousValue");

    /** Static value CustomValue for DataSourceMissingDataPointFillType. */
    public static final DataSourceMissingDataPointFillType CUSTOM_VALUE = fromString("CustomValue");

    /** Static value NoFilling for DataSourceMissingDataPointFillType. */
    public static final DataSourceMissingDataPointFillType NO_FILLING = fromString("NoFilling");

    /**
     * Creates or finds a DataSourceMissingDataPointFillType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DataSourceMissingDataPointFillType.
     */
    public static DataSourceMissingDataPointFillType fromString(String name) {
        return fromString(name, DataSourceMissingDataPointFillType.class);
    }

    /** @return known DataSourceMissingDataPointFillType values. */
    public static Collection<DataSourceMissingDataPointFillType> values() {
        return values(DataSourceMissingDataPointFillType.class);
    }
}
