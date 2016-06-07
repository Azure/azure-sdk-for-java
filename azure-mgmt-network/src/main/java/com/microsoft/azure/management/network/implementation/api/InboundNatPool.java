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
 * Inbound NAT pool of the loadbalancer.
 */
@JsonFlatten
public class InboundNatPool extends SubResource {
    /**
     * Gets or sets a reference to frontend IP Addresses.
     */
    @JsonProperty(value = "properties.frontendIPConfiguration")
    private SubResource frontendIPConfiguration;

    /**
     * Gets or sets the transport potocol for the external endpoint. Possible
     * values are Udp or Tcp. Possible values include: 'Udp', 'Tcp'.
     */
    @JsonProperty(value = "properties.protocol", required = true)
    private String protocol;

    /**
     * Gets or sets the starting port range for the NAT pool. You can spcify
     * any port number you choose, but the port numbers specified for each
     * role in the service must be unique. Possible values range between 1
     * and 65535, inclusive.
     */
    @JsonProperty(value = "properties.frontendPortRangeStart", required = true)
    private int frontendPortRangeStart;

    /**
     * Gets or sets the ending port range for the NAT pool. You can spcify any
     * port number you choose, but the port numbers specified for each role
     * in the service must be unique. Possible values range between 1 and
     * 65535, inclusive.
     */
    @JsonProperty(value = "properties.frontendPortRangeEnd", required = true)
    private int frontendPortRangeEnd;

    /**
     * Gets or sets a port used for internal connections on the endpoint. The
     * localPort attribute maps the eternal port of the endpoint to an
     * internal port on a role. This is useful in scenarios where a role must
     * communicate to an internal compotnent on a port that is different from
     * the one that is exposed externally. If not specified, the value of
     * localPort is the same as the port attribute. Set the value of
     * localPort to '*' to automatically assign an unallocated port that is
     * discoverable using the runtime API.
     */
    @JsonProperty(value = "properties.backendPort", required = true)
    private int backendPort;

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
     * Get the frontendIPConfiguration value.
     *
     * @return the frontendIPConfiguration value
     */
    public SubResource frontendIPConfiguration() {
        return this.frontendIPConfiguration;
    }

    /**
     * Set the frontendIPConfiguration value.
     *
     * @param frontendIPConfiguration the frontendIPConfiguration value to set
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withFrontendIPConfiguration(SubResource frontendIPConfiguration) {
        this.frontendIPConfiguration = frontendIPConfiguration;
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
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the frontendPortRangeStart value.
     *
     * @return the frontendPortRangeStart value
     */
    public int frontendPortRangeStart() {
        return this.frontendPortRangeStart;
    }

    /**
     * Set the frontendPortRangeStart value.
     *
     * @param frontendPortRangeStart the frontendPortRangeStart value to set
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withFrontendPortRangeStart(int frontendPortRangeStart) {
        this.frontendPortRangeStart = frontendPortRangeStart;
        return this;
    }

    /**
     * Get the frontendPortRangeEnd value.
     *
     * @return the frontendPortRangeEnd value
     */
    public int frontendPortRangeEnd() {
        return this.frontendPortRangeEnd;
    }

    /**
     * Set the frontendPortRangeEnd value.
     *
     * @param frontendPortRangeEnd the frontendPortRangeEnd value to set
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withFrontendPortRangeEnd(int frontendPortRangeEnd) {
        this.frontendPortRangeEnd = frontendPortRangeEnd;
        return this;
    }

    /**
     * Get the backendPort value.
     *
     * @return the backendPort value
     */
    public int backendPort() {
        return this.backendPort;
    }

    /**
     * Set the backendPort value.
     *
     * @param backendPort the backendPort value to set
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withBackendPort(int backendPort) {
        this.backendPort = backendPort;
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
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withProvisioningState(String provisioningState) {
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
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withName(String name) {
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
     * @return the InboundNatPool object itself.
     */
    public InboundNatPool withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
