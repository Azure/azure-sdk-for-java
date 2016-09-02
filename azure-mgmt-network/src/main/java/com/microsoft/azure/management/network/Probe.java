/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.ProbeInner;
import com.microsoft.azure.management.network.model.HasLoadBalancingRules;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of a load balancing probe.
 */
public interface Probe extends
    Wrapper<ProbeInner>,
    ChildResource,
    HasLoadBalancingRules,
    HasProtocol<ProbeProtocol> {

    /**
     * @return the port number the probe is monitoring
     */
    int port();

    /**
     * @return number of seconds between probes
     */
    int intervalInSeconds();

    /**
     * @return number of failed probes before the node is determined to be unhealthy
     */
    int numberOfProbes();
}
