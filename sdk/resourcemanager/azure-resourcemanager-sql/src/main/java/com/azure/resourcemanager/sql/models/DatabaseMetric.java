// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL DatabaseMetric. */
@Fluent
public interface DatabaseMetric {

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource
     */
    String resourceName();

    /**
     * Gets the metric display name.
     *
     * @return the metric display name
     */
    String displayName();

    /**
     * Gets the current value of the metric.
     *
     * @return the current value of the metric
     */
    double currentValue();

    /**
     * Gets the current limit of the metric.
     *
     * @return the current limit of the metric
     */
    double limit();

    /**
     * Gets the units of the metric.
     *
     * @return the units of the metric
     */
    String unit();

    /**
     * Gets the next reset time for the metric.
     *
     * @return the next reset time for the metric (ISO8601 format)
     */
    OffsetDateTime nextResetTime();
}
