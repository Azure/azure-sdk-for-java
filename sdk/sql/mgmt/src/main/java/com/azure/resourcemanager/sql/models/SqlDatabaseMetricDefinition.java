// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.MetricDefinitionInner;
import java.util.List;

/** Response containing the Azure SQL Database metric definition. */
@Fluent
public interface SqlDatabaseMetricDefinition extends HasInner<MetricDefinitionInner> {
    /** @return the name of the metric */
    String name();

    /** @return the primary aggregation type */
    PrimaryAggregationType primaryAggregationType();

    /** @return the resource URI */
    String resourceUri();

    /** @return the unit type */
    UnitDefinitionType unit();

    /** @return the metric availabilities */
    List<SqlDatabaseMetricAvailability> metricAvailabilities();
}
