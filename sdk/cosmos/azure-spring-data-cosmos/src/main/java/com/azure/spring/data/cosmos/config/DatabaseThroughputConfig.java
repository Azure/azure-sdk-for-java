// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.config;

/**
 * Throughput config for database creation
 */
public class DatabaseThroughputConfig {

    private final boolean autoScale;
    private final int requestUnits;

    public DatabaseThroughputConfig(boolean autoScale, int requestUnits) {
        this.autoScale = autoScale;
        this.requestUnits = requestUnits;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

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
