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
 * Route resource.
 */
@JsonFlatten
public class RouteInner extends SubResource {
    /**
     * Gets or sets the destination CIDR to which the route applies.
     */
    @JsonProperty(value = "properties.addressPrefix")
    private String addressPrefix;

    /**
     * Gets or sets the type of Azure hop the packet should be sent to.
     * Possible values include: 'VirtualNetworkGateway', 'VnetLocal',
     * 'Internet', 'VirtualAppliance', 'None'.
     */
    @JsonProperty(value = "properties.nextHopType", required = true)
    private String nextHopType;

    /**
     * Gets or sets the IP address packets should be forwarded to. Next hop
     * values are only allowed in routes where the next hop type is
     * VirtualAppliance.
     */
    @JsonProperty(value = "properties.nextHopIpAddress")
    private String nextHopIpAddress;

    /**
     * Gets or sets Provisioning state of the resource
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
     * Get the addressPrefix value.
     *
     * @return the addressPrefix value
     */
    public String addressPrefix() {
        return this.addressPrefix;
    }

    /**
     * Set the addressPrefix value.
     *
     * @param addressPrefix the addressPrefix value to set
     * @return the RouteInner object itself.
     */
    public RouteInner withAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
        return this;
    }

    /**
     * Get the nextHopType value.
     *
     * @return the nextHopType value
     */
    public String nextHopType() {
        return this.nextHopType;
    }

    /**
     * Set the nextHopType value.
     *
     * @param nextHopType the nextHopType value to set
     * @return the RouteInner object itself.
     */
    public RouteInner withNextHopType(String nextHopType) {
        this.nextHopType = nextHopType;
        return this;
    }

    /**
     * Get the nextHopIpAddress value.
     *
     * @return the nextHopIpAddress value
     */
    public String nextHopIpAddress() {
        return this.nextHopIpAddress;
    }

    /**
     * Set the nextHopIpAddress value.
     *
     * @param nextHopIpAddress the nextHopIpAddress value to set
     * @return the RouteInner object itself.
     */
    public RouteInner withNextHopIpAddress(String nextHopIpAddress) {
        this.nextHopIpAddress = nextHopIpAddress;
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
     * @return the RouteInner object itself.
     */
    public RouteInner withProvisioningState(String provisioningState) {
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
     * @return the RouteInner object itself.
     */
    public RouteInner withName(String name) {
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
     * @return the RouteInner object itself.
     */
    public RouteInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
