/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.sql.SqlWarehouse;

/**
 * Implementation for SqlWarehouse and its parent interfaces.
 */
@LangDefinition
class SqlWarehouseImpl
        extends SqlDatabaseImpl
        implements SqlWarehouse {

    protected SqlWarehouseImpl(String name, DatabaseInner innerObject, DatabasesInner innerCollection, SqlServerManager manager) {
        super(name, innerObject, innerCollection, manager);
    }

    @Override
    public void pauseDataWarehouse() {
        this.innerCollection.pauseDataWarehouse(this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public void resumeDataWarehouse() {
        this.innerCollection.resumeDataWarehouse(this.resourceGroupName(), this.sqlServerName(), this.name());
    }
}
