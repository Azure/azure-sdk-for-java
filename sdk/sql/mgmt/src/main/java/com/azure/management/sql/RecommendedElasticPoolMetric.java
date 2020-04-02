/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.RecommendedElasticPoolMetricInner;

import java.time.OffsetDateTime;


/**
 * An immutable client-side representation of an Azure SQL Replication link.
 */
@Fluent
public interface RecommendedElasticPoolMetric extends
        HasInner<RecommendedElasticPoolMetricInner> {
    /**
     * @return the time of metric (ISO8601 format).
     */
    OffsetDateTime dateTime();

    /**
     * @return the DTUs (Database Transaction Units)
     * See  https://azure.microsoft.com/en-us/documentation/articles/sql-database-what-is-a-dtu/
     */
    double dtu();

    /**
     * @return the size in gigabytes.
     */
    double sizeGB();
}

