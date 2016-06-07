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
 * LoadBalancer resource.
 */
@JsonFlatten
public class LoadBalancerInner extends Resource {
    /**
     * Gets or sets frontend IP addresses of the load balancer.
     */
    @JsonProperty(value = "properties.frontendIPConfigurations")
    private List<FrontendIPConfiguration> frontendIPConfigurations;

    /**
     * Gets or sets Pools of backend IP addresseses.
     */
    @JsonProperty(value = "properties.backendAddressPools")
    private List<BackendAddressPool> backendAddressPools;

    /**
     * Gets or sets loadbalancing rules.
     */
    @JsonProperty(value = "properties.loadBalancingRules")
    private List<LoadBalancingRule> loadBalancingRules;

    /**
     * Gets or sets list of Load balancer probes.
     */
    @JsonProperty(value = "properties.probes")
    private List<Probe> probes;

    /**
     * Gets or sets list of inbound rules.
     */
    @JsonProperty(value = "properties.inboundNatRules")
    private List<InboundNatRule> inboundNatRules;

    /**
     * Gets or sets inbound NAT pools.
     */
    @JsonProperty(value = "properties.inboundNatPools")
    private List<InboundNatPool> inboundNatPools;

    /**
     * Gets or sets outbound NAT rules.
     */
    @JsonProperty(value = "properties.outboundNatRules")
    private List<OutboundNatRule> outboundNatRules;

    /**
     * Gets or sets resource guid property of the Load balancer resource.
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
     * Get the frontendIPConfigurations value.
     *
     * @return the frontendIPConfigurations value
     */
    public List<FrontendIPConfiguration> frontendIPConfigurations() {
        return this.frontendIPConfigurations;
    }

    /**
     * Set the frontendIPConfigurations value.
     *
     * @param frontendIPConfigurations the frontendIPConfigurations value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withFrontendIPConfigurations(List<FrontendIPConfiguration> frontendIPConfigurations) {
        this.frontendIPConfigurations = frontendIPConfigurations;
        return this;
    }

    /**
     * Get the backendAddressPools value.
     *
     * @return the backendAddressPools value
     */
    public List<BackendAddressPool> backendAddressPools() {
        return this.backendAddressPools;
    }

    /**
     * Set the backendAddressPools value.
     *
     * @param backendAddressPools the backendAddressPools value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withBackendAddressPools(List<BackendAddressPool> backendAddressPools) {
        this.backendAddressPools = backendAddressPools;
        return this;
    }

    /**
     * Get the loadBalancingRules value.
     *
     * @return the loadBalancingRules value
     */
    public List<LoadBalancingRule> loadBalancingRules() {
        return this.loadBalancingRules;
    }

    /**
     * Set the loadBalancingRules value.
     *
     * @param loadBalancingRules the loadBalancingRules value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withLoadBalancingRules(List<LoadBalancingRule> loadBalancingRules) {
        this.loadBalancingRules = loadBalancingRules;
        return this;
    }

    /**
     * Get the probes value.
     *
     * @return the probes value
     */
    public List<Probe> probes() {
        return this.probes;
    }

    /**
     * Set the probes value.
     *
     * @param probes the probes value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withProbes(List<Probe> probes) {
        this.probes = probes;
        return this;
    }

    /**
     * Get the inboundNatRules value.
     *
     * @return the inboundNatRules value
     */
    public List<InboundNatRule> inboundNatRules() {
        return this.inboundNatRules;
    }

    /**
     * Set the inboundNatRules value.
     *
     * @param inboundNatRules the inboundNatRules value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withInboundNatRules(List<InboundNatRule> inboundNatRules) {
        this.inboundNatRules = inboundNatRules;
        return this;
    }

    /**
     * Get the inboundNatPools value.
     *
     * @return the inboundNatPools value
     */
    public List<InboundNatPool> inboundNatPools() {
        return this.inboundNatPools;
    }

    /**
     * Set the inboundNatPools value.
     *
     * @param inboundNatPools the inboundNatPools value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withInboundNatPools(List<InboundNatPool> inboundNatPools) {
        this.inboundNatPools = inboundNatPools;
        return this;
    }

    /**
     * Get the outboundNatRules value.
     *
     * @return the outboundNatRules value
     */
    public List<OutboundNatRule> outboundNatRules() {
        return this.outboundNatRules;
    }

    /**
     * Set the outboundNatRules value.
     *
     * @param outboundNatRules the outboundNatRules value to set
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withOutboundNatRules(List<OutboundNatRule> outboundNatRules) {
        this.outboundNatRules = outboundNatRules;
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
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withResourceGuid(String resourceGuid) {
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
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withProvisioningState(String provisioningState) {
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
     * @return the LoadBalancerInner object itself.
     */
    public LoadBalancerInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
