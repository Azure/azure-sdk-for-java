/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.HasLoadBalancingRules;
import com.azure.management.network.models.HasPort;
import com.azure.management.network.models.HasProtocol;
import com.azure.management.network.models.ProbeInner;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of a load balancing probe.
 */
@Fluent()
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
