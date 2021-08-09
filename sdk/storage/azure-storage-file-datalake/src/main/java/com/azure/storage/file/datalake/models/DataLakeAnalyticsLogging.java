// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;

/**
 * Azure Analytics Logging settings.
 */
@Fluent
public final class DataLakeAnalyticsLogging {
    /*
     * The version of Storage Analytics to configure.
     */
    private String version;

    /*
     * Indicates whether all delete requests should be logged.
     */
    private boolean delete;

    /*
     * Indicates whether all read requests should be logged.
     */
    private boolean read;

    /*
     * Indicates whether all write requests should be logged.
     */
    private boolean write;

    /*
     * The retentionPolicy property.
     */
    private DataLakeRetentionPolicy retentionPolicy;

    /**
     * Get the version property: The version of Storage Analytics to configure.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the version property: The version of Storage Analytics to configure.
     *
     * @param version the version value to set.
     * @return the DataLakeAnalyticsLogging object itself.
     */
    public DataLakeAnalyticsLogging setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get the delete property: Indicates whether all delete requests should be
     * logged.
     *
     * @return the delete value.
     */
    public boolean isDelete() {
        return this.delete;
    }

    /**
     * Set the delete property: Indicates whether all delete requests should be
     * logged.
     *
     * @param delete the delete value to set.
     * @return the DataLakeAnalyticsLogging object itself.
     */
    public DataLakeAnalyticsLogging setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    /**
     * Get the read property: Indicates whether all read requests should be
     * logged.
     *
     * @return the read value.
     */
    public boolean isRead() {
        return this.read;
    }

    /**
     * Set the read property: Indicates whether all read requests should be
     * logged.
     *
     * @param read the read value to set.
     * @return the DataLakeAnalyticsLogging object itself.
     */
    public DataLakeAnalyticsLogging setRead(boolean read) {
        this.read = read;
        return this;
    }

    /**
     * Get the write property: Indicates whether all write requests should be
     * logged.
     *
     * @return the write value.
     */
    public boolean isWrite() {
        return this.write;
    }

    /**
     * Set the write property: Indicates whether all write requests should be
     * logged.
     *
     * @param write the write value to set.
     * @return the DataLakeAnalyticsLogging object itself.
     */
    public DataLakeAnalyticsLogging setWrite(boolean write) {
        this.write = write;
        return this;
    }

    /**
     * Get the retentionPolicy property: The retentionPolicy property.
     *
     * @return the retentionPolicy value.
     */
    public DataLakeRetentionPolicy getRetentionPolicy() {
        return this.retentionPolicy;
    }

    /**
     * Set the retentionPolicy property: The retentionPolicy property.
     *
     * @param retentionPolicy the retentionPolicy value to set.
     * @return the DataLakeAnalyticsLogging object itself.
     */
    public DataLakeAnalyticsLogging setRetentionPolicy(DataLakeRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }
}
