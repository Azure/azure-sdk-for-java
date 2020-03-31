/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb;


import com.azure.management.cosmosdb.models.SqlDatabaseGetResultsInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * An Azure Cosmos DB SQL database.
 */
public interface SqlDatabase
        extends HasInner<SqlDatabaseGetResultsInner> {

    /**
     * Get name of the Cosmos DB SQL database.
     *
     * @return the sqlDatabaseId value
     */
    String sqlDatabaseId();

    /**
     * Get a system generated property. A unique identifier.
     *
     * @return the _rid value
     */
    String _rid();

    /**
     * Get a system generated property that denotes the last updated timestamp of the resource.
     *
     * @return the _ts value
     */
    Object _ts();

    /**
     * Get a system generated property representing the resource etag required for optimistic concurrency control.
     *
     * @return the _etag value
     */
    String _etag();

    /**
     * Get a system generated property that specified the addressable path of the collections resource.
     *
     * @return the _colls value
     */
    String _colls();

    /**
     * Get a system generated property that specifies the addressable path of the users resource.
     *
     * @return the _users value
     */
    String _users();
}
