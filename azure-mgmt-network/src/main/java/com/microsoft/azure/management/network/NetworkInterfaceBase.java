package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.List;

/**
 * The base network interface shared across regular and virtual machine scale set network interface.
 */
@Fluent
public interface NetworkInterfaceBase {
    /**
     * @return <tt>true</tt> if IP forwarding is enabled in this network interface
     */
    boolean isIpForwardingEnabled();

    /**
     * @return the MAC Address of the network interface
     */
    String macAddress();

    /**
     *
     * @return the Internal DNS name assigned to this network interface
     */
    String internalDnsNameLabel();

    /**
     * Gets the fully qualified domain name of this network interface.
     * <p>
     * A network interface receives FQDN as a part of assigning it to a virtual machine.
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
     * @return the network security group resource id associated with this network interface
     */
    String networkSecurityGroupId();

    /**
     * Gets the network security group associated this network interface.
     * <p>
     * This method makes a rest API call to fetch the Network Security Group resource.
     *
     * @return the network security group associated with this network interface.
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * @return the resource ID of the associated virtual machine, or null if none.
     */
    String virtualMachineId();

    /**
     * Gets the private IP address allocated to this network interface's primary IP configuration.
     * <p>
     * The private IP will be within the virtual network subnet of this network interface.
     *
     * @return the private IP addresses
     */
    String primaryPrivateIp();

    /**
     * @return the private IP allocation method (Dynamic, Static) of this network interface's
     * primary IP configuration.
     */
    IPAllocationMethod primaryPrivateIpAllocationMethod();
}
