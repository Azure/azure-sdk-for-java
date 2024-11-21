// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Geo-Replication information for the Secondary Storage Service.
 */
@Immutable
public final class TableServiceGeoReplication {
    /*
     * The status of the secondary location.
     */
    private final TableServiceGeoReplicationStatus status;

    /*
     * A GMT date/time value, to the second. All primary writes preceding this value are guaranteed to be available
     * for read operations at the secondary. Primary writes after this point in time may or may not be available for
     * reads.
     */
    private final OffsetDateTime lastSyncTime;

    /**
     * Creates an instance of {@link TableServiceGeoReplication}.
     *
     * @param status The status of the secondary location.
     * @param lastSyncTime A GMT date/time value, to the second. All primary writes preceding this value are guaranteed
     * to be available for read operations at the secondary. Primary writes after this point in time may or may not
     * be available for reads.
     */
    public TableServiceGeoReplication(TableServiceGeoReplicationStatus status, OffsetDateTime lastSyncTime) {
        this.status = status;
        this.lastSyncTime = lastSyncTime;
    }

    /**
     * Get the {@link TableServiceGeoReplicationStatus status} of the secondary location.
     *
     * @return The {@link TableServiceGeoReplicationStatus status} of the secondary location.
     */
    public TableServiceGeoReplicationStatus getStatus() {
        return this.status;
    }

    /**
     * Get a GMT date/time value, to the second. All primary writes preceding this value are guaranteed to be
     * available for read operations at the secondary. Primary writes after this point in time may or
     * may not be available for reads.
     *
     * @return The {@link OffsetDateTime lastSyncTime} value.
     */
    public OffsetDateTime getLastSyncTime() {
        return this.lastSyncTime;
    }
}
