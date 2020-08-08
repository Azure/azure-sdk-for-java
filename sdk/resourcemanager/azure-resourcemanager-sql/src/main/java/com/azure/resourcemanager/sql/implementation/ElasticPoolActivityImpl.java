// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.sql.models.ElasticPoolActivity;
import com.azure.resourcemanager.sql.fluent.inner.ElasticPoolActivityInner;
import java.time.OffsetDateTime;

/** Implementation for Elastic Pool Activity interface. */
class ElasticPoolActivityImpl extends WrapperImpl<ElasticPoolActivityInner> implements ElasticPoolActivity {
    private final ResourceId resourceId;

    protected ElasticPoolActivityImpl(ElasticPoolActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
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
    public int requestedDatabaseDtuMax() {
        return Utils.toPrimitiveInt(this.inner().requestedDatabaseDtuMax());
    }

    @Override
    public int requestedDatabaseDtuMin() {
        return Utils.toPrimitiveInt(this.inner().requestedDatabaseDtuMin());
    }

    @Override
    public int requestedDtu() {
        return Utils.toPrimitiveInt(this.inner().requestedDtu());
    }

    @Override
    public String requestedElasticPoolName() {
        return this.inner().requestedElasticPoolName();
    }

    @Override
    public long requestedStorageLimitInGB() {
        return Utils.toPrimitiveLong(this.inner().requestedStorageLimitInGB());
    }

    @Override
    public String elasticPoolName() {
        return this.inner().elasticPoolName();
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

    @Override
    public int requestedStorageLimitInMB() {
        return this.inner().requestedStorageLimitInMB();
    }

    @Override
    public int requestedDatabaseDtuGuarantee() {
        return this.inner().requestedDatabaseDtuGuarantee();
    }

    @Override
    public int requestedDatabaseDtuCap() {
        return this.inner().requestedDatabaseDtuCap();
    }

    @Override
    public int requestedDtuGuarantee() {
        return this.inner().requestedDtuGuarantee();
    }
}
