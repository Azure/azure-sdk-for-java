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
 * ApplicationGateways resource.
 */
@JsonFlatten
public class ApplicationGatewayInner extends Resource {
    /**
     * Gets or sets sku of application gateway resource.
     */
    @JsonProperty(value = "properties.sku")
    private ApplicationGatewaySku sku;

    /**
     * Gets operational state of application gateway resource. Possible values
     * include: 'Stopped', 'Starting', 'Running', 'Stopping'.
     */
    @JsonProperty(value = "properties.operationalState", access = JsonProperty.Access.WRITE_ONLY)
    private String operationalState;

    /**
     * Gets or sets subnets of application gateway resource.
     */
    @JsonProperty(value = "properties.gatewayIPConfigurations")
    private List<ApplicationGatewayIPConfiguration> gatewayIPConfigurations;

    /**
     * Gets or sets ssl certificates of application gateway resource.
     */
    @JsonProperty(value = "properties.sslCertificates")
    private List<ApplicationGatewaySslCertificate> sslCertificates;

    /**
     * Gets or sets frontend IP addresses of application gateway resource.
     */
    @JsonProperty(value = "properties.frontendIPConfigurations")
    private List<ApplicationGatewayFrontendIPConfiguration> frontendIPConfigurations;

    /**
     * Gets or sets frontend ports of application gateway resource.
     */
    @JsonProperty(value = "properties.frontendPorts")
    private List<ApplicationGatewayFrontendPort> frontendPorts;

    /**
     * Gets or sets probes of application gateway resource.
     */
    @JsonProperty(value = "properties.probes")
    private List<ApplicationGatewayProbe> probes;

    /**
     * Gets or sets backend address pool of application gateway resource.
     */
    @JsonProperty(value = "properties.backendAddressPools")
    private List<ApplicationGatewayBackendAddressPool> backendAddressPools;

    /**
     * Gets or sets backend http settings of application gateway resource.
     */
    @JsonProperty(value = "properties.backendHttpSettingsCollection")
    private List<ApplicationGatewayBackendHttpSettings> backendHttpSettingsCollection;

    /**
     * Gets or sets HTTP listeners of application gateway resource.
     */
    @JsonProperty(value = "properties.httpListeners")
    private List<ApplicationGatewayHttpListener> httpListeners;

    /**
     * Gets or sets URL path map of application gateway resource.
     */
    @JsonProperty(value = "properties.urlPathMaps")
    private List<ApplicationGatewayUrlPathMap> urlPathMaps;

    /**
     * Gets or sets request routing rules of application gateway resource.
     */
    @JsonProperty(value = "properties.requestRoutingRules")
    private List<ApplicationGatewayRequestRoutingRule> requestRoutingRules;

    /**
     * Gets or sets resource guid property of the ApplicationGateway resource.
     */
    @JsonProperty(value = "properties.resourceGuid")
    private String resourceGuid;

    /**
     * Gets or sets Provisioning state of the ApplicationGateway resource
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
     * Get the sku value.
     *
     * @return the sku value
     */
    public ApplicationGatewaySku sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withSku(ApplicationGatewaySku sku) {
        this.sku = sku;
        return this;
    }

    /**
     * Get the operationalState value.
     *
     * @return the operationalState value
     */
    public String operationalState() {
        return this.operationalState;
    }

    /**
     * Get the gatewayIPConfigurations value.
     *
     * @return the gatewayIPConfigurations value
     */
    public List<ApplicationGatewayIPConfiguration> gatewayIPConfigurations() {
        return this.gatewayIPConfigurations;
    }

    /**
     * Set the gatewayIPConfigurations value.
     *
     * @param gatewayIPConfigurations the gatewayIPConfigurations value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withGatewayIPConfigurations(List<ApplicationGatewayIPConfiguration> gatewayIPConfigurations) {
        this.gatewayIPConfigurations = gatewayIPConfigurations;
        return this;
    }

    /**
     * Get the sslCertificates value.
     *
     * @return the sslCertificates value
     */
    public List<ApplicationGatewaySslCertificate> sslCertificates() {
        return this.sslCertificates;
    }

    /**
     * Set the sslCertificates value.
     *
     * @param sslCertificates the sslCertificates value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withSslCertificates(List<ApplicationGatewaySslCertificate> sslCertificates) {
        this.sslCertificates = sslCertificates;
        return this;
    }

    /**
     * Get the frontendIPConfigurations value.
     *
     * @return the frontendIPConfigurations value
     */
    public List<ApplicationGatewayFrontendIPConfiguration> frontendIPConfigurations() {
        return this.frontendIPConfigurations;
    }

