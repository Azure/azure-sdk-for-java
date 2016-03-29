/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A common class for general resource information.
 */
@JsonFlatten
public class LocalNetworkGateway extends Resource {
    /**
     * Local network site Address space.
     */
    @JsonProperty(value = "properties.localNetworkAddressSpace")
    private AddressSpace localNetworkAddressSpace;

    /**
     * IP address of local network gateway.
     */
    @JsonProperty(value = "properties.gatewayIpAddress")
    private String gatewayIpAddress;

    /**
     * Local network gateway's BGP speaker settings.
     */
    @JsonProperty(value = "properties.bgpSettings")
    private BgpSettings bgpSettings;

    /**
     * Gets or sets resource guid property of the LocalNetworkGateway resource.
     */
    @JsonProperty(value = "properties.resourceGuid")
    private String resourceGuid;

    /**
     * Gets or sets Provisioning state of the LocalNetworkGateway resource
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
     * Get the localNetworkAddressSpace value.
     *
     * @return the localNetworkAddressSpace value
     */
    public AddressSpace getLocalNetworkAddressSpace() {
        return this.localNetworkAddressSpace;
    }

    /**
     * Set the localNetworkAddressSpace value.
     *
     * @param localNetworkAddressSpace the localNetworkAddressSpace value to set
     */
    public void setLocalNetworkAddressSpace(AddressSpace localNetworkAddressSpace) {
        this.localNetworkAddressSpace = localNetworkAddressSpace;
    }

    /**
     * Get the gatewayIpAddress value.
     *
     * @return the gatewayIpAddress value
     */
    public String getGatewayIpAddress() {
        return this.gatewayIpAddress;
    }

    /**
     * Set the gatewayIpAddress value.
     *
     * @param gatewayIpAddress the gatewayIpAddress value to set
     */
    public void setGatewayIpAddress(String gatewayIpAddress) {
        this.gatewayIpAddress = gatewayIpAddress;
    }

    /**
     * Get the bgpSettings value.
     *
     * @return the bgpSettings value
     */
    public BgpSettings getBgpSettings() {
        return this.bgpSettings;
    }

    /**
     * Set the bgpSettings value.
     *
     * @param bgpSettings the bgpSettings value to set
     */
    public void setBgpSettings(BgpSettings bgpSettings) {
        this.bgpSettings = bgpSettings;
    }

    /**
     * Get the resourceGuid value.
     *
     * @return the resourceGuid value
     */
    public String getResourceGuid() {
        return this.resourceGuid;
    }

    /**
     * Set the resourceGuid value.
     *
     * @param resourceGuid the resourceGuid value to set
     */
    public void setResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String getProvisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     */
    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * Get the etag value.
     *
     * @return the etag value
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Set the etag value.
     *
     * @param etag the etag value to set
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

}
