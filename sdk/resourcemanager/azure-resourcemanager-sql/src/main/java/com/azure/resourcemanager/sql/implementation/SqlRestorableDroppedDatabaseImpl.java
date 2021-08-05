// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlRestorableDroppedDatabase;
import com.azure.resourcemanager.sql.fluent.models.RestorableDroppedDatabaseInner;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;

/** Implementation for SQL restorable dropped database interface. */
public class SqlRestorableDroppedDatabaseImpl
    extends RefreshableWrapperImpl<RestorableDroppedDatabaseInner, SqlRestorableDroppedDatabase>
    implements SqlRestorableDroppedDatabase {

    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;

    protected SqlRestorableDroppedDatabaseImpl(
        String resourceGroupName,
        String sqlServerName,
        RestorableDroppedDatabaseInner innerObject,
        SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().location());
    }

    @Override
    public String databaseName() {
        return this.innerModel().databaseName();
    }

    @Override
    public String edition() {
        return this.innerModel().edition();
    }

    @Override
    public String maxSizeBytes() {
        return this.innerModel().maxSizeBytes();
    }

    @Override
    public String serviceLevelObjective() {
        return this.innerModel().serviceLevelObjective();
    }

    @Override
    public String elasticPoolName() {
        return this.innerModel().elasticPoolName();
    }

    @Override
    public OffsetDateTime creationDate() {
        return this.innerModel().creationDate();
    }

    @Override
    public OffsetDateTime deletionDate() {
        return this.innerModel().deletionDate();
    }

    @Override
    public OffsetDateTime earliestRestoreDate() {
        return this.innerModel().earliestRestoreDate();
    }

    @Override
    protected Mono<RestorableDroppedDatabaseInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getRestorableDroppedDatabases()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.innerModel().id());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }
}
