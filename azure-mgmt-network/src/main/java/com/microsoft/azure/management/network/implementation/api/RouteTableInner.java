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
 * RouteTable resource.
 */
@JsonFlatten
public class RouteTableInner extends Resource {
    /**
     * Gets or sets Routes in a Route Table.
     */
    @JsonProperty(value = "properties.routes")
    private List<RouteInner> routes;

    /**
     * Gets collection of references to subnets.
     */
    @JsonProperty(value = "properties.subnets")
    private List<SubnetInner> subnets;

    /**
     * Gets or sets Provisioning state of the resource
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
     * Get the routes value.
     *
     * @return the routes value
     */
    public List<RouteInner> routes() {
        return this.routes;
    }

    /**
     * Set the routes value.
     *
     * @param routes the routes value to set
     * @return the RouteTableInner object itself.
     */
    public RouteTableInner withRoutes(List<RouteInner> routes) {
        this.routes = routes;
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
     * @return the RouteTableInner object itself.
     */
    public RouteTableInner withSubnets(List<SubnetInner> subnets) {
        this.subnets = subnets;
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
     * @return the RouteTableInner object itself.
     */
    public RouteTableInner withProvisioningState(String provisioningState) {
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
     * @return the RouteTableInner object itself.
     */
    public RouteTableInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
