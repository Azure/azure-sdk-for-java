/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.resourcemover.v2021_01_01;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines NIC IP configuration properties.
 */
public class NicIpConfigurationResourceSettings {
    /**
     * Gets or sets the IP configuration name.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Gets or sets the private IP address of the network interface IP
     * Configuration.
     */
    @JsonProperty(value = "privateIpAddress")
    private String privateIpAddress;

    /**
     * Gets or sets the private IP address allocation method.
     */
    @JsonProperty(value = "privateIpAllocationMethod")
    private String privateIpAllocationMethod;

    /**
     * The subnet property.
     */
    @JsonProperty(value = "subnet")
    private SubnetReference subnet;

    /**
     * Gets or sets a value indicating whether this IP configuration is the
     * primary.
     */
    @JsonProperty(value = "primary")
    private Boolean primary;

    /**
     * Gets or sets the references of the load balancer backend address pools.
     */
    @JsonProperty(value = "loadBalancerBackendAddressPools")
    private List<LoadBalancerBackendAddressPoolReference> loadBalancerBackendAddressPools;

    /**
     * Gets or sets the references of the load balancer NAT rules.
     */
    @JsonProperty(value = "loadBalancerNatRules")
    private List<LoadBalancerNatRuleReference> loadBalancerNatRules;

    /**
     * The publicIp property.
     */
    @JsonProperty(value = "publicIp")
    private PublicIpReference publicIp;

    /**
     * Get gets or sets the IP configuration name.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set gets or sets the IP configuration name.
     *
     * @param name the name value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get gets or sets the private IP address of the network interface IP Configuration.
     *
     * @return the privateIpAddress value
     */
    public String privateIpAddress() {
        return this.privateIpAddress;
    }

    /**
     * Set gets or sets the private IP address of the network interface IP Configuration.
     *
     * @param privateIpAddress the privateIpAddress value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
        return this;
    }

    /**
     * Get gets or sets the private IP address allocation method.
     *
     * @return the privateIpAllocationMethod value
     */
    public String privateIpAllocationMethod() {
        return this.privateIpAllocationMethod;
    }

    /**
     * Set gets or sets the private IP address allocation method.
     *
     * @param privateIpAllocationMethod the privateIpAllocationMethod value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withPrivateIpAllocationMethod(String privateIpAllocationMethod) {
        this.privateIpAllocationMethod = privateIpAllocationMethod;
        return this;
    }

    /**
     * Get the subnet value.
     *
     * @return the subnet value
     */
    public SubnetReference subnet() {
        return this.subnet;
    }

    /**
     * Set the subnet value.
     *
     * @param subnet the subnet value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withSubnet(SubnetReference subnet) {
        this.subnet = subnet;
        return this;
    }

    /**
     * Get gets or sets a value indicating whether this IP configuration is the primary.
     *
     * @return the primary value
     */
    public Boolean primary() {
        return this.primary;
    }

    /**
     * Set gets or sets a value indicating whether this IP configuration is the primary.
     *
     * @param primary the primary value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    /**
     * Get gets or sets the references of the load balancer backend address pools.
     *
     * @return the loadBalancerBackendAddressPools value
     */
    public List<LoadBalancerBackendAddressPoolReference> loadBalancerBackendAddressPools() {
        return this.loadBalancerBackendAddressPools;
    }

    /**
     * Set gets or sets the references of the load balancer backend address pools.
     *
     * @param loadBalancerBackendAddressPools the loadBalancerBackendAddressPools value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withLoadBalancerBackendAddressPools(List<LoadBalancerBackendAddressPoolReference> loadBalancerBackendAddressPools) {
        this.loadBalancerBackendAddressPools = loadBalancerBackendAddressPools;
        return this;
    }

    /**
     * Get gets or sets the references of the load balancer NAT rules.
     *
     * @return the loadBalancerNatRules value
     */
    public List<LoadBalancerNatRuleReference> loadBalancerNatRules() {
        return this.loadBalancerNatRules;
    }

    /**
     * Set gets or sets the references of the load balancer NAT rules.
     *
     * @param loadBalancerNatRules the loadBalancerNatRules value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withLoadBalancerNatRules(List<LoadBalancerNatRuleReference> loadBalancerNatRules) {
        this.loadBalancerNatRules = loadBalancerNatRules;
        return this;
    }

    /**
     * Get the publicIp value.
     *
     * @return the publicIp value
     */
    public PublicIpReference publicIp() {
        return this.publicIp;
    }

    /**
     * Set the publicIp value.
     *
     * @param publicIp the publicIp value to set
     * @return the NicIpConfigurationResourceSettings object itself.
     */
    public NicIpConfigurationResourceSettings withPublicIp(PublicIpReference publicIp) {
        this.publicIp = publicIp;
        return this;
    }

}
