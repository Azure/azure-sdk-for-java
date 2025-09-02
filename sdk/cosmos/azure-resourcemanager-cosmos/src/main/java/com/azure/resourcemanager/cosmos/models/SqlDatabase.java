// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.models;

import com.azure.resourcemanager.cosmos.fluent.models.SqlDatabaseGetResultsInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An Azure Cosmos DB SQL database. */
public interface SqlDatabase extends HasInnerModel<SqlDatabaseGetResultsInner> {

    /**
     * Get name of the Cosmos DB SQL database.
     *
     * @return the sqlDatabaseId value
     */
    String sqlDatabaseId();

    /**
     * Get a system generated property. A unique identifier.
     *
     * @return the rid value
     */
    String rid();

    /**
     * Get a system generated property that denotes the last updated timestamp of the resource.
     *
     * @return the ts value
     */
    Object ts();

    /**
     * Get a system generated property representing the resource etag required for optimistic concurrency control.
     *
     * @return the etag value
     */
    String etag();

    /**
     * Get a system generated property that specified the addressable path of the collections resource.
     *
     * @return the colls value
     */
    String colls();

    /**
     * Get a system generated property that specifies the addressable path of the users resource.
     *
     * @return the users value
     */
    String users();
}
