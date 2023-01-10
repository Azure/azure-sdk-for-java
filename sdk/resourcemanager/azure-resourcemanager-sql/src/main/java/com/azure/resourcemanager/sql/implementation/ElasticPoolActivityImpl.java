// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolOperationInner;
import com.azure.resourcemanager.sql.models.ElasticPoolActivity;
import java.time.OffsetDateTime;

/** Implementation for Elastic Pool Activity interface. */
class ElasticPoolActivityImpl extends WrapperImpl<ElasticPoolOperationInner> implements ElasticPoolActivity {
    private final ResourceId resourceId;

    protected ElasticPoolActivityImpl(ElasticPoolOperationInner innerObject) {
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
        return this.innerModel().estimatedCompletionTime();
    }

    @Override
    public int errorCode() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().errorCode());
    }

    @Override
    public String errorMessage() {
        return this.innerModel().errorDescription();
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
        return this.innerModel().id();
    }

    @Override
    public int percentComplete() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().percentComplete());
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
}
