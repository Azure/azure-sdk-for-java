/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * The VnetGateway contract. This is used to give the vnet gateway access to
 * the VPN package.
 */
@JsonFlatten
public class VnetGatewayInner extends Resource {
    /**
     * The VNET name.
     */
    @JsonProperty(value = "properties.vnetName")
    private String vnetName;

    /**
     * The URI where the Vpn package can be downloaded.
     */
    @JsonProperty(value = "properties.vpnPackageUri")
    private String vpnPackageUri;

    /**
     * Get the vnetName value.
     *
     * @return the vnetName value
     */
    public String vnetName() {
        return this.vnetName;
    }

    /**
     * Set the vnetName value.
     *
     * @param vnetName the vnetName value to set
     * @return the VnetGatewayInner object itself.
     */
    public VnetGatewayInner withVnetName(String vnetName) {
        this.vnetName = vnetName;
        return this;
    }

    /**
     * Get the vpnPackageUri value.
     *
     * @return the vpnPackageUri value
     */
    public String vpnPackageUri() {
        return this.vpnPackageUri;
    }

    /**
     * Set the vpnPackageUri value.
     *
     * @param vpnPackageUri the vpnPackageUri value to set
     * @return the VnetGatewayInner object itself.
     */
    public VnetGatewayInner withVpnPackageUri(String vpnPackageUri) {
        this.vpnPackageUri = vpnPackageUri;
        return this;
    }

}
