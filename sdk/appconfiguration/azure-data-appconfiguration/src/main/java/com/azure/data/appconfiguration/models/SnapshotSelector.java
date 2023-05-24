// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Snapshot selector, the optional bag
 */
@Fluent
public final class SnapshotSelector {
    private String name;
    private List<SnapshotStatus> status;

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
    public List<SnapshotStatus> getSnapshotStatus() {
        return status;
    }

    /**
     * Sets the snapshot status.
     *
     * @param status the snapshot status.
     * @return The updated SnapshotSelector object
     */
    public SnapshotSelector setSnapshotStatusList(List<SnapshotStatus> status) {
        this.status = status;
        return this;
    }

}
