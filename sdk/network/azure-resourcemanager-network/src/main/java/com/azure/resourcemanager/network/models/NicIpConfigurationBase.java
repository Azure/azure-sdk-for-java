// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import java.util.Collection;
import java.util.List;

/**
 * The base IP configuration shared across IP configurations in regular and virtual machine scale set network interface.
 */
@Fluent
public interface NicIpConfigurationBase extends HasSubnet, HasPrivateIpAddress {
    /**
     * Checks whether this is the primary IP configuration.
     *
     * @return true if this is the primary IP configuration
     */
    boolean isPrimary();

    /**
     * Gets the virtual network associated with this IP configuration.
     *
     * @return the virtual network associated with this IP configuration
     */
    Network getNetwork();

    /**
     * Gets the network security group.
     *
     * @return the network security group, if any, associated with the subnet, if any, assigned to this network
     *     interface IP configuration
     *     <p>(Note that this results in additional calls to Azure.)
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * Gets private IP address version.
     *
     * @return private IP address version
     */
    IpVersion privateIpAddressVersion();

    /**
     * Gets the load balancer backends associated with this network interface IP configuration.
     *
     * @return the load balancer backends associated with this network interface IP configuration
     */
    // TODO: This should be a Collection
    List<LoadBalancerBackend> listAssociatedLoadBalancerBackends();

    /**
     * Gets the load balancer inbound NAT rules associated with this network interface IP configuration.
     *
     * @return the load balancer inbound NAT rules associated with this network interface IP configuration
     */
    // TODO: This should be a Collection
    List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules();

    /**
     * Gets the application gateway backends associated with this network IP configuration.
     *
     * @return the application gateway backends associated with this network IP configuration
     */
    Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends();

    /**
     * Gets the application security groups associated with this network IP configuration.
     *
     * @return the application security groups associated with this network IP configuration
     */
    List<ApplicationSecurityGroup> listAssociatedApplicationSecurityGroups();

    /**
     * Gets the application security groups associated with this network IP configuration.
     *
     * @param context the {@link Context} of the request
     * @return the application security groups associated with this network IP configuration
     */
    default List<ApplicationSecurityGroup> listAssociatedApplicationSecurityGroups(Context context) {
        throw new UnsupportedOperationException(
            "[listAssociatedApplicationSecurityGroups(Context)] is not supported in " + getClass());
    }
}
