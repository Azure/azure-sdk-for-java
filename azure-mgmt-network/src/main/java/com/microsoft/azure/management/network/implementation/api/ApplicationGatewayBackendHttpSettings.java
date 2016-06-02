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
 * Backend address pool settings of application gateway.
 */
@JsonFlatten
public class ApplicationGatewayBackendHttpSettings extends SubResource {
    /**
     * Gets or sets the port.
     */
    @JsonProperty(value = "properties.port")
    private Integer port;

    /**
     * Gets or sets the protocol. Possible values include: 'Http', 'Https'.
     */
    @JsonProperty(value = "properties.protocol")
    private String protocol;

    /**
     * Gets or sets the cookie affinity. Possible values include: 'Enabled',
     * 'Disabled'.
     */
    @JsonProperty(value = "properties.cookieBasedAffinity")
    private String cookieBasedAffinity;

    /**
     * Gets or sets request timeout.
     */
    @JsonProperty(value = "properties.requestTimeout")
    private Integer requestTimeout;

    /**
     * Gets or sets probe resource of application gateway.
     */
    @JsonProperty(value = "properties.probe")
    private SubResource probe;

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
     * Get the port value.
     *
     * @return the port value
     */
    public Integer port() {
        return this.port;
    }

    /**
     * Set the port value.
     *
     * @param port the port value to set
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withPort(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Get the protocol value.
     *
     * @return the protocol value
     */
    public String protocol() {
        return this.protocol;
    }

    /**
     * Set the protocol value.
     *
     * @param protocol the protocol value to set
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the cookieBasedAffinity value.
     *
     * @return the cookieBasedAffinity value
     */
    public String cookieBasedAffinity() {
        return this.cookieBasedAffinity;
    }

    /**
     * Set the cookieBasedAffinity value.
     *
     * @param cookieBasedAffinity the cookieBasedAffinity value to set
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withCookieBasedAffinity(String cookieBasedAffinity) {
        this.cookieBasedAffinity = cookieBasedAffinity;
        return this;
    }

    /**
     * Get the requestTimeout value.
     *
     * @return the requestTimeout value
     */
    public Integer requestTimeout() {
        return this.requestTimeout;
    }

    /**
     * Set the requestTimeout value.
     *
     * @param requestTimeout the requestTimeout value to set
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Get the probe value.
     *
     * @return the probe value
     */
    public SubResource probe() {
        return this.probe;
    }

    /**
     * Set the probe value.
     *
     * @param probe the probe value to set
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withProbe(SubResource probe) {
        this.probe = probe;
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
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withName(String name) {
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
     * @return the ApplicationGatewayBackendHttpSettings object itself.
     */
    public ApplicationGatewayBackendHttpSettings withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
