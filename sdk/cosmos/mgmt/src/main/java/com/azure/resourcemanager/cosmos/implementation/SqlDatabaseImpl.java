// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.models.SqlDatabase;
import com.azure.resourcemanager.cosmos.fluent.inner.SqlDatabaseGetResultsInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** An Azure Cosmos DB SQL database. */
public class SqlDatabaseImpl extends WrapperImpl<SqlDatabaseGetResultsInner> implements SqlDatabase {

    SqlDatabaseImpl(SqlDatabaseGetResultsInner innerObject) {
        super(innerObject);
    }

    @Override
    public String sqlDatabaseId() {
        return this.inner().resource().id();
    }

    @Override
    public String rid() {
        return this.inner().resource().rid();
    }

    @Override
    public Object ts() {
        return this.inner().resource().ts();
    }

    @Override
    public String etag() {
        return this.inner().resource().etag();
    }

    @Override
    public String colls() {
        return this.inner().resource().colls();
    }

    @Override
    public String users() {
        return this.inner().resource().users();
    }
}
