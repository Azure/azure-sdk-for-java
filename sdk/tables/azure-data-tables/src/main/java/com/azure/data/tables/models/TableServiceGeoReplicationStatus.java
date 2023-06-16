// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for {@link TableServiceGeoReplicationStatus}.
 */
public final class TableServiceGeoReplicationStatus extends ExpandableStringEnum<TableServiceGeoReplicationStatus> {
    /**
     * Static value 'live' for {@link TableServiceGeoReplicationStatus}.
     */
    public static final TableServiceGeoReplicationStatus LIVE = fromString("live");

    /**
     * Static value 'bootstrap' for {@link TableServiceGeoReplicationStatus}.
     */
    public static final TableServiceGeoReplicationStatus BOOTSTRAP = fromString("bootstrap");

    /**
     * Static value 'unavailable' for {@link TableServiceGeoReplicationStatus}.
     */
    public static final TableServiceGeoReplicationStatus UNAVAILABLE = fromString("unavailable");

    /**
     * Creates a new instance of TableServiceGeoReplicationStatus value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public TableServiceGeoReplicationStatus() {}

    /**
     * Creates or finds a {@link TableServiceGeoReplicationStatus} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link TableServiceGeoReplicationStatus}.
     */
    public static TableServiceGeoReplicationStatus fromString(String name) {
        return fromString(name, TableServiceGeoReplicationStatus.class);
    }

    /**
     * Gets known TableServiceGeoReplicationStatus values.
     *
     * @return known TableServiceGeoReplicationStatus values.
     */
    public static Collection<TableServiceGeoReplicationStatus> values() {
        return values(TableServiceGeoReplicationStatus.class);
    }
}
