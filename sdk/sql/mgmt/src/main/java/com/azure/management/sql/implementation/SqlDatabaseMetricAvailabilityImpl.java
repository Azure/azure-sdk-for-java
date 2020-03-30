/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.sql.SqlDatabaseMetricAvailability;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.MetricAvailability;

/**
 * Response containing the SQL database metric availability.
 */
public class SqlDatabaseMetricAvailabilityImpl extends WrapperImpl<MetricAvailability> implements SqlDatabaseMetricAvailability {
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
