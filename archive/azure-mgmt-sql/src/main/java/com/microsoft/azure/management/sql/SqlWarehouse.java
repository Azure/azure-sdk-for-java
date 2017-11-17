/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;

/**
 * An immutable client-side representation of an Azure SQL Warehouse.
 */
@Fluent
public interface SqlWarehouse extends
        SqlDatabase {
    /**
     * Pause an Azure SQL Data Warehouse database.
     */
    void pauseDataWarehouse();

    /**
     * Pause an Azure SQL Data Warehouse database asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable pauseDataWarehouseAsync();

    /**
     * Pause an Azure SQL Data Warehouse database asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> pauseDataWarehouseAsync(ServiceCallback<Void> callback);

    /**
     * Resume an Azure SQL Data Warehouse database.
     */
    void resumeDataWarehouse();

    /**
     * Resume an Azure SQL Data Warehouse database asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Completable resumeDataWarehouseAsync();

    /**
     * Resume an Azure SQL Data Warehouse database asynchronously.
     *
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> resumeDataWarehouseAsync(ServiceCallback<Void> callback);
 }

