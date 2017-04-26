/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlDatabases;
import com.microsoft.azure.management.sql.SqlServer;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for SQLDatabases and its parent interfaces.
 */
@LangDefinition
class SqlDatabasesImpl extends IndependentChildResourcesImpl<
            SqlDatabase,
            SqlDatabaseImpl,
            DatabaseInner,
            DatabasesInner,
            SqlServerManager,
            SqlServer>
        implements SqlDatabases.SqlDatabaseCreatable,
        SupportsGettingByParent<SqlDatabase, SqlServer, SqlServerManager>,
        SupportsListingByParent<SqlDatabase, SqlServer, SqlServerManager> {
    protected SqlDatabasesImpl(SqlServerManager manager) {
        super(manager.inner().databases(), manager);
    }

    @Override
    protected SqlDatabaseImpl wrapModel(String name) {
        DatabaseInner inner = new DatabaseInner();
        return new SqlDatabaseImpl(name, inner, this.manager());
    }

    @Override
    public Observable<SqlDatabase> getByParentAsync(String resourceGroup, String parentName, String name) {
        return this.inner().getAsync(resourceGroup, parentName, name).map(new Func1<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase call(DatabaseInner databaseInner) {
                return wrapModel(databaseInner);
            }
        });
    }

    @Override
    public PagedList<SqlDatabase> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.inner().listByServer(resourceGroupName, parentName));
    }

    @Override
    protected SqlDatabaseImpl wrapModel(DatabaseInner inner) {
        if (inner == null) {
            return null;
        }

        return new SqlWarehouseImpl(inner.name(), inner, this.manager());
    }

    @Override
    public SqlDatabase.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return this.inner().deleteAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public SqlDatabase getBySqlServer(String resourceGroup, String sqlServerName, String name) {
        return this.getByParent(resourceGroup, sqlServerName, name);
    }

    @Override
    public SqlDatabase getBySqlServer(SqlServer sqlServer, String name) {
        return this.getByParent(sqlServer, name);
    }

    @Override
    public List<SqlDatabase> listBySqlServer(String resourceGroupName, String sqlServerName) {
        return this.listByParent(resourceGroupName, sqlServerName);
    }

    @Override
    public List<SqlDatabase> listBySqlServer(SqlServer sqlServer) {
        return this.listByParent(sqlServer);
    }

    @Override
    public SqlDatabase.DefinitionStages.Blank definedWithSqlServer(String resourceGroupName, String sqlServerName, String databaseName, Region region) {
        DatabaseInner inner = new DatabaseInner();
        inner.withLocation(region.name());

        return new SqlDatabaseImpl(databaseName, inner, this.manager())
                .withExistingParentResource(resourceGroupName, sqlServerName);
    }
}
