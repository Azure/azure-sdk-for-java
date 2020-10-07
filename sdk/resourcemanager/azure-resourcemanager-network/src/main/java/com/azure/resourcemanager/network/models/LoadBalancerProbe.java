// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ProbeInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** A client-side representation of a load balancing probe. */
@Fluent()
public interface LoadBalancerProbe
    extends HasInnerModel<ProbeInner>,
        ChildResource<LoadBalancer>,
        HasLoadBalancingRules,
        HasProtocol<ProbeProtocol>,
        HasPort {

    /** @return number of seconds between probes */
    int intervalInSeconds();

    /** @return number of failed probes before the node is determined to be unhealthy */
    int numberOfProbes();
}
