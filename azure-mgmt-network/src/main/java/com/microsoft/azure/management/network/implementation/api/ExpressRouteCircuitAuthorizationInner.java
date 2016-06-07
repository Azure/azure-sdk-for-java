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
 * Authorization in a ExpressRouteCircuit resource.
 */
@JsonFlatten
public class ExpressRouteCircuitAuthorizationInner extends SubResource {
    /**
     * Gets or sets the authorization key.
     */
    @JsonProperty(value = "properties.authorizationKey")
    private String authorizationKey;

    /**
     * Gets or sets AuthorizationUseStatus. Possible values include:
     * 'Available', 'InUse'.
     */
    @JsonProperty(value = "properties.authorizationUseStatus")
    private String authorizationUseStatus;

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
     * Get the authorizationKey value.
     *
     * @return the authorizationKey value
     */
    public String authorizationKey() {
        return this.authorizationKey;
    }

    /**
     * Set the authorizationKey value.
     *
     * @param authorizationKey the authorizationKey value to set
     * @return the ExpressRouteCircuitAuthorizationInner object itself.
     */
    public ExpressRouteCircuitAuthorizationInner withAuthorizationKey(String authorizationKey) {
        this.authorizationKey = authorizationKey;
        return this;
    }

    /**
     * Get the authorizationUseStatus value.
     *
     * @return the authorizationUseStatus value
     */
    public String authorizationUseStatus() {
        return this.authorizationUseStatus;
    }

    /**
     * Set the authorizationUseStatus value.
     *
     * @param authorizationUseStatus the authorizationUseStatus value to set
     * @return the ExpressRouteCircuitAuthorizationInner object itself.
     */
    public ExpressRouteCircuitAuthorizationInner withAuthorizationUseStatus(String authorizationUseStatus) {
        this.authorizationUseStatus = authorizationUseStatus;
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
     * @return the ExpressRouteCircuitAuthorizationInner object itself.
     */
    public ExpressRouteCircuitAuthorizationInner withProvisioningState(String provisioningState) {
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
     * @return the ExpressRouteCircuitAuthorizationInner object itself.
     */
    public ExpressRouteCircuitAuthorizationInner withName(String name) {
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
     * @return the ExpressRouteCircuitAuthorizationInner object itself.
     */
    public ExpressRouteCircuitAuthorizationInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
