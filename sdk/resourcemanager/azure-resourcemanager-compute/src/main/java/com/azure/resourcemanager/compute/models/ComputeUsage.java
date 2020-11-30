// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure compute resource usage info object. */
@Fluent
public interface ComputeUsage extends HasInnerModel<UsageInner> {
    /** @return the unit of measurement */
    ComputeUsageUnit unit();

    /** @return the current count of the allocated resources in the subscription */
    int currentValue();

    /** @return the maximum count of the resources that can be allocated in the subscription */
    long limit();

    /** @return the name of the type of usage */
    UsageName name();
}
