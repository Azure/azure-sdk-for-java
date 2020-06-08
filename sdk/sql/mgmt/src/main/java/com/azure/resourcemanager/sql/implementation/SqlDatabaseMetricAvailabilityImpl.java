// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.MetricAvailability;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricAvailability;

/** Response containing the SQL database metric availability. */
public class SqlDatabaseMetricAvailabilityImpl extends WrapperImpl<MetricAvailability>
    implements SqlDatabaseMetricAvailability {
    protected SqlDatabaseMetricAvailabilityImpl(MetricAvailability innerObject) {
        super(innerObject);
    }

    @Override
    public String retention() {
        return this.inner().retention();
    }

    @Override
    public String timeGrain() {
        return this.inner().timeGrain();
    }
}
