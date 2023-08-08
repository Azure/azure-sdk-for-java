// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * A model representing configurable metrics settings of the Table service.
 */
@Fluent
public final class TableServiceMetrics {
    /*
     * The version of Analytics to configure.
     */
    private String version;

    /*
     * Indicates whether metrics are enabled for the Table service.
     */
    private boolean enabled;

    /*
     * Indicates whether metrics should generate summary statistics for called API operations.
     */
    private Boolean includeApis;

    /*
     * The retention policy.
     */
    private TableServiceRetentionPolicy retentionPolicy;

    /**
     * Get the version of Analytics to configure.
     *
     * @return The {@code version}.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the version of Analytics to configure.
     *
     * @param version The {@code version} to set.
     *
     * @return The updated {@link TableServiceMetrics} object.
     */
    public TableServiceMetrics setVersion(String version) {
        this.version = version;

        return this;
    }

    /**
     * Get a value that indicates whether metrics are enabled for the Table service.
     *
     * @return The {@code enabled} value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set a value that indicates whether metrics are enabled for the Table service.
     *
     * @param enabled The {@code enabled} value to set.
     *
     * @return The updated {@link TableServiceMetrics} object.
     */
    public TableServiceMetrics setEnabled(boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get a value that indicates whether metrics should generate summary statistics for called API operations.
     *
     * @return The {@code includeApis} value.
     */
    public Boolean isIncludeApis() {
        return this.includeApis;
    }

    /**
     * Set a value that indicates whether metrics should generate summary statistics for called API operations.
     *
     * @param includeApis The {@code includeApis} value to set.
     *
     * @return The updated {@link TableServiceMetrics} object.
     */
    public TableServiceMetrics setIncludeApis(Boolean includeApis) {
        this.includeApis = includeApis;

        return this;
    }

    /**
     * Get the {@link TableServiceRetentionPolicy} for these metrics on the Table service.
     *
     * @return The {@link TableServiceRetentionPolicy}.
     */
    public TableServiceRetentionPolicy getTableServiceRetentionPolicy() {
        return this.retentionPolicy;
    }

    /**
     * Set the {@link TableServiceRetentionPolicy} for these metrics on the Table service.
     *
     * @param retentionPolicy The {@link TableServiceRetentionPolicy} to set.
     *
     * @return The updated {@link TableServiceMetrics} object.
     */
    public TableServiceMetrics setRetentionPolicy(TableServiceRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;

        return this;
    }
}
