// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure compute resource usage info object. */
@Fluent
public interface NetworkUsage extends HasInnerModel<UsageInner> {
    /**
     * Gets the unit of measurement.
     *
     * @return the unit of measurement.
     */
    UsageUnit unit();

    /**
     * Gets the current count of the allocated resources in the subscription.
     *
     * @return the current count of the allocated resources in the subscription
     */
    long currentValue();

    /**
     * Gets the maximum count of the resources that can be allocated in the subscription.
     *
     * @return the maximum count of the resources that can be allocated in the subscription
     */
    long limit();

    /**
     * Gets the name of the type of usage.
     *
     * @return the name of the type of usage
     */
    UsageName name();
}
