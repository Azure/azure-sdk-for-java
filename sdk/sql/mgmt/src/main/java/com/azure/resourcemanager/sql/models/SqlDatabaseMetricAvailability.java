// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Response containing the Azure SQL Database metric availability. */
@Fluent
public interface SqlDatabaseMetricAvailability extends HasInner<MetricAvailability> {
    /** @return the length of retention for the database metric */
    String retention();

    /** @return the granularity of the database metric */
    String timeGrain();
}
