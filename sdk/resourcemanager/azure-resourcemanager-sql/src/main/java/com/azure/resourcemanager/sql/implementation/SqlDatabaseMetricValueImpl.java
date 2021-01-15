// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.MetricValue;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricValue;
import java.time.OffsetDateTime;

/** Implementation for SqlDatabaseMetricValue. */
public class SqlDatabaseMetricValueImpl extends WrapperImpl<MetricValue> implements SqlDatabaseMetricValue {
    protected SqlDatabaseMetricValueImpl(MetricValue innerObject) {
        super(innerObject);
    }

    @Override
    public double count() {
        return this.innerModel().count();
    }

    @Override
    public double average() {
        return this.innerModel().average();
    }

    @Override
    public double maximum() {
        return this.innerModel().maximum();
    }

    @Override
    public double minimum() {
        return this.innerModel().minimum();
    }

    @Override
    public OffsetDateTime timestamp() {
        return this.innerModel().timestamp();
    }

    @Override
    public double total() {
        return this.innerModel().total();
    }
}
