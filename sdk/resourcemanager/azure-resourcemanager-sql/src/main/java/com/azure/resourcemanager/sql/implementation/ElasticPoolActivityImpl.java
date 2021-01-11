// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.sql.models.ElasticPoolActivity;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolActivityInner;
import java.time.OffsetDateTime;

/** Implementation for Elastic Pool Activity interface. */
class ElasticPoolActivityImpl extends WrapperImpl<ElasticPoolActivityInner> implements ElasticPoolActivity {
    private final ResourceId resourceId;

    protected ElasticPoolActivityImpl(ElasticPoolActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.innerModel().id());
    }

    @Override
    public String name() {
        return this.resourceId.name();
    }

    @Override
    public String id() {
        return this.resourceId.id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceId.resourceGroupName();
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
    public int requestedDatabaseDtuMax() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().requestedDatabaseDtuMax());
    }

    @Override
    public int requestedDatabaseDtuMin() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().requestedDatabaseDtuMin());
    }

    @Override
    public int requestedDtu() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().requestedDtu());
    }

    @Override
    public String requestedElasticPoolName() {
        return this.innerModel().requestedElasticPoolName();
    }

    @Override
    public long requestedStorageLimitInGB() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().requestedStorageLimitInGB());
    }

    @Override
    public String elasticPoolName() {
        return this.innerModel().elasticPoolName();
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

    @Override
    public int requestedStorageLimitInMB() {
        return this.innerModel().requestedStorageLimitInMB();
    }

    @Override
    public int requestedDatabaseDtuGuarantee() {
        return this.innerModel().requestedDatabaseDtuGuarantee();
    }

    @Override
    public int requestedDatabaseDtuCap() {
        return this.innerModel().requestedDatabaseDtuCap();
    }

    @Override
    public int requestedDtuGuarantee() {
        return this.innerModel().requestedDtuGuarantee();
    }
}
