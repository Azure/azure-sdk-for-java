// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.FrontendIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.Map;

/** An client-side representation of a load balancer frontend. */
@Fluent()
public interface LoadBalancerFrontend
    extends HasInnerModel<FrontendIpConfigurationInner>, ChildResource<LoadBalancer>, HasLoadBalancingRules {

    /** @return true if the frontend is public, i.e. it has a public IP address associated with it */
    boolean isPublic();

    /** @return the inbound NAT pools on this load balancer that use this frontend, indexed by their names */
    Map<String, LoadBalancerInboundNatPool> inboundNatPools();

    /** @return the inbound NAT rules on this load balancer that use this frontend, indexed by their names */
    Map<String, LoadBalancerInboundNatRule> inboundNatRules();

    /** @return the outbound rules on this load balancer that use this frontend, indexed by their names */
    Map<String, LoadBalancerOutboundRule> outboundRules();
}
