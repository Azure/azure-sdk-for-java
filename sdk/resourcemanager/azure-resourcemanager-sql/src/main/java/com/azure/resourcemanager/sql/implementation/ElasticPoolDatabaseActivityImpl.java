// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.sql.models.ElasticPoolDatabaseActivity;
import com.azure.resourcemanager.sql.fluent.inner.ElasticPoolDatabaseActivityInner;
import java.time.OffsetDateTime;

/** Implementation for Elastic Pool Database Activity interface. */
class ElasticPoolDatabaseActivityImpl extends WrapperImpl<ElasticPoolDatabaseActivityInner>
    implements ElasticPoolDatabaseActivity {
    private final ResourceId resourceId;

    protected ElasticPoolDatabaseActivityImpl(ElasticPoolDatabaseActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
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
    public String databaseName() {
        return this.inner().databaseName();
    }

    @Override
    public OffsetDateTime endTime() {
        return this.inner().endTime();
    }

    @Override
    public int errorCode() {
        return Utils.toPrimitiveInt(this.inner().errorCode());
    }

    @Override
    public String errorMessage() {
        return this.inner().errorMessage();
    }

    @Override
    public int errorSeverity() {
        return Utils.toPrimitiveInt(this.inner().errorSeverity());
    }

    @Override
    public String operation() {
        return this.inner().operation();
    }

    @Override
    public String operationId() {
        return this.inner().operationId().toString();
    }

    @Override
    public int percentComplete() {
        return Utils.toPrimitiveInt(this.inner().percentComplete());
    }

    @Override
    public String requestedElasticPoolName() {
        return this.inner().requestedElasticPoolName();
    }

    @Override
    public String currentElasticPoolName() {
        return this.inner().currentElasticPoolName();
    }

    @Override
    public String currentServiceObjective() {
        return this.inner().currentServiceObjective();
    }

    @Override
    public String requestedServiceObjective() {
        return this.inner().requestedServiceObjective();
    }

    @Override
    public String serverName() {
        return this.inner().serverName();
    }

    @Override
    public OffsetDateTime startTime() {
        return this.inner().startTime();
    }

    @Override
    public String state() {
        return this.inner().state();
    }

    @Override
    public String location() {
        return this.inner().location();
    }
}
