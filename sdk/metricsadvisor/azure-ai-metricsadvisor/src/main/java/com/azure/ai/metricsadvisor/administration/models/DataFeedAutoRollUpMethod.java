// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for DataFeedAutoRollUpMethod.
 */
public final class DataFeedAutoRollUpMethod extends ExpandableStringEnum<DataFeedAutoRollUpMethod> {
    /**
     * Static value None for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod NONE = fromString("None");

    /**
     * Static value Sum for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod SUM = fromString("Sum");

    /**
     * Static value Max for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod MAX = fromString("Max");

    /**
     * Static value Min for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod MIN = fromString("Min");

    /**
     * Static value Avg for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod AVG = fromString("Avg");

    /**
     * Static value Count for DataFeedAutoRollUpMethod.
     */
    public static final DataFeedAutoRollUpMethod COUNT = fromString("Count");

    /**
     * Creates or finds a DataFeedAutoRollUpMethod from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding DataFeedAutoRollUpMethod.
     */
    public static DataFeedAutoRollUpMethod fromString(String name) {
        return fromString(name, DataFeedAutoRollUpMethod.class);
    }

    /**
     * Return the set of enum values for DataFeedAutoRollUpMethod.
     *
     * @return known DataFeedAutoRollUpMethod values.
     */
    public static Collection<DataFeedAutoRollUpMethod> values() {
        return values(DataFeedAutoRollUpMethod.class);
    }
}
