/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.UsageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure compute resource usage info object.
 */
@Fluent
public interface NetworkUsage extends Wrapper<UsageInner> {
    /**
     * @return the unit of measurement.
     */
    NetworkUsageUnit unit();

    /**
     * @return the current count of the allocated resources in the subscription
     */
    long currentValue();

    /**
     * @return the maximum count of the resources that can be allocated in the
     * subscription
     */
    long limit();

    /**
     * @return the name of the type of usage
     */
    UsageName name();
}
