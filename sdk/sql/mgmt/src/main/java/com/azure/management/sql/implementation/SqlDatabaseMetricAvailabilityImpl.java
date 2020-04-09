// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.MetricAvailability;
import com.azure.management.sql.SqlDatabaseMetricAvailability;

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
