/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * IPConfiguration in a NetworkInterface.
 */
@JsonFlatten
public class NetworkInterfaceIPConfiguration extends SubResource {
    /**
     * Gets or sets the reference of LoadBalancerBackendAddressPool resource.
     */
    @JsonProperty(value = "properties.loadBalancerBackendAddressPools")
    private List<BackendAddressPool> loadBalancerBackendAddressPools;

    /**
     * Gets or sets list of references of LoadBalancerInboundNatRules.
     */
    @JsonProperty(value = "properties.loadBalancerInboundNatRules")
    private List<InboundNatRule> loadBalancerInboundNatRules;

    /**
     * The privateIPAddress property.
     */
    @JsonProperty(value = "properties.privateIPAddress")
    private String privateIPAddress;

    /**
     * Gets or sets PrivateIP allocation method (Static/Dynamic). Possible
     * values include: 'Static', 'Dynamic'.
     */
    @JsonProperty(value = "properties.privateIPAllocationMethod")
    private String privateIPAllocationMethod;

    /**
     * The subnet property.
     */
    @JsonProperty(value = "properties.subnet")
    private SubnetInner subnet;

    /**
     * The publicIPAddress property.
     */
    @JsonProperty(value = "properties.publicIPAddress")
    private PublicIPAddressInner publicIPAddress;

    /**
     * The provisioningState property.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets name of the resource that is unique within a resource group. This
     * name can be used to access the resource.
     */
    private String name;

    /**
     * A unique read-only string that changes whenever the resource is updated.
     */
    private String etag;

    /**
     * Get the loadBalancerBackendAddressPools value.
     *
     * @return the loadBalancerBackendAddressPools value
     */
    public List<BackendAddressPool> loadBalancerBackendAddressPools() {
        return this.loadBalancerBackendAddressPools;
    }

    /**
     * Set the loadBalancerBackendAddressPools value.
     *
     * @param loadBalancerBackendAddressPools the loadBalancerBackendAddressPools value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withLoadBalancerBackendAddressPools(List<BackendAddressPool> loadBalancerBackendAddressPools) {
        this.loadBalancerBackendAddressPools = loadBalancerBackendAddressPools;
        return this;
    }

    /**
     * Get the loadBalancerInboundNatRules value.
     *
     * @return the loadBalancerInboundNatRules value
     */
    public List<InboundNatRule> loadBalancerInboundNatRules() {
        return this.loadBalancerInboundNatRules;
    }

    /**
     * Set the loadBalancerInboundNatRules value.
     *
     * @param loadBalancerInboundNatRules the loadBalancerInboundNatRules value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withLoadBalancerInboundNatRules(List<InboundNatRule> loadBalancerInboundNatRules) {
        this.loadBalancerInboundNatRules = loadBalancerInboundNatRules;
        return this;
    }

    /**
     * Get the privateIPAddress value.
     *
     * @return the privateIPAddress value
     */
    public String privateIPAddress() {
        return this.privateIPAddress;
    }

    /**
     * Set the privateIPAddress value.
     *
     * @param privateIPAddress the privateIPAddress value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withPrivateIPAddress(String privateIPAddress) {
        this.privateIPAddress = privateIPAddress;
        return this;
    }

    /**
     * Get the privateIPAllocationMethod value.
     *
     * @return the privateIPAllocationMethod value
     */
    public String privateIPAllocationMethod() {
        return this.privateIPAllocationMethod;
    }

    /**
     * Set the privateIPAllocationMethod value.
     *
     * @param privateIPAllocationMethod the privateIPAllocationMethod value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withPrivateIPAllocationMethod(String privateIPAllocationMethod) {
        this.privateIPAllocationMethod = privateIPAllocationMethod;
        return this;
    }

    /**
     * Get the subnet value.
     *
     * @return the subnet value
     */
    public SubnetInner subnet() {
        return this.subnet;
    }

    /**
     * Set the subnet value.
     *
     * @param subnet the subnet value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withSubnet(SubnetInner subnet) {
        this.subnet = subnet;
        return this;
    }

    /**
     * Get the publicIPAddress value.
     *
     * @return the publicIPAddress value
     */
    public PublicIPAddressInner publicIPAddress() {
        return this.publicIPAddress;
    }

    /**
     * Set the publicIPAddress value.
     *
     * @param publicIPAddress the publicIPAddress value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withPublicIPAddress(PublicIPAddressInner publicIPAddress) {
        this.publicIPAddress = publicIPAddress;
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
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withName(String name) {
        this.name = name;
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
     * @return the NetworkInterfaceIPConfiguration object itself.
     */
    public NetworkInterfaceIPConfiguration withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
