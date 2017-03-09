/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.sql.SqlWarehouse;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;

/**
 * Implementation for SqlWarehouse and its parent interfaces.
 */
@LangDefinition
class SqlWarehouseImpl
        extends SqlDatabaseImpl
        implements SqlWarehouse {

    protected SqlWarehouseImpl(String name, DatabaseInner innerObject, SqlServerManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public void pauseDataWarehouse() {
        this.pauseDataWarehouseAsync().await();
    }

    @Override
    public Completable pauseDataWarehouseAsync() {
        return this.manager().inner().databases().pauseDataWarehouseAsync(
                this.resourceGroupName(), this.sqlServerName(), this.name()).toCompletable();
    }

    @Override
    public ServiceFuture<Void> pauseDataWarehouseAsync(ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.pauseDataWarehouseAsync().<Void>toObservable(), callback);
    }

    @Override
    public void resumeDataWarehouse() {
        this.resumeDataWarehouseAsync().await();
    }

    @Override
    public Completable resumeDataWarehouseAsync() {
        return this.manager().inner().databases().resumeDataWarehouseAsync(
                this.resourceGroupName(), this.sqlServerName(), this.name()).toCompletable();
    }

    @Override
    public ServiceFuture<Void> resumeDataWarehouseAsync(ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.resumeDataWarehouseAsync().<Void>toObservable(), callback);
    }
}
