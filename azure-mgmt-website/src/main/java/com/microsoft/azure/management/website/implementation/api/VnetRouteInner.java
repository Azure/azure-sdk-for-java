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
 * VnetRoute contract used to pass routing information for a vnet.
 */
@JsonFlatten
public class VnetRouteInner extends Resource {
    /**
     * The name of this route. This is only returned by the server and does
     * not need to be set by the client.
     */
    @JsonProperty(value = "properties.name")
    private String vnetRouteName;

    /**
     * The starting address for this route. This may also include a CIDR
     * notation, in which case the end address must not be specified.
     */
    @JsonProperty(value = "properties.startAddress")
    private String startAddress;

    /**
     * The ending address for this route. If the start address is specified in
     * CIDR notation, this must be omitted.
     */
    @JsonProperty(value = "properties.endAddress")
    private String endAddress;

    /**
     * The type of route this is:
     * DEFAULT - By default, every web app has routes to the local
     * address ranges specified by RFC1918
     * INHERITED - Routes inherited from the real Virtual Network
     * routes
     * STATIC - Static route set on the web app only
     * 
     * These values will be used for syncing a Web App's routes
     * with those from a Virtual Network. This operation will clear all
     * DEFAULT and INHERITED routes and replace them
     * with new INHERITED routes.
     */
    @JsonProperty(value = "properties.routeType")
    private String routeType;

    /**
     * Get the vnetRouteName value.
     *
     * @return the vnetRouteName value
     */
    public String vnetRouteName() {
        return this.vnetRouteName;
    }

    /**
     * Set the vnetRouteName value.
     *
     * @param vnetRouteName the vnetRouteName value to set
     * @return the VnetRouteInner object itself.
     */
    public VnetRouteInner withVnetRouteName(String vnetRouteName) {
        this.vnetRouteName = vnetRouteName;
        return this;
    }

    /**
     * Get the startAddress value.
     *
     * @return the startAddress value
     */
    public String startAddress() {
        return this.startAddress;
    }

    /**
     * Set the startAddress value.
     *
     * @param startAddress the startAddress value to set
     * @return the VnetRouteInner object itself.
     */
    public VnetRouteInner withStartAddress(String startAddress) {
        this.startAddress = startAddress;
        return this;
    }

    /**
     * Get the endAddress value.
     *
     * @return the endAddress value
     */
    public String endAddress() {
        return this.endAddress;
    }

    /**
     * Set the endAddress value.
     *
     * @param endAddress the endAddress value to set
     * @return the VnetRouteInner object itself.
     */
    public VnetRouteInner withEndAddress(String endAddress) {
        this.endAddress = endAddress;
        return this;
    }

    /**
     * Get the routeType value.
     *
     * @return the routeType value
     */
    public String routeType() {
        return this.routeType;
    }

    /**
     * Set the routeType value.
     *
     * @param routeType the routeType value to set
     * @return the VnetRouteInner object itself.
     */
    public VnetRouteInner withRouteType(String routeType) {
        this.routeType = routeType;
        return this;
    }

}
