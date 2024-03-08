// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for DataFeedAccessMode.
 */
public final class DataFeedAccessMode extends ExpandableStringEnum<DataFeedAccessMode> {

    /**
     * Constructs a DataFeedAccessMode object.
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DataFeedAccessMode() {
    }

    /**
     * Static value Private for DataFeedAccessMode.
     */
    public static final DataFeedAccessMode PRIVATE = fromString("Private");

    /**
     * Static value Public for DataFeedAccessMode.
     */
    public static final DataFeedAccessMode PUBLIC = fromString("Public");

    /**
     * Creates or finds a DataFeedAccessMode from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DataFeedAccessMode.
     */
    public static DataFeedAccessMode fromString(String name) {
        return fromString(name, DataFeedAccessMode.class);
    }

    /**
     * Returns the known DataFeedAccessMode values.
     * @return known DataFeedAccessMode values.
     */
    public static Collection<DataFeedAccessMode> values() {
        return values(DataFeedAccessMode.class);
    }
}
