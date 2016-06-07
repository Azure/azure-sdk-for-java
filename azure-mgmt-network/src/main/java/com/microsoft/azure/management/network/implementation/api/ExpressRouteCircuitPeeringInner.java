/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * Peering in a ExpressRouteCircuit resource.
 */
@JsonFlatten
public class ExpressRouteCircuitPeeringInner extends SubResource {
    /**
     * Gets or sets PeeringType. Possible values include:
     * 'AzurePublicPeering', 'AzurePrivatePeering', 'MicrosoftPeering'.
     */
    @JsonProperty(value = "properties.peeringType")
    private String peeringType;

    /**
     * Gets or sets state of Peering. Possible values include: 'Disabled',
     * 'Enabled'.
     */
    @JsonProperty(value = "properties.state")
    private String state;

    /**
     * Gets or sets the azure ASN.
     */
    @JsonProperty(value = "properties.azureASN")
    private Integer azureASN;

    /**
     * Gets or sets the peer ASN.
     */
    @JsonProperty(value = "properties.peerASN")
    private Integer peerASN;

    /**
     * Gets or sets the primary address prefix.
     */
    @JsonProperty(value = "properties.primaryPeerAddressPrefix")
    private String primaryPeerAddressPrefix;

    /**
     * Gets or sets the secondary address prefix.
     */
    @JsonProperty(value = "properties.secondaryPeerAddressPrefix")
    private String secondaryPeerAddressPrefix;

    /**
     * Gets or sets the primary port.
     */
    @JsonProperty(value = "properties.primaryAzurePort")
    private String primaryAzurePort;

    /**
     * Gets or sets the secondary port.
     */
    @JsonProperty(value = "properties.secondaryAzurePort")
    private String secondaryAzurePort;

    /**
     * Gets or sets the shared key.
     */
    @JsonProperty(value = "properties.sharedKey")
    private String sharedKey;

    /**
     * Gets or sets the vlan id.
     */
    @JsonProperty(value = "properties.vlanId")
    private Integer vlanId;

    /**
     * Gets or sets the mircosoft peering config.
     */
    @JsonProperty(value = "properties.microsoftPeeringConfig")
    private ExpressRouteCircuitPeeringConfig microsoftPeeringConfig;

    /**
     * Gets or peering stats.
     */
    @JsonProperty(value = "properties.stats")
    private ExpressRouteCircuitStatsInner stats;

    /**
     * Gets or sets Provisioning state of the PublicIP resource
     * Updating/Deleting/Failed.
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
     * Get the peeringType value.
     *
     * @return the peeringType value
     */
    public String peeringType() {
        return this.peeringType;
    }

    /**
     * Set the peeringType value.
     *
     * @param peeringType the peeringType value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withPeeringType(String peeringType) {
        this.peeringType = peeringType;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public String state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Get the azureASN value.
     *
     * @return the azureASN value
     */
    public Integer azureASN() {
        return this.azureASN;
    }

    /**
     * Set the azureASN value.
     *
     * @param azureASN the azureASN value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withAzureASN(Integer azureASN) {
        this.azureASN = azureASN;
        return this;
    }

    /**
     * Get the peerASN value.
     *
     * @return the peerASN value
     */
    public Integer peerASN() {
        return this.peerASN;
    }

    /**
     * Set the peerASN value.
     *
     * @param peerASN the peerASN value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withPeerASN(Integer peerASN) {
        this.peerASN = peerASN;
        return this;
    }

    /**
     * Get the primaryPeerAddressPrefix value.
     *
     * @return the primaryPeerAddressPrefix value
     */
    public String primaryPeerAddressPrefix() {
        return this.primaryPeerAddressPrefix;
    }

    /**
     * Set the primaryPeerAddressPrefix value.
     *
     * @param primaryPeerAddressPrefix the primaryPeerAddressPrefix value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withPrimaryPeerAddressPrefix(String primaryPeerAddressPrefix) {
        this.primaryPeerAddressPrefix = primaryPeerAddressPrefix;
        return this;
    }

    /**
     * Get the secondaryPeerAddressPrefix value.
     *
     * @return the secondaryPeerAddressPrefix value
     */
    public String secondaryPeerAddressPrefix() {
        return this.secondaryPeerAddressPrefix;
    }

    /**
     * Set the secondaryPeerAddressPrefix value.
     *
     * @param secondaryPeerAddressPrefix the secondaryPeerAddressPrefix value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withSecondaryPeerAddressPrefix(String secondaryPeerAddressPrefix) {
        this.secondaryPeerAddressPrefix = secondaryPeerAddressPrefix;
        return this;
    }

    /**
     * Get the primaryAzurePort value.
     *
     * @return the primaryAzurePort value
     */
    public String primaryAzurePort() {
        return this.primaryAzurePort;
    }

    /**
     * Set the primaryAzurePort value.
     *
     * @param primaryAzurePort the primaryAzurePort value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withPrimaryAzurePort(String primaryAzurePort) {
        this.primaryAzurePort = primaryAzurePort;
        return this;
    }

    /**
     * Get the secondaryAzurePort value.
     *
     * @return the secondaryAzurePort value
     */
    public String secondaryAzurePort() {
        return this.secondaryAzurePort;
    }

    /**
     * Set the secondaryAzurePort value.
     *
     * @param secondaryAzurePort the secondaryAzurePort value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withSecondaryAzurePort(String secondaryAzurePort) {
        this.secondaryAzurePort = secondaryAzurePort;
        return this;
    }

    /**
     * Get the sharedKey value.
     *
     * @return the sharedKey value
     */
    public String sharedKey() {
        return this.sharedKey;
    }

    /**
     * Set the sharedKey value.
     *
     * @param sharedKey the sharedKey value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
        return this;
    }

    /**
     * Get the vlanId value.
     *
     * @return the vlanId value
     */
    public Integer vlanId() {
        return this.vlanId;
    }

    /**
     * Set the vlanId value.
     *
     * @param vlanId the vlanId value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withVlanId(Integer vlanId) {
        this.vlanId = vlanId;
        return this;
    }

    /**
     * Get the microsoftPeeringConfig value.
     *
     * @return the microsoftPeeringConfig value
     */
    public ExpressRouteCircuitPeeringConfig microsoftPeeringConfig() {
        return this.microsoftPeeringConfig;
    }

    /**
     * Set the microsoftPeeringConfig value.
     *
     * @param microsoftPeeringConfig the microsoftPeeringConfig value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withMicrosoftPeeringConfig(ExpressRouteCircuitPeeringConfig microsoftPeeringConfig) {
        this.microsoftPeeringConfig = microsoftPeeringConfig;
        return this;
    }

    /**
     * Get the stats value.
     *
     * @return the stats value
     */
    public ExpressRouteCircuitStatsInner stats() {
        return this.stats;
    }

    /**
     * Set the stats value.
     *
     * @param stats the stats value to set
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withStats(ExpressRouteCircuitStatsInner stats) {
        this.stats = stats;
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
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withProvisioningState(String provisioningState) {
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
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withName(String name) {
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
     * @return the ExpressRouteCircuitPeeringInner object itself.
     */
    public ExpressRouteCircuitPeeringInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
