// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.sql.models.ElasticPoolDatabaseActivity;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolDatabaseActivityInner;
import java.time.OffsetDateTime;

/** Implementation for Elastic Pool Database Activity interface. */
class ElasticPoolDatabaseActivityImpl extends WrapperImpl<ElasticPoolDatabaseActivityInner>
    implements ElasticPoolDatabaseActivity {
    private final ResourceId resourceId;

    protected ElasticPoolDatabaseActivityImpl(ElasticPoolDatabaseActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.innerModel().id());
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
        return this.resourceId.resourceGroupName();
    }

    @Override
    public String databaseName() {
        return this.innerModel().databaseName();
    }

    @Override
    public OffsetDateTime endTime() {
        return this.innerModel().endTime();
    }

    @Override
    public int errorCode() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().errorCode());
    }

    @Override
    public String errorMessage() {
        return this.innerModel().errorMessage();
    }

    @Override
    public int errorSeverity() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().errorSeverity());
    }

    @Override
    public String operation() {
        return this.innerModel().operation();
    }

    @Override
    public String operationId() {
        return this.innerModel().operationId().toString();
    }

    @Override
    public int percentComplete() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().percentComplete());
    }

    @Override
    public String requestedElasticPoolName() {
        return this.innerModel().requestedElasticPoolName();
    }

    @Override
    public String currentElasticPoolName() {
        return this.innerModel().currentElasticPoolName();
    }

    @Override
    public String currentServiceObjective() {
        return this.innerModel().currentServiceObjective();
    }

    @Override
    public String requestedServiceObjective() {
        return this.innerModel().requestedServiceObjective();
    }

    @Override
    public String serverName() {
        return this.innerModel().serverName();
    }

    @Override
    public OffsetDateTime startTime() {
        return this.innerModel().startTime();
    }

    @Override
    public String state() {
        return this.innerModel().state();
    }

    @Override
    public String location() {
        return this.innerModel().location();
    }
}
