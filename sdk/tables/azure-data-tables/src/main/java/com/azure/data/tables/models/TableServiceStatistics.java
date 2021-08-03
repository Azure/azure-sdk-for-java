// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;

/**
 * A model representing Table service statistics.
 */
@Immutable
public final class TableServiceStatistics {
    /*
     * Geo-Replication information for the Secondary Storage Service.
     */
    private final TableServiceGeoReplication geoReplication;

    /**
     * Creates an instance of {@link TableServiceStatistics}.
     *
     * @param geoReplication Geo-Replication information for the Secondary Storage Service.
     */
    public TableServiceStatistics(TableServiceGeoReplication geoReplication) {
        this.geoReplication = geoReplication;
    }

    /**
     * Get Geo-Replication information for the Secondary Storage Service.
     *
     * @return The {@link TableServiceGeoReplication Geo-Replication information}.
     */
    public TableServiceGeoReplication getGeoReplication() {
        return this.geoReplication;
    }
}
