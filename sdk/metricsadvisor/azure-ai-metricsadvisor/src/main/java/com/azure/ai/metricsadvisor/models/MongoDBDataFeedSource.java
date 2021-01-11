// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;

/** The MongoDBDataFeedSource model. */
@Immutable
public final class MongoDBDataFeedSource extends DataFeedSource {
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

    /**
     * Create a MongoDBDataFeedSource instance.
     *
     * @param connectionString The connection string.
     * @param database The database name.
     * @param command The command value.
     */
    public MongoDBDataFeedSource(final String connectionString, final String database, final String command) {
        this.connectionString = connectionString;
        this.database = database;
        this.command = command;
    }

    /**
     * Get the connectionString property: MongoDB connection string.
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
     * Get the command property: Query script.
     *
     * @return the command value.
     */
    public String getCommand() {
        return this.command;
    }

}
