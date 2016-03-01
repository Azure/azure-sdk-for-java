/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Describes a virtual machine scale set network profile's IP configuration
 * properties.
 */
public class VirtualMachineScaleSetIPConfigurationProperties {
    /**
     * Gets or sets the subnet.
     */
    @JsonProperty(required = true)
    private ApiEntityReference subnet;

    /**
     * Gets or sets the load balancer backend address pools.
     */
    private List<SubResource> loadBalancerBackendAddressPools;

    /**
     * Gets or sets the load balancer inbound nat pools.
     */
    private List<SubResource> loadBalancerInboundNatPools;

    /**
     * Get the subnet value.
     *
     * @return the subnet value
     */
    public ApiEntityReference getSubnet() {
        return this.subnet;
    }

    /**
     * Set the subnet value.
     *
     * @param subnet the subnet value to set
     */
    public void setSubnet(ApiEntityReference subnet) {
        this.subnet = subnet;
    }

    /**
     * Get the loadBalancerBackendAddressPools value.
     *
     * @return the loadBalancerBackendAddressPools value
     */
    public List<SubResource> getLoadBalancerBackendAddressPools() {
        return this.loadBalancerBackendAddressPools;
    }

    /**
     * Set the loadBalancerBackendAddressPools value.
     *
     * @param loadBalancerBackendAddressPools the loadBalancerBackendAddressPools value to set
     */
    public void setLoadBalancerBackendAddressPools(List<SubResource> loadBalancerBackendAddressPools) {
        this.loadBalancerBackendAddressPools = loadBalancerBackendAddressPools;
    }

    /**
     * Get the loadBalancerInboundNatPools value.
     *
     * @return the loadBalancerInboundNatPools value
     */
    public List<SubResource> getLoadBalancerInboundNatPools() {
        return this.loadBalancerInboundNatPools;
    }

    /**
     * Set the loadBalancerInboundNatPools value.
     *
     * @param loadBalancerInboundNatPools the loadBalancerInboundNatPools value to set
     */
    public void setLoadBalancerInboundNatPools(List<SubResource> loadBalancerInboundNatPools) {
        this.loadBalancerInboundNatPools = loadBalancerInboundNatPools;
    }

}
