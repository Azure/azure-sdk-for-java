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
 * Probe of application gateway.
 */
@JsonFlatten
public class ApplicationGatewayProbe extends SubResource {
    /**
     * Gets or sets the protocol. Possible values include: 'Http', 'Https'.
     */
    @JsonProperty(value = "properties.protocol")
    private String protocol;

    /**
     * Gets or sets the host to send probe to.
     */
    @JsonProperty(value = "properties.host")
    private String host;

    /**
     * Gets or sets the relative path of probe.
     */
    @JsonProperty(value = "properties.path")
    private String path;

    /**
     * Gets or sets probing interval in seconds.
     */
    @JsonProperty(value = "properties.interval")
    private Integer interval;

    /**
     * Gets or sets probing timeout in seconds.
     */
    @JsonProperty(value = "properties.timeout")
    private Integer timeout;

    /**
     * Gets or sets probing unhealthy threshold.
     */
    @JsonProperty(value = "properties.unhealthyThreshold")
    private Integer unhealthyThreshold;

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
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the host value.
     *
     * @return the host value
     */
    public String host() {
        return this.host;
    }

    /**
     * Set the host value.
     *
     * @param host the host value to set
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Get the path value.
     *
     * @return the path value
     */
    public String path() {
        return this.path;
    }

    /**
     * Set the path value.
     *
     * @param path the path value to set
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Get the interval value.
     *
     * @return the interval value
     */
    public Integer interval() {
        return this.interval;
    }

    /**
     * Set the interval value.
     *
     * @param interval the interval value to set
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withInterval(Integer interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Get the timeout value.
     *
     * @return the timeout value
     */
    public Integer timeout() {
        return this.timeout;
    }

    /**
     * Set the timeout value.
     *
     * @param timeout the timeout value to set
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Get the unhealthyThreshold value.
     *
     * @return the unhealthyThreshold value
     */
    public Integer unhealthyThreshold() {
        return this.unhealthyThreshold;
    }

    /**
     * Set the unhealthyThreshold value.
     *
     * @param unhealthyThreshold the unhealthyThreshold value to set
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
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
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withName(String name) {
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
     * @return the ApplicationGatewayProbe object itself.
     */
    public ApplicationGatewayProbe withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
