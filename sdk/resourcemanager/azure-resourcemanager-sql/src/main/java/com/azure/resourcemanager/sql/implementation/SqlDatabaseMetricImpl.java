// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.MetricValue;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetric;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricValue;
import com.azure.resourcemanager.sql.models.UnitType;
import com.azure.resourcemanager.sql.fluent.models.MetricInner;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Response containing the SQL database metrics. */
public class SqlDatabaseMetricImpl extends WrapperImpl<MetricInner> implements SqlDatabaseMetric {
    protected SqlDatabaseMetricImpl(MetricInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.innerModel().name().value();
    }

    @Override
    public OffsetDateTime startTime() {
        return this.innerModel().startTime();
    }

    @Override
    public OffsetDateTime endTime() {
        return this.innerModel().endTime();
    }

    @Override
    public String timeGrain() {
        return this.innerModel().timeGrain();
    }

    @Override
    public UnitType unit() {
        return this.innerModel().unit();
    }

    @Override
    public List<SqlDatabaseMetricValue> metricValues() {
        List<SqlDatabaseMetricValue> sqlDatabaseMetricValues = new ArrayList<>();
        if (this.innerModel().metricValues() != null) {
            for (MetricValue metricValue : this.innerModel().metricValues()) {
                sqlDatabaseMetricValues.add(new SqlDatabaseMetricValueImpl(metricValue));
            }
        }
        return Collections.unmodifiableList(sqlDatabaseMetricValues);
    }
}
