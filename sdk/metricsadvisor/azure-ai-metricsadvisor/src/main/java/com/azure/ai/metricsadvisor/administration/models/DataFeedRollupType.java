// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for DataFeedRollupType.
 */
public final class DataFeedRollupType extends ExpandableStringEnum<DataFeedRollupType> {
    /**
     * Enum value NoRollup.
     */
    public static final DataFeedRollupType NO_ROLLUP = fromString("NoRollup");

    /**
     * Enum value NeedRollup.
     */
    public static final DataFeedRollupType AUTO_ROLLUP = fromString("NeedRollup");

    /**
     * Enum value AlreadyRollup.
     */
    public static final DataFeedRollupType ALREADY_ROLLUP = fromString("AlreadyRollup");

    /**
     * Creates or finds a DataFeedRollupType from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding DataFeedRollupType.
     */
    public static DataFeedRollupType fromString(String name) {
        return fromString(name, DataFeedRollupType.class);
    }

    /**
     * @return known DataFeedRollupType values.
     */
    public static Collection<DataFeedRollupType> values() {
        return values(DataFeedRollupType.class);
    }
}
