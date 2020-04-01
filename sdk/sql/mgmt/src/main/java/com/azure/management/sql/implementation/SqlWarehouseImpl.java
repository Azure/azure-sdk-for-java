/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.management.sql.SqlWarehouse;
import com.azure.management.sql.models.DatabaseInner;
import reactor.core.publisher.Mono;

/**
 * Implementation for SqlWarehouse and its parent interfaces.
 */
class SqlWarehouseImpl
        extends SqlDatabaseImpl
        implements SqlWarehouse {

    SqlWarehouseImpl(String name, SqlServerImpl parent, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject, sqlServerManager);
    }

    SqlWarehouseImpl(String resourceGroupName, String sqlServerName, String sqlServerLocation, String name, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(resourceGroupName, sqlServerName, sqlServerLocation, name, innerObject, sqlServerManager);
    }

    @Override
    public void pauseDataWarehouse() {
        this.sqlServerManager.inner().databases()
            .pause(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> pauseDataWarehouseAsync() {
        return this.sqlServerManager.inner().databases()
            .pauseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .flatMap(databaseInner -> Mono.empty());
    }

    @Override
    public void resumeDataWarehouse() {
        this.sqlServerManager.inner().databases()
            .resume(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> resumeDataWarehouseAsync() {
        return this.sqlServerManager.inner().databases()
            .resumeAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .flatMap(databaseInner -> Mono.empty());
    }
}
