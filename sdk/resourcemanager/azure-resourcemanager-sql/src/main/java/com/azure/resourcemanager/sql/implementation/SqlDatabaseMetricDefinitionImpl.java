// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.MetricAvailability;
import com.azure.resourcemanager.sql.models.PrimaryAggregationType;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricAvailability;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricDefinition;
import com.azure.resourcemanager.sql.models.UnitDefinitionType;
import com.azure.resourcemanager.sql.fluent.models.MetricDefinitionInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Response containing the SQL database metric definitions. */
public class SqlDatabaseMetricDefinitionImpl extends WrapperImpl<MetricDefinitionInner>
    implements SqlDatabaseMetricDefinition {
    protected SqlDatabaseMetricDefinitionImpl(MetricDefinitionInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.innerModel().name().value();
    }

    @Override
    public PrimaryAggregationType primaryAggregationType() {
        return this.innerModel().primaryAggregationType();
    }

    @Override
    public String resourceUri() {
        return this.innerModel().resourceUri();
    }

    @Override
    public UnitDefinitionType unit() {
        return this.innerModel().unit();
    }

    @Override
    public List<SqlDatabaseMetricAvailability> metricAvailabilities() {
        List<SqlDatabaseMetricAvailability> sqlDatabaseMetricAvailabilities = new ArrayList<>();
        if (this.innerModel().metricAvailabilities() != null) {
            for (MetricAvailability metricAvailability : this.innerModel().metricAvailabilities()) {
                sqlDatabaseMetricAvailabilities.add(new SqlDatabaseMetricAvailabilityImpl(metricAvailability));
            }
        }
        return Collections.unmodifiableList(sqlDatabaseMetricAvailabilities);
    }
}
