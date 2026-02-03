// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** The base network interface shared across regular and virtual machine scale set network interface. */
@Fluent
public interface NetworkInterfaceBase extends HasManager<NetworkManager>, HasInnerModel<NetworkInterfaceInner> {

    /**
     * Checks whether accelerated networking is enabled for this network interface.
     *
     * @return true if accelerated networking is enabled for this network interface
     */
    boolean isAcceleratedNetworkingEnabled();

    /**
     * Checks whether IP forwarding is enabled in this network interface.
     *
     * @return true if IP forwarding is enabled in this network interface
     */
    boolean isIPForwardingEnabled();

    /**
     * Gets the MAC Address of the network interface.
     *
     * @return the MAC Address of the network interface
     */
    String macAddress();

    /**
     * Gets the Internal DNS name assigned to this network interface.
     *
     * @return the Internal DNS name assigned to this network interface
     */
    String internalDnsNameLabel();

    /**
     * Gets the fully qualified domain name of this network interface.
     *
     * <p>A network interface receives FQDN as a part of assigning it to a virtual machine.
     *
     * @return the qualified domain name
     */
    String internalFqdn();

    /**
     * Gets the internal domain name suffix.
     *
     * @return the internal domain name suffix
     */
    String internalDomainNameSuffix();

    /**
     * Gets IP addresses of this network interface's DNS servers.
     *
     * @return IP addresses of this network interface's DNS servers
     */
    List<String> dnsServers();

    /**
     * Gets applied DNS servers.
     *
     * @return applied DNS servers
     */
    List<String> appliedDnsServers();

    /**
     * Gets the network security group resource id associated with this network interface.
     *
     * @return the network security group resource id associated with this network interface
     */
    String networkSecurityGroupId();

    /**
     * Gets the network security group associated this network interface.
     *
     * <p>This method makes a rest API call to fetch the Network Security Group resource.
     *
     * @return the network security group associated with this network interface.
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * Gets the resource ID of the associated virtual machine.
     *
     * @return the resource ID of the associated virtual machine, or null if none.
     */
    String virtualMachineId();

    /**
     * Gets the private IP address allocated to this network interface's primary IP configuration.
     *
     * <p>The private IP will be within the virtual network subnet of this network interface.
     *
     * @return the private IP addresses
     */
    String primaryPrivateIP();

    /**
     * Gets the private IP allocation method (Dynamic, Static) of this network interface's primary IP configuration.
     *
     * @return the private IP allocation method (Dynamic, Static) of this network interface's primary IP configuration.
     */
    IpAllocationMethod primaryPrivateIpAllocationMethod();
}
