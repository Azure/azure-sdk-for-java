// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;

/**
 * a summary of request statistics grouped by API in hour or minute aggregates for datalake.
 */
@Fluent
public final class DataLakeMetrics {
    /*
     * The version of Storage Analytics to configure.
     */
    private String version;

    /*
     * Indicates whether metrics are enabled for the DataLake service.
     */
    private boolean enabled;

    /*
     * The retentionPolicy property.
     */
    private DataLakeRetentionPolicy retentionPolicy;

    /*
     * Indicates whether metrics should generate summary statistics for called
     * API operations.
     */
    private Boolean includeApis;

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
     * @return the DataLakeMetrics object itself.
     */
    public DataLakeMetrics setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get the enabled property: Indicates whether metrics are enabled for the
     * DataLake service.
     *
     * @return the enabled value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: Indicates whether metrics are enabled for the
     * DataLake service.
     *
     * @param enabled the enabled value to set.
     * @return the DataLakeMetrics object itself.
     */
    public DataLakeMetrics setEnabled(boolean enabled) {
        this.enabled = enabled;
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
     * @return the DataLakeMetrics object itself.
     */
    public DataLakeMetrics setRetentionPolicy(DataLakeRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    /**
     * Get the includeApis property: Indicates whether metrics should generate
     * summary statistics for called API operations.
     *
     * @return the includeApis value.
     */
    public Boolean isIncludeApis() {
        return this.includeApis;
    }

    /**
     * Set the includeApis property: Indicates whether metrics should generate
     * summary statistics for called API operations.
     *
     * @param includeApis the includeApis value to set.
     * @return the DataLakeMetrics object itself.
     */
    public DataLakeMetrics setIncludeApis(Boolean includeApis) {
        this.includeApis = includeApis;
        return this;
    }
}
