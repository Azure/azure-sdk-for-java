/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.microsoft.azure.SubResource;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * UrlPathMap of application gateway.
 */
@JsonFlatten
public class ApplicationGatewayUrlPathMap extends SubResource {
    /**
     * Gets or sets default backend address pool resource of URL path map.
     */
    @JsonProperty(value = "properties.defaultBackendAddressPool")
    private SubResource defaultBackendAddressPool;

    /**
     * Gets or sets default backend http settings resource of URL path map.
     */
    @JsonProperty(value = "properties.defaultBackendHttpSettings")
    private SubResource defaultBackendHttpSettings;

    /**
     * Gets or sets path rule of URL path map resource.
     */
    @JsonProperty(value = "properties.pathRules")
    private List<ApplicationGatewayPathRule> pathRules;

    /**
     * Gets or sets Provisioning state of the backend http settings resource
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
     * Get the defaultBackendAddressPool value.
     *
     * @return the defaultBackendAddressPool value
     */
    public SubResource defaultBackendAddressPool() {
        return this.defaultBackendAddressPool;
    }

    /**
     * Set the defaultBackendAddressPool value.
     *
     * @param defaultBackendAddressPool the defaultBackendAddressPool value to set
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withDefaultBackendAddressPool(SubResource defaultBackendAddressPool) {
        this.defaultBackendAddressPool = defaultBackendAddressPool;
        return this;
    }

    /**
     * Get the defaultBackendHttpSettings value.
     *
     * @return the defaultBackendHttpSettings value
     */
    public SubResource defaultBackendHttpSettings() {
        return this.defaultBackendHttpSettings;
    }

    /**
     * Set the defaultBackendHttpSettings value.
     *
     * @param defaultBackendHttpSettings the defaultBackendHttpSettings value to set
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withDefaultBackendHttpSettings(SubResource defaultBackendHttpSettings) {
        this.defaultBackendHttpSettings = defaultBackendHttpSettings;
        return this;
    }

    /**
     * Get the pathRules value.
     *
     * @return the pathRules value
     */
    public List<ApplicationGatewayPathRule> pathRules() {
        return this.pathRules;
    }

    /**
     * Set the pathRules value.
     *
     * @param pathRules the pathRules value to set
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withPathRules(List<ApplicationGatewayPathRule> pathRules) {
        this.pathRules = pathRules;
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
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withName(String name) {
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
     * @return the ApplicationGatewayUrlPathMap object itself.
     */
    public ApplicationGatewayUrlPathMap withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
