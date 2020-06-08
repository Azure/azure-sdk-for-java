// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlSubscriptionUsageMetric;
import com.azure.resourcemanager.sql.fluent.inner.SubscriptionUsageInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for Azure SQL subscription usage. */
public class SqlSubscriptionUsageMetricImpl
    extends RefreshableWrapperImpl<SubscriptionUsageInner, SqlSubscriptionUsageMetric>
    implements SqlSubscriptionUsageMetric {

    private final SqlServerManager sqlServerManager;
    private final String location;

    protected SqlSubscriptionUsageMetricImpl(
        String location, SubscriptionUsageInner innerObject, SqlServerManager sqlServerManager) {
        super(innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.location = location;
    }

    @Override
    protected Mono<SubscriptionUsageInner> getInnerAsync() {
        return this.sqlServerManager.inner().getSubscriptionUsages().getAsync(this.location, this.name());
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
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public double currentValue() {
        return this.inner().currentValue() != null ? this.inner().currentValue() : 0;
    }

    @Override
    public double limit() {
        return this.inner().limit() != null ? this.inner().limit() : 0;
    }

    @Override
    public String unit() {
        return this.inner().unit();
    }

    @Override
    public String type() {
        return this.inner().type();
    }
}
