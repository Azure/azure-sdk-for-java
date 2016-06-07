/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.microsoft.azure.SubResource;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A NetworkInterface in a resource group.
 */
@JsonFlatten
public class NetworkInterfaceInner extends Resource {
    /**
     * Gets or sets the reference of a VirtualMachine.
     */
    @JsonProperty(value = "properties.virtualMachine")
    private SubResource virtualMachine;

    /**
     * Gets or sets the reference of the NetworkSecurityGroup resource.
     */
    @JsonProperty(value = "properties.networkSecurityGroup")
    private NetworkSecurityGroupInner networkSecurityGroup;

    /**
     * Gets or sets list of IPConfigurations of the NetworkInterface.
     */
    @JsonProperty(value = "properties.ipConfigurations")
    private List<NetworkInterfaceIPConfiguration> ipConfigurations;

    /**
     * Gets or sets DNS Settings in  NetworkInterface.
     */
    @JsonProperty(value = "properties.dnsSettings")
    private NetworkInterfaceDnsSettings dnsSettings;

    /**
     * Gets the MAC Address of the network interface.
     */
    @JsonProperty(value = "properties.macAddress")
    private String macAddress;

    /**
     * Gets whether this is a primary NIC on a virtual machine.
     */
    @JsonProperty(value = "properties.primary")
    private Boolean primary;

    /**
     * Gets or sets whether IPForwarding is enabled on the NIC.
     */
    @JsonProperty(value = "properties.enableIPForwarding")
    private Boolean enableIPForwarding;

    /**
     * Gets or sets resource guid property of the network interface resource.
     */
    @JsonProperty(value = "properties.resourceGuid")
    private String resourceGuid;

    /**
     * Gets or sets Provisioning state of the PublicIP resource
     * Updating/Deleting/Failed.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets a unique read-only string that changes whenever the resource is
     * updated.
     */
    private String etag;

    /**
     * Get the virtualMachine value.
     *
     * @return the virtualMachine value
     */
    public SubResource virtualMachine() {
        return this.virtualMachine;
    }

    /**
     * Set the virtualMachine value.
     *
     * @param virtualMachine the virtualMachine value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withVirtualMachine(SubResource virtualMachine) {
        this.virtualMachine = virtualMachine;
        return this;
    }

    /**
     * Get the networkSecurityGroup value.
     *
     * @return the networkSecurityGroup value
     */
    public NetworkSecurityGroupInner networkSecurityGroup() {
        return this.networkSecurityGroup;
    }

    /**
     * Set the networkSecurityGroup value.
     *
     * @param networkSecurityGroup the networkSecurityGroup value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withNetworkSecurityGroup(NetworkSecurityGroupInner networkSecurityGroup) {
        this.networkSecurityGroup = networkSecurityGroup;
        return this;
    }

    /**
     * Get the ipConfigurations value.
     *
     * @return the ipConfigurations value
     */
    public List<NetworkInterfaceIPConfiguration> ipConfigurations() {
        return this.ipConfigurations;
    }

    /**
     * Set the ipConfigurations value.
     *
     * @param ipConfigurations the ipConfigurations value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withIpConfigurations(List<NetworkInterfaceIPConfiguration> ipConfigurations) {
        this.ipConfigurations = ipConfigurations;
        return this;
    }

    /**
     * Get the dnsSettings value.
     *
     * @return the dnsSettings value
     */
    public NetworkInterfaceDnsSettings dnsSettings() {
        return this.dnsSettings;
    }

    /**
     * Set the dnsSettings value.
     *
     * @param dnsSettings the dnsSettings value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withDnsSettings(NetworkInterfaceDnsSettings dnsSettings) {
        this.dnsSettings = dnsSettings;
        return this;
    }

    /**
     * Get the macAddress value.
     *
     * @return the macAddress value
     */
    public String macAddress() {
        return this.macAddress;
    }

    /**
     * Set the macAddress value.
     *
     * @param macAddress the macAddress value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public Boolean primary() {
        return this.primary;
    }

    /**
     * Set the primary value.
     *
     * @param primary the primary value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Get the enableIPForwarding value.
     *
     * @return the enableIPForwarding value
     */
    public Boolean enableIPForwarding() {
        return this.enableIPForwarding;
    }

    /**
     * Set the enableIPForwarding value.
     *
     * @param enableIPForwarding the enableIPForwarding value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withEnableIPForwarding(Boolean enableIPForwarding) {
        this.enableIPForwarding = enableIPForwarding;
        return this;
    }

    /**
     * Get the resourceGuid value.
     *
     * @return the resourceGuid value
     */
    public String resourceGuid() {
        return this.resourceGuid;
    }

    /**
     * Set the resourceGuid value.
     *
     * @param resourceGuid the resourceGuid value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the etag value.
     *
     * @return the etag value
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Set the etag value.
     *
     * @param etag the etag value to set
     * @return the NetworkInterfaceInner object itself.
     */
    public NetworkInterfaceInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
