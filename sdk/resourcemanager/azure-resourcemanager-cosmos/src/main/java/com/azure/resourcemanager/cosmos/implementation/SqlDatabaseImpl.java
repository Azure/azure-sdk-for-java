// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.models.SqlDatabase;
import com.azure.resourcemanager.cosmos.fluent.models.SqlDatabaseGetResultsInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** An Azure Cosmos DB SQL database. */
public class SqlDatabaseImpl extends WrapperImpl<SqlDatabaseGetResultsInner> implements SqlDatabase {

    SqlDatabaseImpl(SqlDatabaseGetResultsInner innerObject) {
        super(innerObject);
    }

    @Override
    public String sqlDatabaseId() {
        return this.innerModel().resource().id();
    }

    @Override
    public String rid() {
        return this.innerModel().resource().rid();
    }

    @Override
    public Object ts() {
        return this.innerModel().resource().ts();
    }

    @Override
    public String etag() {
        return this.innerModel().resource().etag();
    }

    @Override
    public String colls() {
        return this.innerModel().resource().colls();
    }

    @Override
    public String users() {
        return this.innerModel().resource().users();
    }
}
