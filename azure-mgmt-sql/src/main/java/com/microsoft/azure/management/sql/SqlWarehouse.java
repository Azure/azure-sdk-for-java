/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;

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
     * Resume an Azure SQL Data Warehouse database.
     */
    void resumeDataWarehouse();
 }

