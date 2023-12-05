// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * A model representing configurable settings of the Table service.
 */
@Fluent
public final class TableServiceProperties {
    /*
     * Azure Analytics Logging settings.
     */
    private TableServiceLogging logging;

    /*
     * A summary of request statistics grouped by API in hourly aggregates for tables.
     */
    private TableServiceMetrics hourMetrics;

    /*
     * A summary of request statistics grouped by API in minute aggregates for tables.
     */
    private TableServiceMetrics minuteMetrics;

    /*
     * The set of CORS rules.
     */
    private List<TableServiceCorsRule> corsRules;

    /**
     * Set the {@link TableServiceLogging Azure Analytics Logging settings}.
     *
     * @param logging The {@link TableServiceLogging} to set.
     *
     * @return The updated {@link TableServiceProperties} object.
     */
    public TableServiceProperties setLogging(TableServiceLogging logging) {
        this.logging = logging;

        return this;
    }

    /**
     * Get the {@link TableServiceLogging Azure Analytics Logging settings}.
     *
     * @return The {@link TableServiceLogging}.
     */
    public TableServiceLogging getLogging() {
        return this.logging;
    }

    /**
     * Get settings for generating a summary of request statistics grouped by API in hourly aggregates for tables.
     *
     * @return The {@link TableServiceMetrics hourMetrics}.
     */
    public TableServiceMetrics getHourMetrics() {
        return this.hourMetrics;
    }

    /**
     * Set settings for generating a summary of request statistics grouped by API in hourly aggregates for tables.
     *
     * @param hourMetrics The {@link TableServiceMetrics hourMetrics} value to set.
     *
     * @return The updated {@link TableServiceProperties} object.
     */
    public TableServiceProperties setHourMetrics(TableServiceMetrics hourMetrics) {
        this.hourMetrics = hourMetrics;

        return this;
    }

    /**
     * Get settings for generating a summary of request statistics grouped by API in minute aggregates for tables.
     *
     * @return The {@link TableServiceMetrics minuteMetrics}.
     */
    public TableServiceMetrics getMinuteMetrics() {
        return this.minuteMetrics;
    }

    /**
     * Set settings for generating a summary of request statistics grouped by API in minute aggregates for tables.
     *
     * @param minuteMetrics The {@link TableServiceMetrics minuteMetrics} to set.
     *
     * @return The updated {@link TableServiceProperties} object.
     */
    public TableServiceProperties setMinuteMetrics(TableServiceMetrics minuteMetrics) {
        this.minuteMetrics = minuteMetrics;

        return this;
    }

    /**
     * Get the {@link TableServiceCorsRule CORS rules}.
     *
     * @return A collection of {@link TableServiceCorsRule CORS rules}.
     */
    public List<TableServiceCorsRule> getCorsRules() {
        return this.corsRules;
    }

    /**
     * Set the {@link TableServiceCorsRule CORS rules}.
     *
     * @param corsRules A collection of {@link TableServiceCorsRule CORS rules} to set.
     *
     * @return The updated {@link TableServiceProperties} object.
     */
    public TableServiceProperties setCorsRules(List<TableServiceCorsRule> corsRules) {
        this.corsRules = corsRules;

        return this;
    }
}
