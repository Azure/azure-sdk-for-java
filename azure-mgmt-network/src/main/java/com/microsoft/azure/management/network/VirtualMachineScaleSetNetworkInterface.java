/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;
import java.util.Map;

/**
 * Virtual machine scale set network interface.
 */
@Fluent
public interface VirtualMachineScaleSetNetworkInterface extends
        Resource,
        Refreshable<VirtualMachineScaleSetNetworkInterface>,
        Wrapper<NetworkInterfaceInner> {
    /**
     * @return <tt>true</tt> if Ip forwarding is enabled in this network interface
     */
    boolean isIpForwardingEnabled();

    /**
     * @return the MAC Address of the network interface
     */
    String macAddress();

    /**
     * @return the internal DNS name assigned to this network interface
     */
    String internalDnsNameLabel();

    /**
     * Gets the fully qualified domain name of this network interface.
     * <p>
     * A network interface receives FQDN as a part of assigning it to a scale set virtual machine.
     *
     * @return the qualified domain name
     */
    String internalFqdn();

    /**
     * @return the internal domain name suffix
     */
    String internalDomainNameSuffix();

    /**
     * @return IP addresses of this network interface's DNS servers
     */
    List<String> dnsServers();

    /**
     * @return applied DNS servers
     */
    List<String> appliedDnsServers();

    /**
     * @return the IP configurations of this network interface, indexed by their names
     */
    Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigurations();

    /**
     * @return the network security group resource id associated with this network interface
     */
    String networkSecurityGroupId();

    /**
     * @return the resource Id of the associated scale set virtual machine
     */
    String virtualMachineId();
}