// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlDatabaseUsageMetric;
import com.azure.resourcemanager.sql.fluent.models.DatabaseUsageInner;
import java.time.OffsetDateTime;

/** Implementation for Azure SQL Database usage. */
public class SqlDatabaseUsageMetricImpl extends WrapperImpl<DatabaseUsageInner> implements SqlDatabaseUsageMetric {

    protected SqlDatabaseUsageMetricImpl(DatabaseUsageInner innerObject) {
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
        return this.innerModel().currentValue() != null ? this.innerModel().currentValue() : 0;
    }

    @Override
    public double limit() {
        return this.innerModel().limit() != null ? this.innerModel().limit() : 0;
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
