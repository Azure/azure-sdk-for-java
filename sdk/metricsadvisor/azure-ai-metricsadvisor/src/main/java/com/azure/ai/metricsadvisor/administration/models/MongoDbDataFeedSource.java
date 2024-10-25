// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.MongoDbDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/** The MongoDbDataFeedSource model. */
@Immutable
public final class MongoDbDataFeedSource extends DataFeedSource {
    /*
     * MongoDB connection string
     */
    private final String connectionString;

    /*
     * Database name
     */
    private final String database;

    /*
     * Query script
     */
    private final String command;

    static {
        MongoDbDataFeedSourceAccessor.setAccessor(
            new MongoDbDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(MongoDbDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a MongoDbDataFeedSource instance.
     *
     * @param connectionString The connection string.
     * @param database The database name.
     * @param command The command value.
     */
    public MongoDbDataFeedSource(final String connectionString, final String database, final String command) {
        this.connectionString = connectionString;
        this.database = database;
        this.command = command;
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
     * Get the command property: Query script.
     *
     * @return the command value.
     */
    public String getCommand() {
        return this.command;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
