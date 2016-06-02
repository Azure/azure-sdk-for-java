/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Request routing rule of application gateway.
 */
@JsonFlatten
public class ApplicationGatewayRequestRoutingRule extends SubResource {
    /**
     * Gets or sets the rule type. Possible values include: 'Basic',
     * 'PathBasedRouting'.
     */
    @JsonProperty(value = "properties.ruleType")
    private String ruleType;

    /**
     * Gets or sets backend address pool resource of application gateway.
     */
    @JsonProperty(value = "properties.backendAddressPool")
    private SubResource backendAddressPool;

    /**
     * Gets or sets frontend port resource of application gateway.
     */
    @JsonProperty(value = "properties.backendHttpSettings")
    private SubResource backendHttpSettings;

    /**
     * Gets or sets http listener resource of application gateway.
     */
    @JsonProperty(value = "properties.httpListener")
    private SubResource httpListener;

    /**
     * Gets or sets url path map resource of application gateway.
     */
    @JsonProperty(value = "properties.urlPathMap")
    private SubResource urlPathMap;

    /**
     * Gets or sets Provisioning state of the request routing rule resource
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
     * Get the ruleType value.
     *
     * @return the ruleType value
     */
    public String ruleType() {
        return this.ruleType;
    }

    /**
     * Set the ruleType value.
     *
     * @param ruleType the ruleType value to set
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withRuleType(String ruleType) {
        this.ruleType = ruleType;
        return this;
    }

    /**
     * Get the backendAddressPool value.
     *
     * @return the backendAddressPool value
     */
    public SubResource backendAddressPool() {
        return this.backendAddressPool;
    }

    /**
     * Set the backendAddressPool value.
     *
     * @param backendAddressPool the backendAddressPool value to set
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withBackendAddressPool(SubResource backendAddressPool) {
        this.backendAddressPool = backendAddressPool;
        return this;
    }

    /**
     * Get the backendHttpSettings value.
     *
     * @return the backendHttpSettings value
     */
    public SubResource backendHttpSettings() {
        return this.backendHttpSettings;
    }

    /**
     * Set the backendHttpSettings value.
     *
     * @param backendHttpSettings the backendHttpSettings value to set
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withBackendHttpSettings(SubResource backendHttpSettings) {
        this.backendHttpSettings = backendHttpSettings;
        return this;
    }

    /**
     * Get the httpListener value.
     *
     * @return the httpListener value
     */
    public SubResource httpListener() {
        return this.httpListener;
    }

    /**
     * Set the httpListener value.
     *
     * @param httpListener the httpListener value to set
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withHttpListener(SubResource httpListener) {
        this.httpListener = httpListener;
        return this;
    }

    /**
     * Get the urlPathMap value.
     *
     * @return the urlPathMap value
     */
    public SubResource urlPathMap() {
        return this.urlPathMap;
    }

    /**
     * Set the urlPathMap value.
     *
     * @param urlPathMap the urlPathMap value to set
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withUrlPathMap(SubResource urlPathMap) {
        this.urlPathMap = urlPathMap;
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
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withName(String name) {
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
     * @return the ApplicationGatewayRequestRoutingRule object itself.
     */
    public ApplicationGatewayRequestRoutingRule withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
