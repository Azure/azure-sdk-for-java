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
 * Inbound NAT rule of the loadbalancer.
 */
@JsonFlatten
public class InboundNatRule extends SubResource {
    /**
     * Gets or sets a reference to frontend IP Addresses.
     */
    @JsonProperty(value = "properties.frontendIPConfiguration")
    private SubResource frontendIPConfiguration;

    /**
     * Gets or sets a reference to a private ip address defined on a
     * NetworkInterface of a VM. Traffic sent to frontendPort of each of the
     * frontendIPConfigurations is forwarded to the backed IP.
     */
    @JsonProperty(value = "properties.backendIPConfiguration")
    private NetworkInterfaceIPConfiguration backendIPConfiguration;

    /**
     * Gets or sets the transport potocol for the external endpoint. Possible
     * values are Udp or Tcp. Possible values include: 'Udp', 'Tcp'.
     */
    @JsonProperty(value = "properties.protocol")
    private String protocol;

    /**
     * Gets or sets the port for the external endpoint. You can spcify any
     * port number you choose, but the port numbers specified for each role
     * in the service must be unique. Possible values range between 1 and
     * 65535, inclusive.
     */
    @JsonProperty(value = "properties.frontendPort")
    private Integer frontendPort;

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
    @JsonProperty(value = "properties.backendPort")
    private Integer backendPort;

    /**
     * Gets or sets the timeout for the Tcp idle connection. The value can be
     * set between 4 and 30 minutes. The default value is 4 minutes. This
     * emlement is only used when the protocol is set to Tcp.
     */
    @JsonProperty(value = "properties.idleTimeoutInMinutes")
    private Integer idleTimeoutInMinutes;

    /**
     * Configures a virtual machine's endpoint for the floating IP capability
     * required to configure a SQL AlwaysOn availability Group. This setting
     * is required when using the SQL Always ON availability Groups in SQL
     * server. This setting can't be changed after you create the endpoint.
     */
    @JsonProperty(value = "properties.enableFloatingIP")
    private Boolean enableFloatingIP;

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
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withFrontendIPConfiguration(SubResource frontendIPConfiguration) {
        this.frontendIPConfiguration = frontendIPConfiguration;
        return this;
    }

    /**
     * Get the backendIPConfiguration value.
     *
     * @return the backendIPConfiguration value
     */
    public NetworkInterfaceIPConfiguration backendIPConfiguration() {
        return this.backendIPConfiguration;
    }

    /**
     * Set the backendIPConfiguration value.
     *
     * @param backendIPConfiguration the backendIPConfiguration value to set
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withBackendIPConfiguration(NetworkInterfaceIPConfiguration backendIPConfiguration) {
        this.backendIPConfiguration = backendIPConfiguration;
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
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the frontendPort value.
     *
     * @return the frontendPort value
     */
    public Integer frontendPort() {
        return this.frontendPort;
    }

    /**
     * Set the frontendPort value.
     *
     * @param frontendPort the frontendPort value to set
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withFrontendPort(Integer frontendPort) {
        this.frontendPort = frontendPort;
        return this;
    }

    /**
     * Get the backendPort value.
     *
     * @return the backendPort value
     */
    public Integer backendPort() {
        return this.backendPort;
    }

    /**
     * Set the backendPort value.
     *
     * @param backendPort the backendPort value to set
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
        return this;
    }

    /**
     * Get the idleTimeoutInMinutes value.
     *
     * @return the idleTimeoutInMinutes value
     */
    public Integer idleTimeoutInMinutes() {
        return this.idleTimeoutInMinutes;
    }

    /**
     * Set the idleTimeoutInMinutes value.
     *
     * @param idleTimeoutInMinutes the idleTimeoutInMinutes value to set
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withIdleTimeoutInMinutes(Integer idleTimeoutInMinutes) {
        this.idleTimeoutInMinutes = idleTimeoutInMinutes;
        return this;
    }

    /**
     * Get the enableFloatingIP value.
     *
     * @return the enableFloatingIP value
     */
    public Boolean enableFloatingIP() {
        return this.enableFloatingIP;
    }

    /**
     * Set the enableFloatingIP value.
     *
     * @param enableFloatingIP the enableFloatingIP value to set
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withEnableFloatingIP(Boolean enableFloatingIP) {
        this.enableFloatingIP = enableFloatingIP;
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
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withProvisioningState(String provisioningState) {
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
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withName(String name) {
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
     * @return the InboundNatRule object itself.
     */
    public InboundNatRule withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
