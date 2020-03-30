/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;


import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.MetricValue;
import com.azure.management.sql.SqlDatabaseMetricValue;

import java.time.OffsetDateTime;

/**
 * Implementation for SqlDatabaseMetricValue.
 */
public class SqlDatabaseMetricValueImpl extends WrapperImpl<MetricValue>
    implements SqlDatabaseMetricValue {
    protected SqlDatabaseMetricValueImpl(MetricValue innerObject) {
        super(innerObject);
    }

    @Override
    public double count() {
        return this.inner().count();
    }

    @Override
    public double average() {
        return this.inner().average();
    }

    @Override
    public double maximum() {
        return this.inner().maximum();
    }

    @Override
    public double minimum() {
        return this.inner().minimum();
    }

    @Override
    public OffsetDateTime timestamp() {
        return this.inner().timestamp();
    }

    @Override
    public double total() {
        return this.inner().total();
    }
}
