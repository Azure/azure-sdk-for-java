/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.SqlDatabaseUsageMetric;
import com.azure.management.sql.models.DatabaseUsageInner;

import java.time.OffsetDateTime;

/**
 * Implementation for Azure SQL Database usage.
 */
public class SqlDatabaseUsageMetricImpl
    extends WrapperImpl<DatabaseUsageInner>
    implements SqlDatabaseUsageMetric {

    protected SqlDatabaseUsageMetricImpl(DatabaseUsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String resourceName() {
        return this.inner().resourceName();
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
    public OffsetDateTime nextResetTime() {
        return this.inner().nextResetTime();
    }
}
