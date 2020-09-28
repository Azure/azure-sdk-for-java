// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.ServerMetric;
import com.azure.resourcemanager.sql.fluent.models.ServerUsageInner;
import java.time.OffsetDateTime;

/** Implementation for DatabaseMetric interface. */
class ServerMetricImpl extends WrapperImpl<ServerUsageInner> implements ServerMetric {

    protected ServerMetricImpl(ServerUsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String resourceName() {
        return this.innerModel().resourceName();
    }

    @Override
    public String displayName() {
        return this.innerModel().displayName();
    }

    @Override
    public double currentValue() {
        return this.innerModel().currentValue();
    }

    @Override
    public double limit() {
        return this.innerModel().limit();
    }

    @Override
    public String unit() {
        return this.innerModel().unit();
    }

    @Override
    public OffsetDateTime nextResetTime() {
        return this.innerModel().nextResetTime();
    }
}
