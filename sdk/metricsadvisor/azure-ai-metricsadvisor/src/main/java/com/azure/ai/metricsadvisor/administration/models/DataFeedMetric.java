// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.DataFeedMetricAccessor;
import com.azure.core.annotation.Fluent;

/**
 * Type describing a metric of a DataFeed.
 */
@Fluent
public final class DataFeedMetric {
    private String id;
    private final String name;
    private String displayName;
    private String description;

    static {
        DataFeedMetricAccessor.setAccessor(new DataFeedMetricAccessor.Accessor() {
            @Override
            public void setId(DataFeedMetric dataFeedMetric, String id) {
                dataFeedMetric.setId(id);
            }
        });
    }

    /**
     * Creates a DataFeed metric with the provided name.
     *
     * @param name the metric name.
     */
    public DataFeedMetric(String name) {
        this.name = name;
    }

    /**
     * Gets the id.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the display name.
     *
     * @return the display name value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Gets the description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the display name value to set.
     * @return the DataFeedMetric object itself.
     */
    public DataFeedMetric setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description the description value to set.
     * @return the DataFeedMetric object itself.
     */
    public DataFeedMetric setDescription(String description) {
        this.description = description;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }
}
