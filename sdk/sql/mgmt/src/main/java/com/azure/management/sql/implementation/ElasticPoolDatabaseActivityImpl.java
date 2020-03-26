/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.management.sql.ElasticPoolDatabaseActivity;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.sql.models.ElasticPoolDatabaseActivityInner;

import java.time.OffsetDateTime;

/**
 * Implementation for Elastic Pool Database Activity interface.
 */
class ElasticPoolDatabaseActivityImpl
        extends WrapperImpl<ElasticPoolDatabaseActivityInner>
        implements ElasticPoolDatabaseActivity {
    private final ResourceId resourceId;

    protected ElasticPoolDatabaseActivityImpl(ElasticPoolDatabaseActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().getId());
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
