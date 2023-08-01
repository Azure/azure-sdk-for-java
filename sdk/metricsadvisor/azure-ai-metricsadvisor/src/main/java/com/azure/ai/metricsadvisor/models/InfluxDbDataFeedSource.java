// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.administration.models.DataFeedSource;
import com.azure.ai.metricsadvisor.implementation.util.InfluxDbDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The InfluxDbDataFeedSource model.
 */
@Immutable
public final class InfluxDbDataFeedSource extends DataFeedSource {
    /*
     * InfluxDB connection string
     */
    private final String connectionString;

    /*
     * Database name
     */
    private final String database;

    /*
     * Database access user
     */
    private final String userName;

    /*
     * Database access password
     */
    private final String password;

    /*
     * Query script
     */
    private final String query;

    static {
        InfluxDbDataFeedSourceAccessor.setAccessor(
            new InfluxDbDataFeedSourceAccessor.Accessor() {
                @Override
                public String getPassword(InfluxDbDataFeedSource feedSource) {
                    return feedSource.getPassword();
                }
            });
    }

    /**
     * Create a InfluxDbDataFeedSource instance.
     *
     * @param connectionString InfluxDB connection string
     * @param database the database name.
     * @param userName the database access user.
     * @param password the database access password.
     * @param query the query value.
     */
    public InfluxDbDataFeedSource(final String connectionString, final String database, final String userName,
                                  final String password,
                                  final String query) {
        this.connectionString = connectionString;
        this.database = database;
        this.userName = userName;
        this.password = password;
        this.query = query;
    }

    /**
     * Get the connectionString property: InfluxDB connection string.
     *
     * @return the connectionString value.
     */
    public String getConnectionString() {
        return this.connectionString;
    }

    /**
     * Get the database property: Database name.
     *
     * @return the database value.
     */
    public String getDatabase() {
        return this.database;
    }


    /**
     * Get the userName property: Database access user.
     *
     * @return the userName value.
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Get the query property: Query script.
     *
     * @return the query value.
     */
    public String getQuery() {
        return this.query;
    }

    private String getPassword() {
        return this.password;
    }
}
