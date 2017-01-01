/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An IP configuration in a network interface associated with a virtual machine
 * scale set.
 */
@Fluent
public interface VirtualMachineScaleSetNicIpConfiguration extends
        Wrapper<NetworkInterfaceIPConfigurationInner>,
        ChildResource<VirtualMachineScaleSetNetworkInterface>,
        HasPrivateIpAddress,
        HasSubnet {
    /**
     * @return true if this is the primary ip configuration
     */
    boolean isPrimary();
    /**
     * @return the virtual network associated with the Ip configuration
     */
    Network getNetwork();

    /**
     * @return private Ip address version
     */
    IPVersion privateIpAddressVersion();

    /**
     * @return the load balancer backends associated with the network interface Ip configuration
     */
    List<LoadBalancerBackend> listAssociatedLoadBalancerBackends();

    /**
     * @return the load balancer inbound NAT rules associated with this network interface Ip configuration
     */
    List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules();
}