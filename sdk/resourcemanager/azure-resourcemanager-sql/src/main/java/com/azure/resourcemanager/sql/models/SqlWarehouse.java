// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Warehouse. */
@Fluent
public interface SqlWarehouse extends SqlDatabase {
    /** Pause an Azure SQL Data Warehouse database. */
    void pauseDataWarehouse();

    /**
     * Pause an Azure SQL Data Warehouse database asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> pauseDataWarehouseAsync();

    /** Resume an Azure SQL Data Warehouse database. */
    void resumeDataWarehouse();

    /**
     * Resume an Azure SQL Data Warehouse database asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> resumeDataWarehouseAsync();
}
