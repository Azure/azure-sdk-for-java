// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import java.util.Collection;
import java.util.List;

/**
 * The base IP configuration shared across IP configurations in regular and virtual machine scale set network interface.
 */
@Fluent
public interface NicIpConfigurationBase extends HasSubnet, HasPrivateIpAddress {
    /** @return true if this is the primary IP configuration */
    boolean isPrimary();

    /** @return the virtual network associated with this IP configuration */
    Network getNetwork();

    /**
     * @return the network security group, if any, associated with the subnet, if any, assigned to this network
     *     interface IP configuration
     *     <p>(Note that this results in additional calls to Azure.)
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /** @return private IP address version */
    IpVersion privateIpAddressVersion();

    /** @return the load balancer backends associated with this network interface IP configuration */
    // TODO: This should be a Collection
    List<LoadBalancerBackend> listAssociatedLoadBalancerBackends();

    /** @return the load balancer inbound NAT rules associated with this network interface IP configuration */
    // TODO: This should be a Collection
    List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules();

    /** @return the application gateway backends associated with this network IP configuration */
    Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends();

    /**
     * @return the application security groups associated with this network IP configuration
     */
    List<ApplicationSecurityGroup> listAssociatedApplicationSecurityGroups();
}
