/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.FrontendIPConfigurationInner;
import com.azure.management.network.models.HasLoadBalancingRules;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.Map;

/**
 * An client-side representation of a load balancer frontend.
 */
@Fluent()
public interface LoadBalancerFrontend extends
        HasInner<FrontendIPConfigurationInner>,
        ChildResource<LoadBalancer>,
        HasLoadBalancingRules {

    /**
     * @return true if the frontend is public, i.e. it has a public IP address associated with it
     */
    boolean isPublic();

    /**
     * @return the inbound NAT pools on this load balancer that use this frontend, indexed by their names
     */
    Map<String, LoadBalancerInboundNatPool> inboundNatPools();

    /**
     * @return the inbound NAT rules on this load balancer that use this frontend, indexed by their names
     */
    Map<String, LoadBalancerInboundNatRule> inboundNatRules();
}
