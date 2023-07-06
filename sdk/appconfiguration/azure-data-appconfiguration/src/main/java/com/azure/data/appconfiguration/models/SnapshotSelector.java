// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * Snapshot selector, the optional bag
 */
@Fluent
public final class SnapshotSelector {
    private String name;
    private Iterable<SnapshotStatus> status;

    /**
     * Gets the snapshot name
     *
     * @return The snapshot name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the snapshot name.
     *
     * @param name the snapshot name.
     * @return The updated SnapshotSelector object
     */
    public SnapshotSelector setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the snapshot status
     *
     * @return The snapshot status.
     */
    public Iterable<SnapshotStatus> getSnapshotStatus() {
        return status;
    }

    /**
     * Sets the snapshot status. Used to filter returned snapshots by their status properties.
     *
     * @param status the snapshot status.
     * @return The updated SnapshotSelector object
     */
    public SnapshotSelector setSnapshotStatus(SnapshotStatus... status) {
        this.status = status == null ? null : Arrays.asList(status);
        return this;
    }

}
