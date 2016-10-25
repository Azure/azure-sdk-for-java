/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabases;
import rx.Observable;

/**
 * Implementation for SQLDatabases and its parent interfaces.
 */
@LangDefinition
public class SqlDatabasesImpl extends IndependentChildResourcesImpl<
            SqlDatabase,
            SqlDatabaseImpl,
            DatabaseInner,
            DatabasesInner,
            SqlServerManager>
        implements SqlDatabases,
        SupportsGettingByParent<SqlDatabase>,
        SupportsListingByParent<SqlDatabase> {
    protected SqlDatabasesImpl(DatabasesInner innerCollection, SqlServerManager manager) {
        super(innerCollection, manager);
    }

    @Override
    protected SqlDatabaseImpl wrapModel(String name) {
        DatabaseInner inner = new DatabaseInner();
        return new SqlDatabaseImpl(
                name,
                inner,
                this.innerCollection,
                manager.sqlElasticPools());
    }

    @Override
    public SqlDatabase getByParent(String resourceGroup, String parentName, String name) {
        return wrapModel(this.innerCollection.get(resourceGroup, parentName, name));
    }

    @Override
    public PagedList<SqlDatabase> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.innerCollection.listByServer(resourceGroupName, parentName));
    }

    @Override
    protected SqlDatabaseImpl wrapModel(DatabaseInner inner) {
        return new SqlDatabaseImpl(inner.name(), inner, this.innerCollection, manager.sqlElasticPools());
    }

    @Override
    public SqlDatabase.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return this.innerCollection.deleteAsync(groupName, parentName, name);
    }

    @Override
    public SqlDatabase getBySqlServer(String resourceGroup, String sqlServerName, String name) {
        return this.getByParent(resourceGroup, sqlServerName, name);
    }

    @Override
    public SqlDatabase getBySqlServer(GroupableResource sqlServer, String name) {
        return this.getByParent(sqlServer, name);
    }

    @Override
    public PagedList<SqlDatabase> listBySqlServer(String resourceGroupName, String sqlServerName) {
        return this.listByParent(resourceGroupName, sqlServerName);
    }

    @Override
    public PagedList<SqlDatabase> listBySqlServer(GroupableResource sqlServer) {
        return this.listByParent(sqlServer);
    }
}
