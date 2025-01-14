// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.models.SubscriptionUsageInner;

/** The result of SQL server usages per current subscription. */
@Fluent
public interface SqlSubscriptionUsageMetric
    extends Refreshable<SqlSubscriptionUsageMetric>, HasId, HasName, HasInnerModel<SubscriptionUsageInner> {

    /**
     * Gets a user-readable name of the metric.
     *
     * @return a user-readable name of the metric
     */
    String displayName();

    /**
     * Gets the current value of the metric.
     *
     * @return the current value of the metric
     */
    double currentValue();

    /**
     * Gets the boundary value of the metric.
     *
     * @return the boundary value of the metric
     */
    double limit();

    /**
     * Gets the unit of the metric.
     *
     * @return the unit of the metric
     */
    String unit();

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    String type();
}
