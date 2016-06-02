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
 * NetworkSecurityGroup resource.
 */
@JsonFlatten
public class NetworkSecurityGroupInner extends Resource {
    /**
     * Gets or sets Security rules of network security group.
     */
    @JsonProperty(value = "properties.securityRules")
    private List<SecurityRuleInner> securityRules;

    /**
     * Gets or sets Default security rules of network security group.
     */
    @JsonProperty(value = "properties.defaultSecurityRules")
    private List<SecurityRuleInner> defaultSecurityRules;

    /**
     * Gets collection of references to Network Interfaces.
     */
    @JsonProperty(value = "properties.networkInterfaces")
    private List<NetworkInterfaceInner> networkInterfaces;

    /**
     * Gets collection of references to subnets.
     */
    @JsonProperty(value = "properties.subnets")
    private List<SubnetInner> subnets;

    /**
     * Gets or sets resource guid property of the network security group
     * resource.
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
     * Get the securityRules value.
     *
     * @return the securityRules value
     */
    public List<SecurityRuleInner> securityRules() {
        return this.securityRules;
    }

    /**
     * Set the securityRules value.
     *
     * @param securityRules the securityRules value to set
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withSecurityRules(List<SecurityRuleInner> securityRules) {
        this.securityRules = securityRules;
        return this;
    }

    /**
     * Get the defaultSecurityRules value.
     *
     * @return the defaultSecurityRules value
     */
    public List<SecurityRuleInner> defaultSecurityRules() {
        return this.defaultSecurityRules;
    }

    /**
     * Set the defaultSecurityRules value.
     *
     * @param defaultSecurityRules the defaultSecurityRules value to set
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withDefaultSecurityRules(List<SecurityRuleInner> defaultSecurityRules) {
        this.defaultSecurityRules = defaultSecurityRules;
        return this;
    }

    /**
     * Get the networkInterfaces value.
     *
     * @return the networkInterfaces value
     */
    public List<NetworkInterfaceInner> networkInterfaces() {
        return this.networkInterfaces;
    }

    /**
     * Set the networkInterfaces value.
     *
     * @param networkInterfaces the networkInterfaces value to set
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withNetworkInterfaces(List<NetworkInterfaceInner> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
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
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withSubnets(List<SubnetInner> subnets) {
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
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withResourceGuid(String resourceGuid) {
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
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withProvisioningState(String provisioningState) {
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
     * @return the NetworkSecurityGroupInner object itself.
     */
    public NetworkSecurityGroupInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
