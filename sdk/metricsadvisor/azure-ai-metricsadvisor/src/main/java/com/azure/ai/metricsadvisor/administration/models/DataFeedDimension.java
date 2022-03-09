// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type describing a dimension of a DataFeed.
 */
@Fluent
public final class DataFeedDimension {
    private final String name;
    private String displayName;

    /**
     * Creates a DataFeedDimension.
     *
     * @param name the dimension name.
     */
    public DataFeedDimension(String name) {
        this.name = name;
    }

    /**
     * Gets the dimension name.
     *
     * @return the dimension name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the dimension display name.
     *
     * @return the dimension display name
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Sets the dimension display name.
     *
     * @param displayName the dimension display name
     * @return the DataFeedDimension object itself.
     */
    public DataFeedDimension setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }
}
