// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.OffsetDateTime;

/** Response containing the Azure SQL Database metric value. */
@Fluent
public interface SqlDatabaseMetricValue extends HasInnerModel<MetricValue> {
    /** @return the number of values for the metric */
    double count();

    /** @return the average value of the metric */
    double average();

    /** @return the max value of the metric */
    double maximum();

    /** @return the min value of the metric */
    double minimum();

    /** @return the metric timestamp (ISO-8601 format) */
    OffsetDateTime timestamp();

    /** @return the total value of the metric */
    double total();
}
