/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.ElasticPoolActivity;
import org.joda.time.DateTime;

/**
 * Implementation for Elastic Pool Activity interface.
 */
@LangDefinition
class ElasticPoolActivityImpl
        extends WrapperImpl<ElasticPoolActivityInner>
        implements ElasticPoolActivity {
    private final ResourceId resourceId;

    protected ElasticPoolActivityImpl(ElasticPoolActivityInner innerObject) {
        super(innerObject);
        this.resourceId = ResourceId.parseResourceId(this.inner().id());
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
    public DateTime endTime() {
        return this.inner().endTime();
    }

    @Override
    public int errorCode() {
        return this.inner().errorCode();
    }

    @Override
    public String errorMessage() {
        return this.inner().errorMessage();
    }

    @Override
    public int errorSeverity() {
        return this.inner().errorSeverity();
    }

    @Override
    public String operation() {
        return this.inner().operation();
    }

    @Override
    public String operationId() {
        return this.inner().operationId();
    }

    @Override
    public int percentComplete() {
        return this.inner().percentComplete();
    }

    @Override
    public int requestedDatabaseDtuMax() {
        return this.inner().requestedDatabaseDtuMax();
    }

    @Override
    public int requestedDatabaseDtuMin() {
        return this.inner().requestedDatabaseDtuMin();
    }

    @Override
    public int requestedDtu() {
        return this.inner().requestedDtu();
    }

    @Override
    public String requestedElasticPoolName() {
        return this.inner().requestedElasticPoolName();
    }

    @Override
    public long requestedStorageLimitInGB() {
        return this.inner().requestedStorageLimitInGB();
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
    public DateTime startTime() {
        return this.inner().startTime();
    }

    @Override
    public String state() {
        return this.inner().state();
    }
}
