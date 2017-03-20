/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ProbeInner;
import com.microsoft.azure.management.network.model.HasLoadBalancingRules;
import com.microsoft.azure.management.network.model.HasPort;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of a load balancing probe.
 */
@Fluent()
@Beta
public interface LoadBalancerProbe extends
    HasInner<ProbeInner>,
    ChildResource<LoadBalancer>,
    HasLoadBalancingRules,
    HasProtocol<ProbeProtocol>,
    HasPort {

    /**
     * @return number of seconds between probes
     */
    int intervalInSeconds();

    /**
     * @return number of failed probes before the node is determined to be unhealthy
     */
    int numberOfProbes();
}
