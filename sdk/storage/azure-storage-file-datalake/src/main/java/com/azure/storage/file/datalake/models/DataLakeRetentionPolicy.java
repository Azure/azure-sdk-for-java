// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;

/**
 * the retention policy which determines how long the associated data should
 * persist.
 */
@Fluent
public final class DataLakeRetentionPolicy {
    /*
     * Indicates whether a retention policy is enabled for the storage service
     */
    private boolean enabled;

    /*
     * Indicates the number of days that metrics or logging or soft-deleted
     * data should be retained. All data older than this value will be deleted
     */
    private Integer days;

    /**
     * Get the enabled property: Indicates whether a retention policy is
     * enabled for the storage service.
     *
     * @return the enabled value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: Indicates whether a retention policy is
     * enabled for the storage service.
     *
     * @param enabled the enabled value to set.
     * @return the DataLakeRetentionPolicy object itself.
     */
    public DataLakeRetentionPolicy setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the days property: Indicates the number of days that metrics or
     * logging or soft-deleted data should be retained. All data older than
     * this value will be deleted.
     *
     * @return the days value.
     */
    public Integer getDays() {
        return this.days;
    }

    /**
     * Set the days property: Indicates the number of days that metrics or
     * logging or soft-deleted data should be retained. All data older than
     * this value will be deleted.
     *
     * @param days the days value to set.
     * @return the DataLakeRetentionPolicy object itself.
     */
    public DataLakeRetentionPolicy setDays(Integer days) {
        this.days = days;
        return this;
    }
}
