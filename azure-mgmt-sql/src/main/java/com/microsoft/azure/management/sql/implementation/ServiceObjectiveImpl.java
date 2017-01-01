/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.ServiceObjective;

/**
 * Implementation for Azure SQL Server's Service Objective.
 */
@LangDefinition
class ServiceObjectiveImpl
        extends WrapperImpl<ServiceObjectiveInner>
        implements ServiceObjective {
    private final ResourceId resourceId;
    private final ServersInner serversInner;

    protected ServiceObjectiveImpl(ServiceObjectiveInner innerObject, ServersInner serversInner) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
        this.serversInner = serversInner;
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
        return this.resourceId.resourceGroupName();
    }

    @Override
    public String sqlServerName() {
        return this.resourceId.parent().name();
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
    public String description() {
        return this.inner().description();
    }

    @Override
    public boolean enabled() {
        return this.inner().enabled();
    }

    @Override
    public ServiceObjective refresh() {
        this.setInner(this.serversInner.getServiceObjective(this.resourceGroupName(), this.sqlServerName(), this.name()));
        return this;
    }
}
