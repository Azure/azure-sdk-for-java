/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * VNETInfo contract. This contract is public and is a stripped down version
 * of VNETInfoInternal.
 */
@JsonFlatten
public class VnetInfoInner extends Resource {
    /**
     * The vnet resource id.
     */
    @JsonProperty(value = "properties.vnetResourceId")
    private String vnetResourceId;

    /**
     * The client certificate thumbprint.
     */
    @JsonProperty(value = "properties.certThumbprint")
    private String certThumbprint;

    /**
     * A certificate file (.cer) blob containing the public key of the private
     * key used to authenticate a
     * Point-To-Site VPN connection.
     */
    @JsonProperty(value = "properties.certBlob")
    private String certBlob;

    /**
     * The routes that this virtual network connection uses.
     */
    @JsonProperty(value = "properties.routes")
    private List<VnetRouteInner> routes;

    /**
     * Flag to determine if a resync is required.
     */
    @JsonProperty(value = "properties.resyncRequired")
    private Boolean resyncRequired;

    /**
     * Dns servers to be used by this VNET. This should be a comma-separated
     * list of IP addresses.
     */
    @JsonProperty(value = "properties.dnsServers")
    private String dnsServers;

    /**
     * Get the vnetResourceId value.
     *
     * @return the vnetResourceId value
     */
    public String vnetResourceId() {
        return this.vnetResourceId;
    }

    /**
     * Set the vnetResourceId value.
     *
     * @param vnetResourceId the vnetResourceId value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withVnetResourceId(String vnetResourceId) {
        this.vnetResourceId = vnetResourceId;
        return this;
    }

    /**
     * Get the certThumbprint value.
     *
     * @return the certThumbprint value
     */
    public String certThumbprint() {
        return this.certThumbprint;
    }

    /**
     * Set the certThumbprint value.
     *
     * @param certThumbprint the certThumbprint value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withCertThumbprint(String certThumbprint) {
        this.certThumbprint = certThumbprint;
        return this;
    }

    /**
     * Get the certBlob value.
     *
     * @return the certBlob value
     */
    public String certBlob() {
        return this.certBlob;
    }

    /**
     * Set the certBlob value.
     *
     * @param certBlob the certBlob value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withCertBlob(String certBlob) {
        this.certBlob = certBlob;
        return this;
    }

    /**
     * Get the routes value.
     *
     * @return the routes value
     */
    public List<VnetRouteInner> routes() {
        return this.routes;
    }

    /**
     * Set the routes value.
     *
     * @param routes the routes value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withRoutes(List<VnetRouteInner> routes) {
        this.routes = routes;
        return this;
    }

    /**
     * Get the resyncRequired value.
     *
     * @return the resyncRequired value
     */
    public Boolean resyncRequired() {
        return this.resyncRequired;
    }

    /**
     * Set the resyncRequired value.
     *
     * @param resyncRequired the resyncRequired value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withResyncRequired(Boolean resyncRequired) {
        this.resyncRequired = resyncRequired;
        return this;
    }

    /**
     * Get the dnsServers value.
     *
     * @return the dnsServers value
     */
    public String dnsServers() {
        return this.dnsServers;
    }

    /**
     * Set the dnsServers value.
     *
     * @param dnsServers the dnsServers value to set
     * @return the VnetInfoInner object itself.
     */
    public VnetInfoInner withDnsServers(String dnsServers) {
        this.dnsServers = dnsServers;
        return this;
    }

}
