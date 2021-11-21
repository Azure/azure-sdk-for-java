// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;
import com.azure.data.tables.implementation.models.RetentionPolicy;

/**
 * A model representing configurable Azure Analytics Logging settings of the Table service.
 */
@Fluent
public final class TableServiceLogging {
    /*
     * The version of Analytics to configure.
     */
    private String analyticsVersion;

    /*
     * Indicates whether all delete requests should be logged.
     */
    private boolean deleteLogged;

    /*
     * Indicates whether all read requests should be logged.
     */
    private boolean readLogged;

    /*
     * Indicates whether all write requests should be logged.
     */
    private boolean writeLogged;

    /*
     * The retention policy.
     */
    private TableServiceRetentionPolicy retentionPolicy;

    /**
     * Get the version of Analytics to configure.
     *
     * @return The {@code analyticsVersion}.
     */
    public String getAnalyticsVersion() {
        return this.analyticsVersion;
    }

    /**
     * Set the version of Analytics to configure.
     *
     * @param analyticsVersion The {@code analyticsVersion} to set.
     *
     * @return The updated {@link TableServiceLogging} object.
     */
    public TableServiceLogging setAnalyticsVersion(String analyticsVersion) {
        this.analyticsVersion = analyticsVersion;

        return this;
    }

    /**
     * Get a value that indicates whether all delete requests should be logged.
     *
     * @return The {@code deleteLogged} value.
     */
    public boolean isDeleteLogged() {
        return this.deleteLogged;
    }

    /**
     * Set a value that indicates whether all delete requests should be logged.
     *
     * @param delete The {@code deleteLogged} value to set.
     *
     * @return The updated {@link TableServiceLogging} object.
     */
    public TableServiceLogging setDeleteLogged(boolean delete) {
        this.deleteLogged = delete;

        return this;
    }

    /**
     * Get a value that indicates whether all read requests should be logged.
     *
     * @return The {@code readLogged} value.
     */
    public boolean isReadLogged() {
        return this.readLogged;
    }

    /**
     * Set a value that indicates whether all read requests should be logged.
     *
     * @param read The {@code readLogged} value to set.
     *
     * @return The updated {@link TableServiceLogging} object.
     */
    public TableServiceLogging setReadLogged(boolean read) {
        this.readLogged = read;

        return this;
    }

    /**
     * Get a value that indicates whether all write requests should be logged.
     *
     * @return The {@code writeLogged} value.
     */
    public boolean isWriteLogged() {
        return this.writeLogged;
    }

    /**
     * Set a value that indicates whether all writeLogged requests should be logged.
     *
     * @param writeLogged The {@code writeLogged} value to set.
     *
     * @return The updated {@link TableServiceLogging} object.
     */
    public TableServiceLogging setWriteLogged(boolean writeLogged) {
        this.writeLogged = writeLogged;

        return this;
    }

    /**
     * Get the {@link RetentionPolicy}.
     *
     * @return The {@link RetentionPolicy}.
     */
    public TableServiceRetentionPolicy getRetentionPolicy() {
        return this.retentionPolicy;
    }

    /**
     * Set the {@link RetentionPolicy}.
     *
     * @param retentionPolicy The {@link RetentionPolicy} to set.
     *
     * @return The updated {@link TableServiceLogging} object.
     */
    public TableServiceLogging setRetentionPolicy(TableServiceRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;

        return this;
    }
}
