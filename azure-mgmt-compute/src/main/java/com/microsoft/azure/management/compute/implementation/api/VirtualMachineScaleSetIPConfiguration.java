/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Describes a virtual machine scale set network profile's IP configuration.
 */
@JsonFlatten
public class VirtualMachineScaleSetIPConfiguration extends SubResource {
    /**
     * Gets or sets the IP configuration name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the subnet.
     */
    @JsonProperty(value = "properties.subnet", required = true)
    private ApiEntityReference subnet;

    /**
     * Gets or sets the load balancer backend address pools.
     */
    @JsonProperty(value = "properties.loadBalancerBackendAddressPools")
    private List<SubResource> loadBalancerBackendAddressPools;

    /**
     * Gets or sets the load balancer inbound nat pools.
     */
    @JsonProperty(value = "properties.loadBalancerInboundNatPools")
    private List<SubResource> loadBalancerInboundNatPools;

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
     * @return the VirtualMachineScaleSetIPConfiguration object itself.
     */
    public VirtualMachineScaleSetIPConfiguration withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the subnet value.
     *
     * @return the subnet value
     */
    public ApiEntityReference subnet() {
        return this.subnet;
    }

    /**
     * Set the subnet value.
     *
     * @param subnet the subnet value to set
     * @return the VirtualMachineScaleSetIPConfiguration object itself.
     */
    public VirtualMachineScaleSetIPConfiguration withSubnet(ApiEntityReference subnet) {
        this.subnet = subnet;
        return this;
    }

    /**
     * Get the loadBalancerBackendAddressPools value.
     *
     * @return the loadBalancerBackendAddressPools value
     */
    public List<SubResource> loadBalancerBackendAddressPools() {
        return this.loadBalancerBackendAddressPools;
    }

    /**
     * Set the loadBalancerBackendAddressPools value.
     *
     * @param loadBalancerBackendAddressPools the loadBalancerBackendAddressPools value to set
     * @return the VirtualMachineScaleSetIPConfiguration object itself.
     */
    public VirtualMachineScaleSetIPConfiguration withLoadBalancerBackendAddressPools(List<SubResource> loadBalancerBackendAddressPools) {
        this.loadBalancerBackendAddressPools = loadBalancerBackendAddressPools;
        return this;
    }

    /**
     * Get the loadBalancerInboundNatPools value.
     *
     * @return the loadBalancerInboundNatPools value
     */
    public List<SubResource> loadBalancerInboundNatPools() {
        return this.loadBalancerInboundNatPools;
    }

    /**
     * Set the loadBalancerInboundNatPools value.
     *
     * @param loadBalancerInboundNatPools the loadBalancerInboundNatPools value to set
     * @return the VirtualMachineScaleSetIPConfiguration object itself.
     */
    public VirtualMachineScaleSetIPConfiguration withLoadBalancerInboundNatPools(List<SubResource> loadBalancerInboundNatPools) {
        this.loadBalancerInboundNatPools = loadBalancerInboundNatPools;
        return this;
    }

}
