/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.MetricAvailability;
import com.azure.management.sql.PrimaryAggregationType;
import com.azure.management.sql.SqlDatabaseMetricAvailability;
import com.azure.management.sql.SqlDatabaseMetricDefinition;
import com.azure.management.sql.UnitDefinitionType;
import com.azure.management.sql.models.MetricDefinitionInner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response containing the SQL database metric definitions.
 */
public class SqlDatabaseMetricDefinitionImpl extends WrapperImpl<MetricDefinitionInner> implements SqlDatabaseMetricDefinition {
    protected SqlDatabaseMetricDefinitionImpl(MetricDefinitionInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return this.inner().name().value();
    }

    @Override
    public PrimaryAggregationType primaryAggregationType() {
        return this.inner().primaryAggregationType();
    }

    @Override
    public String resourceUri() {
        return this.inner().resourceUri();
    }

    @Override
    public UnitDefinitionType unit() {
        return this.inner().unit();
    }

    @Override
    public List<SqlDatabaseMetricAvailability> metricAvailabilities() {
        List<SqlDatabaseMetricAvailability> sqlDatabaseMetricAvailabilities = new ArrayList<>();
        if (this.inner().metricAvailabilities() != null) {
            for (MetricAvailability metricAvailability : this.inner().metricAvailabilities()) {
                sqlDatabaseMetricAvailabilities.add(new SqlDatabaseMetricAvailabilityImpl(metricAvailability));
            }
        }
        return Collections.unmodifiableList(sqlDatabaseMetricAvailabilities);
    }
}
