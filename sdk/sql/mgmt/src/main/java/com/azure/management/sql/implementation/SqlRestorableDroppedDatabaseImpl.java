/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;


import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.management.sql.SqlRestorableDroppedDatabase;
import com.azure.management.sql.models.RestorableDroppedDatabaseInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Implementation for SQL restorable dropped database interface.
 */
public class SqlRestorableDroppedDatabaseImpl extends RefreshableWrapperImpl<RestorableDroppedDatabaseInner, SqlRestorableDroppedDatabase>
    implements SqlRestorableDroppedDatabase {

    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;

    protected SqlRestorableDroppedDatabaseImpl(String resourceGroupName, String sqlServerName,  RestorableDroppedDatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public Region region() {
        return Region.fromName(this.inner().location());
    }

    @Override
    public String databaseName() {
        return this.inner().databaseName();
    }

    @Override
    public String edition() {
        return this.inner().edition();
    }

    @Override
    public String maxSizeBytes() {
        return this.inner().maxSizeBytes();
    }

    @Override
    public String serviceLevelObjective() {
        return this.inner().serviceLevelObjective();
    }

    @Override
    public String elasticPoolName() {
        return this.inner().elasticPoolName();
    }

    @Override
    public OffsetDateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public OffsetDateTime deletionDate() {
        return this.inner().deletionDate();
    }

    @Override
    public OffsetDateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }

    @Override
    protected Mono<RestorableDroppedDatabaseInner> getInnerAsync() {
        return this.sqlServerManager.inner().restorableDroppedDatabases().getAsync(this.resourceGroupName, this.sqlServerName, this.inner().getId());
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }
}
