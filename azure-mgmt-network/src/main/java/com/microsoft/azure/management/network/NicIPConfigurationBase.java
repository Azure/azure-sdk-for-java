/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;

import java.util.Collection;
import java.util.List;

/**
 * The base IP configuration shared across IP configurations in regular and virtual machine scale set
 * network interface.
 */
@Fluent
public interface NicIPConfigurationBase extends HasSubnet, HasPrivateIPAddress {
    /**
     * @return true if this is the primary IP configuration
     */
    boolean isPrimary();

    /**
     * @return the virtual network associated with this IP configuration
     */
    Network getNetwork();

    /**
     * @return the network security group, if any, associated with the subnet, if any, assigned to this network interface IP configuration
     * <p>
     * (Note that this results in additional calls to Azure.)
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * @return private IP address version
     */
    IPVersion privateIPAddressVersion();

    /**
     * @return the load balancer backends associated with this network interface IP configuration
     */
    // TODO: This should be a Collection
    List<LoadBalancerBackend> listAssociatedLoadBalancerBackends();

    /**
     * @return the load balancer inbound NAT rules associated with this network interface IP configuration
     */
    // TODO This should be a Collection
    List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules();

    /**
     * @return the application gateway backends associated with this network IP configuration
     */
    @Beta(SinceVersion.V1_2_0)
    Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends();
}
