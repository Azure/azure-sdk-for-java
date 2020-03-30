/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.MetricValue;
import com.azure.management.sql.SqlDatabaseMetric;
import com.azure.management.sql.SqlDatabaseMetricValue;
import com.azure.management.sql.UnitType;
import com.azure.management.sql.models.MetricInner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response containing the SQL database metrics.
 */
public class SqlDatabaseMetricImpl extends WrapperImpl<MetricInner> implements SqlDatabaseMetric {
    protected SqlDatabaseMetricImpl(MetricInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.inner().name().value();
    }

    @Override
    public OffsetDateTime startTime() {
        return this.inner().startTime();
    }

    @Override
    public OffsetDateTime endTime() {
        return this.inner().endTime();
    }

    @Override
    public String timeGrain() {
        return this.inner().timeGrain();
    }

    @Override
    public UnitType unit() {
        return this.inner().unit();
    }

    @Override
    public List<SqlDatabaseMetricValue> metricValues() {
        List<SqlDatabaseMetricValue> sqlDatabaseMetricValues = new ArrayList<>();
        if (this.inner().metricValues() != null) {
            for (MetricValue metricValue : this.inner().metricValues()) {
                sqlDatabaseMetricValues.add(new SqlDatabaseMetricValueImpl(metricValue));
            }
        }
        return Collections.unmodifiableList(sqlDatabaseMetricValues);
    }
}
