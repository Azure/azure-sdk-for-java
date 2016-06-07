/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Virtual Network resource.
 */
@JsonFlatten
public class VirtualNetworkInner extends Resource {
    /**
     * Gets or sets AddressSpace that contains an array of IP address ranges
     * that can be used by subnets.
     */
    @JsonProperty(value = "properties.addressSpace")
    private AddressSpace addressSpace;

    /**
     * Gets or sets DHCPOptions that contains an array of DNS servers
     * available to VMs deployed in the virtual network.
     */
    @JsonProperty(value = "properties.dhcpOptions")
    private DhcpOptions dhcpOptions;

    /**
     * Gets or sets List of subnets in a VirtualNetwork.
     */
    @JsonProperty(value = "properties.subnets")
    private List<SubnetInner> subnets;

    /**
     * Gets or sets resource guid property of the VirtualNetwork resource.
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
     * Get the addressSpace value.
     *
     * @return the addressSpace value
     */
    public AddressSpace addressSpace() {
        return this.addressSpace;
    }

    /**
     * Set the addressSpace value.
     *
     * @param addressSpace the addressSpace value to set
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withAddressSpace(AddressSpace addressSpace) {
        this.addressSpace = addressSpace;
        return this;
    }

    /**
     * Get the dhcpOptions value.
     *
     * @return the dhcpOptions value
     */
    public DhcpOptions dhcpOptions() {
        return this.dhcpOptions;
    }

    /**
     * Set the dhcpOptions value.
     *
     * @param dhcpOptions the dhcpOptions value to set
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withDhcpOptions(DhcpOptions dhcpOptions) {
        this.dhcpOptions = dhcpOptions;
        return this;
    }

    /**
     * Get the subnets value.
     *
     * @return the subnets value
     */
    public List<SubnetInner> subnets() {
        return this.subnets;
    }

    /**
     * Set the subnets value.
     *
     * @param subnets the subnets value to set
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withSubnets(List<SubnetInner> subnets) {
        this.subnets = subnets;
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
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withResourceGuid(String resourceGuid) {
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
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withProvisioningState(String provisioningState) {
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
     * @return the VirtualNetworkInner object itself.
     */
    public VirtualNetworkInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
