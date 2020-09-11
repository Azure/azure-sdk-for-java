// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlWarehouse;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseInner;
import reactor.core.publisher.Mono;

/** Implementation for SqlWarehouse and its parent interfaces. */
class SqlWarehouseImpl extends SqlDatabaseImpl implements SqlWarehouse {

    SqlWarehouseImpl(String name, SqlServerImpl parent, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject, sqlServerManager);
    }

    SqlWarehouseImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlServerLocation,
        String name,
        DatabaseInner innerObject,
        SqlServerManager sqlServerManager) {
        super(resourceGroupName, sqlServerName, sqlServerLocation, name, innerObject, sqlServerManager);
    }

    @Override
    public void pauseDataWarehouse() {
        this.sqlServerManager.inner().getDatabases().pause(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> pauseDataWarehouseAsync() {
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .pauseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .flatMap(databaseInner -> Mono.empty());
    }

    @Override
    public void resumeDataWarehouse() {
        this.sqlServerManager.inner().getDatabases().resume(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> resumeDataWarehouseAsync() {
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .resumeAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .flatMap(databaseInner -> Mono.empty());
    }
}
