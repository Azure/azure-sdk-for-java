// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.PostgreSqlDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The PostgreSqlDataFeedSource model.
 */
@Immutable
public final class PostgreSqlDataFeedSource extends DataFeedSource {
    /*
     * Database connection string
     */
    private final String connectionString;

    /*
     * Query script
     */
    private final String query;

    static {
        PostgreSqlDataFeedSourceAccessor.setAccessor(
            new PostgreSqlDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(PostgreSqlDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a PostgreSqlDataFeedSource instance.
     *
     * @param connectionString Database connection string.
     * @param query the query value.
     */
    public PostgreSqlDataFeedSource(final String connectionString, final String query) {
        this.connectionString = connectionString;
        this.query = query;
    }

    /**
     * Get the query property: Query script.
     *
     * @return the query value.
     */
    public String getQuery() {
        return this.query;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
