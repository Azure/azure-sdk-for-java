// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseUsageInner;
import java.time.OffsetDateTime;

/** The result of SQL server usages per SQL Database. */
@Fluent
public interface SqlDatabaseUsageMetric extends HasName, HasInner<DatabaseUsageInner> {

    /** @return the name of the SQL Database resource */
    String resourceName();

    /** @return a user-readable name of the metric */
    String displayName();

    /** @return the current value of the metric */
    double currentValue();

    /** @return the boundary value of the metric */
    double limit();

    /** @return the unit of the metric */
    String unit();

    /** @return the next reset time for the usage metric (ISO8601 format) */
    OffsetDateTime nextResetTime();
}
