// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage Service Properties.
 */
@Fluent
public final class DataLakeServiceProperties {
    /*
     * The logging property.
     */
    private DataLakeAnalyticsLogging logging;

    /*
     * The hourMetrics property.
     */
    private DataLakeMetrics hourMetrics;

    /*
     * The minuteMetrics property.
     */
    private DataLakeMetrics minuteMetrics;

    /*
     * The set of CORs rules.
     */
    private List<DataLakeCorsRule> cors;

    /*
     * The default version to use for requests to the DataLake service if an
     * incoming request's version is not specified. Possible values include
     * version 2008-10-27 and all more recent versions
     */
    private String defaultServiceVersion;

    /*
     * The deleteRetentionPolicy property.
     */
    private DataLakeRetentionPolicy deleteRetentionPolicy;


    /*
     * The properties that enable an account to host a static website
     */
    private DataLakeStaticWebsite staticWebsite;

    /**
     * Get the logging property: The logging property.
     *
     * @return the logging value.
     */
    public DataLakeAnalyticsLogging getLogging() {
        return this.logging;
    }

    /**
     * Set the logging property: The logging property.
     *
     * @param logging the logging value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setLogging(DataLakeAnalyticsLogging logging) {
        this.logging = logging;
        return this;
    }

    /**
     * Get the hourMetrics property: The hourMetrics property.
     *
     * @return the hourMetrics value.
     */
    public DataLakeMetrics getHourMetrics() {
        return this.hourMetrics;
    }

    /**
     * Set the hourMetrics property: The hourMetrics property.
     *
     * @param hourMetrics the hourMetrics value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setHourMetrics(DataLakeMetrics hourMetrics) {
        this.hourMetrics = hourMetrics;
        return this;
    }

    /**
     * Get the minuteMetrics property: The minuteMetrics property.
     *
     * @return the minuteMetrics value.
     */
    public DataLakeMetrics getMinuteMetrics() {
        return this.minuteMetrics;
    }

    /**
     * Set the minuteMetrics property: The minuteMetrics property.
     *
     * @param minuteMetrics the minuteMetrics value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setMinuteMetrics(DataLakeMetrics minuteMetrics) {
        this.minuteMetrics = minuteMetrics;
        return this;
    }

    /**
     * Get the cors property: The set of CORS rules.
     *
     * @return the cors value.
     */
    public List<DataLakeCorsRule> getCors() {
        if (this.cors == null) {
            this.cors = new ArrayList<DataLakeCorsRule>();
        }
        return this.cors;
    }

    /**
     * Set the cors property: The set of CORS rules.
     *
     * @param cors the cors value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setCors(List<DataLakeCorsRule> cors) {
        this.cors = cors;
        return this;
    }

    /**
     * Get the defaultServiceVersion property: The default version to use for
     * requests to the DataLake service if an incoming request's version is not
     * specified. Possible values include version 2008-10-27 and all more
     * recent versions.
     *
     * @return the defaultServiceVersion value.
     */
    public String getDefaultServiceVersion() {
        return this.defaultServiceVersion;
    }

    /**
     * Set the defaultServiceVersion property: The default version to use for
     * requests to the DataLake service if an incoming request's version is not
     * specified. Possible values include version 2008-10-27 and all more
     * recent versions.
     *
     * @param defaultServiceVersion the defaultServiceVersion value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setDefaultServiceVersion(String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
        return this;
    }

    /**
     * Get the deleteRetentionPolicy property: The deleteRetentionPolicy
     * property.
     *
     * @return the deleteRetentionPolicy value.
     */
    public DataLakeRetentionPolicy getDeleteRetentionPolicy() {
        return this.deleteRetentionPolicy;
    }

    /**
     * Set the deleteRetentionPolicy property: The deleteRetentionPolicy
     * property.
     *
     * @param deleteRetentionPolicy the deleteRetentionPolicy value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setDeleteRetentionPolicy(DataLakeRetentionPolicy deleteRetentionPolicy) {
        this.deleteRetentionPolicy = deleteRetentionPolicy;
        return this;
    }

    /**
     * Get the staticWebsite property: The properties that enable an account to host a static website.
     *
     * @return the staticWebsite value.
     */
    public DataLakeStaticWebsite getStaticWebsite() {
        return this.staticWebsite;
    }

    /**
     * Set the staticWebsite property: The properties that enable an account to host a static website.
     *
     * @param staticWebsite the staticWebsite value to set.
     * @return the DataLakeServiceProperties object itself.
     */
    public DataLakeServiceProperties setStaticWebsite(DataLakeStaticWebsite staticWebsite) {
        this.staticWebsite = staticWebsite;
        return this;
    }
}
