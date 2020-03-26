/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;


import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.MetricInner;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response containing the Azure SQL Database metric.
 */
@Fluent
public interface SqlDatabaseMetric extends HasInner<MetricInner> {

    /**
     * @return the metric name
     */
    String name();

    /**
     * @return the start time
     */
    OffsetDateTime startTime();

    /**
     * @return the end time
     */
    OffsetDateTime endTime();

    /**
     * @return the time grain
     */
    String timeGrain();

    /**
     * @return the metric's unit type
     */
    UnitType unit();

    /**
     * @return the metric values
     */
    List<SqlDatabaseMetricValue> metricValues();
}
