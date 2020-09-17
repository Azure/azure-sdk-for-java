// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.models.ServiceObjective;
import com.azure.resourcemanager.sql.fluent.inner.ServiceObjectiveInner;
import reactor.core.publisher.Mono;

/** Implementation for Azure SQL Server's Service Objective. */
class ServiceObjectiveImpl extends RefreshableWrapperImpl<ServiceObjectiveInner, ServiceObjective>
    implements ServiceObjective {
    private final SqlServerImpl sqlServer;

    protected ServiceObjectiveImpl(ServiceObjectiveInner innerObject, SqlServerImpl sqlServer) {
        super(innerObject);
        this.sqlServer = sqlServer;
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String resourceGroupName() {
        return this.sqlServer.resourceGroupName();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServer.name();
    }

    @Override
    public String serviceObjectiveName() {
        return this.inner().serviceObjectiveName();
    }

    @Override
    public boolean isDefault() {
        return this.inner().isDefault();
    }

    @Override
    public boolean isSystem() {
        return this.inner().isSystem();
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    protected Mono<ServiceObjectiveInner> getInnerAsync() {
        return this
            .sqlServer
            .manager()
            .inner()
            .getServiceObjectives()
            .getAsync(this.resourceGroupName(), this.sqlServerName(), this.name());
    }
}
