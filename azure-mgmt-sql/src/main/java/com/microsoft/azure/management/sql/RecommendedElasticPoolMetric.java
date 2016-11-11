/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.RecommendedElasticPoolMetricInner;
import org.joda.time.DateTime;


/**
 * An immutable client-side representation of an Azure SQL Replication link.
 */
@Fluent
public interface RecommendedElasticPoolMetric extends
        Wrapper<RecommendedElasticPoolMetricInner> {
    /**
     * @return the time of metric (ISO8601 format).
     */
    DateTime dateTimeProperty();

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

