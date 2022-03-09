// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * The Table service retention policy.
 */
@Fluent
public final class TableServiceRetentionPolicy {
    /*
     * Indicates whether a retention policy is enabled for the Table service.
     */
    private boolean enabled;

    /*
     * Indicates the number of days that metrics or logging or soft-deleted data should be retained. All data older
     * than this value will be deleted.
     */
    private Integer daysToRetain;

    /**
     * Get a value that indicates whether a retention policy is enabled for the Table service.
     *
     * @return The {@code enabled} value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set a value that indicates whether a retention policy is enabled for the Table service.
     *
     * @param enabled The {@code enabled} value to set.
     *
     * @return The updated {@link TableServiceRetentionPolicy} object.
     */
    public TableServiceRetentionPolicy setEnabled(boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the number of days that metrics or logging or soft-deleted data should be retained. All data older than
     * this value will be deleted.
     *
     * @return The {@code daysToRetain}.
     */
    public Integer getDaysToRetain() {
        return this.daysToRetain;
    }

    /**
     * Set the number of daysToRetain that metrics or logging or soft-deleted data should be retained. All data older
     * than this value will be deleted.
     *
     * @param daysToRetain The {@code daysToRetain} to set.
     *
     * @return The updated {@link TableServiceRetentionPolicy} object.
     */
    public TableServiceRetentionPolicy setDaysToRetain(Integer daysToRetain) {
        this.daysToRetain = daysToRetain;

        return this;
    }
}