    /**
     * Set the frontendIPConfigurations value.
     *
     * @param frontendIPConfigurations the frontendIPConfigurations value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withFrontendIPConfigurations(List<ApplicationGatewayFrontendIPConfiguration> frontendIPConfigurations) {
        this.frontendIPConfigurations = frontendIPConfigurations;
        return this;
    }

    /**
     * Get the frontendPorts value.
     *
     * @return the frontendPorts value
     */
    public List<ApplicationGatewayFrontendPort> frontendPorts() {
        return this.frontendPorts;
    }

    /**
     * Set the frontendPorts value.
     *
     * @param frontendPorts the frontendPorts value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withFrontendPorts(List<ApplicationGatewayFrontendPort> frontendPorts) {
        this.frontendPorts = frontendPorts;
        return this;
    }

    /**
     * Get the probes value.
     *
     * @return the probes value
     */
    public List<ApplicationGatewayProbe> probes() {
        return this.probes;
    }

    /**
     * Set the probes value.
     *
     * @param probes the probes value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withProbes(List<ApplicationGatewayProbe> probes) {
        this.probes = probes;
        return this;
    }

    /**
     * Get the backendAddressPools value.
     *
     * @return the backendAddressPools value
     */
    public List<ApplicationGatewayBackendAddressPool> backendAddressPools() {
        return this.backendAddressPools;
    }

    /**
     * Set the backendAddressPools value.
     *
     * @param backendAddressPools the backendAddressPools value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withBackendAddressPools(List<ApplicationGatewayBackendAddressPool> backendAddressPools) {
        this.backendAddressPools = backendAddressPools;
        return this;
    }

    /**
     * Get the backendHttpSettingsCollection value.
     *
     * @return the backendHttpSettingsCollection value
     */
    public List<ApplicationGatewayBackendHttpSettings> backendHttpSettingsCollection() {
        return this.backendHttpSettingsCollection;
    }

    /**
     * Set the backendHttpSettingsCollection value.
     *
     * @param backendHttpSettingsCollection the backendHttpSettingsCollection value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withBackendHttpSettingsCollection(List<ApplicationGatewayBackendHttpSettings> backendHttpSettingsCollection) {
        this.backendHttpSettingsCollection = backendHttpSettingsCollection;
        return this;
    }

    /**
     * Get the httpListeners value.
     *
     * @return the httpListeners value
     */
    public List<ApplicationGatewayHttpListener> httpListeners() {
        return this.httpListeners;
    }

    /**
     * Set the httpListeners value.
     *
     * @param httpListeners the httpListeners value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withHttpListeners(List<ApplicationGatewayHttpListener> httpListeners) {
        this.httpListeners = httpListeners;
        return this;
    }

    /**
     * Get the urlPathMaps value.
     *
     * @return the urlPathMaps value
     */
    public List<ApplicationGatewayUrlPathMap> urlPathMaps() {
        return this.urlPathMaps;
    }

    /**
     * Set the urlPathMaps value.
     *
     * @param urlPathMaps the urlPathMaps value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withUrlPathMaps(List<ApplicationGatewayUrlPathMap> urlPathMaps) {
        this.urlPathMaps = urlPathMaps;
        return this;
    }

    /**
     * Get the requestRoutingRules value.
     *
     * @return the requestRoutingRules value
     */
    public List<ApplicationGatewayRequestRoutingRule> requestRoutingRules() {
        return this.requestRoutingRules;
    }

    /**
     * Set the requestRoutingRules value.
     *
     * @param requestRoutingRules the requestRoutingRules value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withRequestRoutingRules(List<ApplicationGatewayRequestRoutingRule> requestRoutingRules) {
        this.requestRoutingRules = requestRoutingRules;
        return this;
    }

    /**
     * Get the resourceGuid value.
     *
     * @return the resourceGuid value
     */
    public String resourceGuid() {
        return this.resourceGuid;
    }

    /**
     * Set the resourceGuid value.
     *
     * @param resourceGuid the resourceGuid value to set
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withResourceGuid(String resourceGuid) {
        this.resourceGuid = resourceGuid;
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
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withProvisioningState(String provisioningState) {
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
     * @return the ApplicationGatewayInner object itself.
     */
    public ApplicationGatewayInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
