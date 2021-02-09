// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for DataPointType. */
public final class DataPointType extends ExpandableStringEnum<DataPointType> {
    /** Static value Measurement for DataPointType. */
    public static final DataPointType MEASUREMENT = fromString("Measurement");

    /** Static value Aggregation for DataPointType. */
    public static final DataPointType AGGREGATION = fromString("Aggregation");

    /**
     * Creates or finds a DataPointType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DataPointType.
     */
    @JsonCreator
    public static DataPointType fromString(String name) {
        return fromString(name, DataPointType.class);
    }

    /** @return known DataPointType values. */
    public static Collection<DataPointType> values() {
        return values(DataPointType.class);
    }
}
