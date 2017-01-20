/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.List;

/**
 * The base ip configuration shared across ip configurations in regular and virtual machine scale set
 * network interface.
 */
@Fluent
public interface NicIpConfigurationBase {
    /**
     * @return true if this is the primary ip configuration
     */
    boolean isPrimary();

    /**
     * @return the virtual network associated with this IP configuration
     */
    Network getNetwork();

    /**
     * @return private IP address version
     */
    IPVersion privateIpAddressVersion();

    /**
     * @return the load balancer backends associated with this network interface IP configuration
     */
    List<LoadBalancerBackend> listAssociatedLoadBalancerBackends();

    /**
     * @return the load balancer inbound NAT rules associated with this network interface IP configuration
     */
    List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules();
}
