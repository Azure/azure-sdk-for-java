/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.sql.models.SubscriptionUsageInner;

/**
 * The result of SQL server usages per current subscription.
 */
@Fluent
public interface SqlSubscriptionUsageMetric extends
        Refreshable<SqlSubscriptionUsageMetric>,
        HasId,
        HasName,
        HasInner<SubscriptionUsageInner> {

    /**
     * @return a user-readable name of the metric
     */
    String displayName();

    /**
     * @return the current value of the metric
     */
    double currentValue();

    /**
     * @return the boundary value of the metric
     */
    double limit();

    /**
     * @return the unit of the metric
     */
    String unit();

    /**
     * @return the resource type
     */
    String type();
}
