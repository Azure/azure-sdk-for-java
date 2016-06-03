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
 * Http listener of application gateway.
 */
@JsonFlatten
public class ApplicationGatewayHttpListener extends SubResource {
    /**
     * Gets or sets frontend IP configuration resource of application gateway.
     */
    @JsonProperty(value = "properties.frontendIPConfiguration")
    private SubResource frontendIPConfiguration;

    /**
     * Gets or sets frontend port resource of application gateway.
     */
    @JsonProperty(value = "properties.frontendPort")
    private SubResource frontendPort;

    /**
     * Gets or sets the protocol. Possible values include: 'Http', 'Https'.
     */
    @JsonProperty(value = "properties.protocol")
    private String protocol;

    /**
     * Gets or sets the host name of http listener.
     */
    @JsonProperty(value = "properties.hostName")
    private String hostName;

    /**
     * Gets or sets ssl certificate resource of application gateway.
     */
    @JsonProperty(value = "properties.sslCertificate")
    private SubResource sslCertificate;

    /**
     * Gets or sets the requireServerNameIndication of http listener.
     */
    @JsonProperty(value = "properties.requireServerNameIndication")
    private Boolean requireServerNameIndication;

    /**
     * Gets or sets Provisioning state of the http listener resource
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
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withFrontendIPConfiguration(SubResource frontendIPConfiguration) {
        this.frontendIPConfiguration = frontendIPConfiguration;
        return this;
    }

    /**
     * Get the frontendPort value.
     *
     * @return the frontendPort value
     */
    public SubResource frontendPort() {
        return this.frontendPort;
    }

    /**
     * Set the frontendPort value.
     *
     * @param frontendPort the frontendPort value to set
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withFrontendPort(SubResource frontendPort) {
        this.frontendPort = frontendPort;
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
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the hostName value.
     *
     * @return the hostName value
     */
    public String hostName() {
        return this.hostName;
    }

    /**
     * Set the hostName value.
     *
     * @param hostName the hostName value to set
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Get the sslCertificate value.
     *
     * @return the sslCertificate value
     */
    public SubResource sslCertificate() {
        return this.sslCertificate;
    }

    /**
     * Set the sslCertificate value.
     *
     * @param sslCertificate the sslCertificate value to set
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withSslCertificate(SubResource sslCertificate) {
        this.sslCertificate = sslCertificate;
        return this;
    }

    /**
     * Get the requireServerNameIndication value.
     *
     * @return the requireServerNameIndication value
     */
    public Boolean requireServerNameIndication() {
        return this.requireServerNameIndication;
    }

    /**
     * Set the requireServerNameIndication value.
     *
     * @param requireServerNameIndication the requireServerNameIndication value to set
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withRequireServerNameIndication(Boolean requireServerNameIndication) {
        this.requireServerNameIndication = requireServerNameIndication;
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
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withName(String name) {
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
     * @return the ApplicationGatewayHttpListener object itself.
     */
    public ApplicationGatewayHttpListener withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
