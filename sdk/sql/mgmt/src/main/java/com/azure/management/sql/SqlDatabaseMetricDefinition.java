/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.MetricDefinitionInner;

import java.util.List;

/**
 * Response containing the Azure SQL Database metric definition.
 */
@Fluent
public interface SqlDatabaseMetricDefinition extends HasInner<MetricDefinitionInner> {
    /**
     * @return the name of the metric
     */
    String name();

    /**
     * @return the primary aggregation type
     */
    PrimaryAggregationType primaryAggregationType();

    /**
     * @return the resource URI
     */
    String resourceUri();

    /**
     * @return the unit type
     */
    UnitDefinitionType unit();

    /**
     * @return the metric availabilities
     */
    List<SqlDatabaseMetricAvailability> metricAvailabilities();
}
