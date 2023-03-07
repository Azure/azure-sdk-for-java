// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.config;

/**
 * Throughput config for database creation
 */
public class DatabaseThroughputConfig {

    private final boolean autoScale;
    private final int requestUnits;

    /**
     * Constructor.
     *
     * @param autoScale flag to enable autoscale.
     * @param requestUnits the request units.
     */
    public DatabaseThroughputConfig(boolean autoScale, int requestUnits) {
        this.autoScale = autoScale;
        this.requestUnits = requestUnits;
    }

    /**
     * Flag to indicate whether autoscale is enabled.
     *
     * @return Flag to indicate whether autoscale is enabled.
     */
    public boolean isAutoScale() {
        return autoScale;
    }

    /**
     * Get the request units.
     *
     * @return The request units.
     */
    public int getRequestUnits() {
        return requestUnits;
    }

    @Override
    public String toString() {
        return "DatabaseThroughputConfig{"
            + "autoScale=" + autoScale
            + ", requestUnits=" + requestUnits
            + '}';
    }

}
