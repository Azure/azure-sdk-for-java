// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.MySqlDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The MySqlDataFeedSource model.
 */
@Immutable
public final class MySqlDataFeedSource extends DataFeedSource {
    /*
     * Database connection string
     */
    private final String connectionString;

    /*
     * Query script
     */
    private final String query;

    static {
        MySqlDataFeedSourceAccessor.setAccessor(
            new MySqlDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(MySqlDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a MySqlDataFeedSource instance.
     *
     * @param connectionString the database connection string
     * @param query the query value.
     */
    public MySqlDataFeedSource(final String connectionString, final String query) {
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
