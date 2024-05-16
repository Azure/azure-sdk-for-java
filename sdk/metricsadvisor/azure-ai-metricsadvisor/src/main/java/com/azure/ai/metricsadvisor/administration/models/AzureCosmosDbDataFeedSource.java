// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureCosmosDbDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureCosmosDbDataFeedSource model.
 */
@Immutable
public final class AzureCosmosDbDataFeedSource extends DataFeedSource {
    /*
     * Azure CosmosDB connection string
     */
    private final String connectionString;

    /*
     * Query script
     */
    private final String sqlQuery;

    /*
     * Database name
     */
    private final String database;

    /*
     * Collection id
     */
    private final String collectionId;

    static {
        AzureCosmosDbDataFeedSourceAccessor.setAccessor(
            new AzureCosmosDbDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(AzureCosmosDbDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a AzureCosmosDataFeedSource.
     *
     * @param connectionString the Azure CosmosDB connection string.
     * @param sqlQuery the query script.
     * @param database the database name.
     * @param collectionId the collection Id value.
     */
    public AzureCosmosDbDataFeedSource(final String connectionString,
                                       final String sqlQuery,
                                       final String database,
                                       final String collectionId) {
        this.connectionString = connectionString;
        this.sqlQuery = sqlQuery;
        this.database = database;
        this.collectionId = collectionId;
    }

    /**
     * Get the sqlQuery property: Query script.
     *
     * @return the sqlQuery value.
     */
    public String getSqlQuery() {
        return this.sqlQuery;
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
     * Get the collectionId property: Collection id.
     *
     * @return the collectionId value.
     */
    public String getCollectionId() {
        return this.collectionId;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}
