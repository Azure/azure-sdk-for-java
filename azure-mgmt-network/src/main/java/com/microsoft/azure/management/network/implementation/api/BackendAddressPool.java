/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;
import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Pool of backend IP addresseses.
 */
@JsonFlatten
public class BackendAddressPool extends SubResource {
    /**
     * Gets collection of references to IPs defined in NICs.
     */
    @JsonProperty(value = "properties.backendIPConfigurations")
    private List<NetworkInterfaceIPConfiguration> backendIPConfigurations;

    /**
     * Gets Load Balancing rules that use this Backend Address Pool.
     */
    @JsonProperty(value = "properties.loadBalancingRules")
    private List<SubResource> loadBalancingRules;

    /**
     * Gets outbound rules that use this Backend Address Pool.
     */
    @JsonProperty(value = "properties.outboundNatRule")
    private SubResource outboundNatRule;

    /**
     * Provisioning state of the PublicIP resource Updating/Deleting/Failed.
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
     * Get the backendIPConfigurations value.
     *
     * @return the backendIPConfigurations value
     */
    public List<NetworkInterfaceIPConfiguration> backendIPConfigurations() {
        return this.backendIPConfigurations;
    }

    /**
     * Set the backendIPConfigurations value.
     *
     * @param backendIPConfigurations the backendIPConfigurations value to set
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withBackendIPConfigurations(List<NetworkInterfaceIPConfiguration> backendIPConfigurations) {
        this.backendIPConfigurations = backendIPConfigurations;
        return this;
    }

    /**
     * Get the loadBalancingRules value.
     *
     * @return the loadBalancingRules value
     */
    public List<SubResource> loadBalancingRules() {
        return this.loadBalancingRules;
    }

    /**
     * Set the loadBalancingRules value.
     *
     * @param loadBalancingRules the loadBalancingRules value to set
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withLoadBalancingRules(List<SubResource> loadBalancingRules) {
        this.loadBalancingRules = loadBalancingRules;
        return this;
    }

    /**
     * Get the outboundNatRule value.
     *
     * @return the outboundNatRule value
     */
    public SubResource outboundNatRule() {
        return this.outboundNatRule;
    }

    /**
     * Set the outboundNatRule value.
     *
     * @param outboundNatRule the outboundNatRule value to set
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withOutboundNatRule(SubResource outboundNatRule) {
        this.outboundNatRule = outboundNatRule;
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
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withProvisioningState(String provisioningState) {
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
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withName(String name) {
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
     * @return the BackendAddressPool object itself.
     */
    public BackendAddressPool withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
