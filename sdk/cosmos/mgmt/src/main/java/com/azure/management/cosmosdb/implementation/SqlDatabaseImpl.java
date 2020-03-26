/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb.implementation;

import com.azure.management.cosmosdb.SqlDatabase;
import com.azure.management.cosmosdb.models.SqlDatabaseGetResultsInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * An Azure Cosmos DB SQL database.
 */
public class SqlDatabaseImpl
        extends WrapperImpl<SqlDatabaseGetResultsInner>
        implements SqlDatabase {

    SqlDatabaseImpl(SqlDatabaseGetResultsInner innerObject) {
        super(innerObject);
    }

    @Override
    public String sqlDatabaseId() {
        return this.inner().resource().getId();
    }

    @Override
    public String _rid() {
        return this.inner().resource().rid();
    }

    @Override
    public Object _ts() {
        return this.inner().resource().ts();
    }

    @Override
    public String _etag() {
        return this.inner().resource().etag();
    }

    @Override
    public String _colls() {
        return this.inner().resource().colls();
    }

    @Override
    public String _users() {
        return this.inner().resource().users();
    }
}
