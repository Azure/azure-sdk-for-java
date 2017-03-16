/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.FrontendIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasLoadBalancingRules;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of a load balancer frontend.
 */
@Fluent()
@Beta
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
