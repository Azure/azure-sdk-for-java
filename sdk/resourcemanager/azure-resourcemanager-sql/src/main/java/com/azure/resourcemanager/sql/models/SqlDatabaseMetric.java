// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.MetricInner;
import java.time.OffsetDateTime;
import java.util.List;

/** Response containing the Azure SQL Database metric. */
@Fluent
public interface SqlDatabaseMetric extends HasInner<MetricInner> {

    /** @return the metric name */
    String name();

    /** @return the start time */
    OffsetDateTime startTime();

    /** @return the end time */
    OffsetDateTime endTime();

    /** @return the time grain */
    String timeGrain();

    /** @return the metric's unit type */
    UnitType unit();

    /** @return the metric values */
    List<SqlDatabaseMetricValue> metricValues();
}
