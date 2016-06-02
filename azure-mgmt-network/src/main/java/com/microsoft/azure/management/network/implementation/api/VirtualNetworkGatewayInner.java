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
import com.microsoft.azure.Resource;

/**
 * A common class for general resource information.
 */
@JsonFlatten
public class VirtualNetworkGatewayInner extends Resource {
    /**
     * IpConfigurations for Virtual network gateway.
     */
    @JsonProperty(value = "properties.ipConfigurations")
    private List<VirtualNetworkGatewayIPConfiguration> ipConfigurations;

    /**
     * The type of this virtual network gateway. Possible values include:
     * 'Vpn', 'ExpressRoute'.
     */
    @JsonProperty(value = "properties.gatewayType")
    private String gatewayType;

    /**
     * The type of this virtual network gateway. Possible values include:
     * 'PolicyBased', 'RouteBased'.
     */
    @JsonProperty(value = "properties.vpnType")
    private String vpnType;

    /**
     * EnableBgp Flag.
     */
    @JsonProperty(value = "properties.enableBgp")
    private Boolean enableBgp;

    /**
     * Gets or sets the reference of the LocalNetworkGateway resource which
     * represents Local network site having default routes. Assign Null value
     * in case of removing existing default site setting.
     */
    @JsonProperty(value = "properties.gatewayDefaultSite")
    private SubResource gatewayDefaultSite;

    /**
     * Gets or sets the reference of the VirtualNetworkGatewaySku resource
     * which represents the sku selected for Virtual network gateway.
     */
    @JsonProperty(value = "properties.sku")
    private VirtualNetworkGatewaySku sku;

    /**
     * Gets or sets the reference of the VpnClientConfiguration resource which
     * represents the P2S VpnClient configurations.
     */
    @JsonProperty(value = "properties.vpnClientConfiguration")
    private VpnClientConfiguration vpnClientConfiguration;

    /**
     * Virtual network gateway's BGP speaker settings.
     */
    @JsonProperty(value = "properties.bgpSettings")
    private BgpSettings bgpSettings;

    /**
     * Gets or sets resource guid property of the VirtualNetworkGateway
     * resource.
     */
    @JsonProperty(value = "properties.resourceGuid")
    private String resourceGuid;

    /**
     * Gets or sets Provisioning state of the VirtualNetworkGateway resource
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
     * Get the ipConfigurations value.
     *
     * @return the ipConfigurations value
     */
    public List<VirtualNetworkGatewayIPConfiguration> ipConfigurations() {
        return this.ipConfigurations;
    }

    /**
     * Set the ipConfigurations value.
     *
     * @param ipConfigurations the ipConfigurations value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withIpConfigurations(List<VirtualNetworkGatewayIPConfiguration> ipConfigurations) {
        this.ipConfigurations = ipConfigurations;
        return this;
    }

    /**
     * Get the gatewayType value.
     *
     * @return the gatewayType value
     */
    public String gatewayType() {
        return this.gatewayType;
    }

    /**
     * Set the gatewayType value.
     *
     * @param gatewayType the gatewayType value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
        return this;
    }

    /**
     * Get the vpnType value.
     *
     * @return the vpnType value
     */
    public String vpnType() {
        return this.vpnType;
    }

    /**
     * Set the vpnType value.
     *
     * @param vpnType the vpnType value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withVpnType(String vpnType) {
        this.vpnType = vpnType;
        return this;
    }

    /**
     * Get the enableBgp value.
     *
     * @return the enableBgp value
     */
    public Boolean enableBgp() {
        return this.enableBgp;
    }

    /**
     * Set the enableBgp value.
     *
     * @param enableBgp the enableBgp value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withEnableBgp(Boolean enableBgp) {
        this.enableBgp = enableBgp;
        return this;
    }

    /**
     * Get the gatewayDefaultSite value.
     *
     * @return the gatewayDefaultSite value
     */
    public SubResource gatewayDefaultSite() {
        return this.gatewayDefaultSite;
    }

    /**
     * Set the gatewayDefaultSite value.
     *
     * @param gatewayDefaultSite the gatewayDefaultSite value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withGatewayDefaultSite(SubResource gatewayDefaultSite) {
        this.gatewayDefaultSite = gatewayDefaultSite;
        return this;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public VirtualNetworkGatewaySku sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withSku(VirtualNetworkGatewaySku sku) {
        this.sku = sku;
        return this;
    }

    /**
     * Get the vpnClientConfiguration value.
     *
     * @return the vpnClientConfiguration value
     */
    public VpnClientConfiguration vpnClientConfiguration() {
        return this.vpnClientConfiguration;
    }

    /**
     * Set the vpnClientConfiguration value.
     *
     * @param vpnClientConfiguration the vpnClientConfiguration value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withVpnClientConfiguration(VpnClientConfiguration vpnClientConfiguration) {
        this.vpnClientConfiguration = vpnClientConfiguration;
        return this;
    }

    /**
     * Get the bgpSettings value.
     *
     * @return the bgpSettings value
     */
    public BgpSettings bgpSettings() {
        return this.bgpSettings;
    }

    /**
     * Set the bgpSettings value.
     *
     * @param bgpSettings the bgpSettings value to set
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withBgpSettings(BgpSettings bgpSettings) {
        this.bgpSettings = bgpSettings;
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
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withResourceGuid(String resourceGuid) {
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
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withProvisioningState(String provisioningState) {
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
     * @return the VirtualNetworkGatewayInner object itself.
     */
    public VirtualNetworkGatewayInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
