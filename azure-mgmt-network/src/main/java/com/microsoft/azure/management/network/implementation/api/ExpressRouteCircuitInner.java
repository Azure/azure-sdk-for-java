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
 * ExpressRouteCircuit resource.
 */
@JsonFlatten
public class ExpressRouteCircuitInner extends Resource {
    /**
     * Gets or sets sku.
     */
    private ExpressRouteCircuitSku sku;

    /**
     * Gets or sets CircuitProvisioningState state of the resource.
     */
    @JsonProperty(value = "properties.circuitProvisioningState")
    private String circuitProvisioningState;

    /**
     * Gets or sets ServiceProviderProvisioningState state of the resource .
     * Possible values include: 'NotProvisioned', 'Provisioning',
     * 'Provisioned', 'Deprovisioning'.
     */
    @JsonProperty(value = "properties.serviceProviderProvisioningState")
    private String serviceProviderProvisioningState;

    /**
     * Gets or sets list of authorizations.
     */
    @JsonProperty(value = "properties.authorizations")
    private List<ExpressRouteCircuitAuthorizationInner> authorizations;

    /**
     * Gets or sets list of peerings.
     */
    @JsonProperty(value = "properties.peerings")
    private List<ExpressRouteCircuitPeeringInner> peerings;

    /**
     * Gets or sets ServiceKey.
     */
    @JsonProperty(value = "properties.serviceKey")
    private String serviceKey;

    /**
     * Gets or sets ServiceProviderNotes.
     */
    @JsonProperty(value = "properties.serviceProviderNotes")
    private String serviceProviderNotes;

    /**
     * Gets or sets ServiceProviderProperties.
     */
    @JsonProperty(value = "properties.serviceProviderProperties")
    private ExpressRouteCircuitServiceProviderProperties serviceProviderProperties;

    /**
     * Gets or sets Provisioning state of the PublicIP resource
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
    public ExpressRouteCircuitSku sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withSku(ExpressRouteCircuitSku sku) {
        this.sku = sku;
        return this;
    }

    /**
     * Get the circuitProvisioningState value.
     *
     * @return the circuitProvisioningState value
     */
    public String circuitProvisioningState() {
        return this.circuitProvisioningState;
    }

    /**
     * Set the circuitProvisioningState value.
     *
     * @param circuitProvisioningState the circuitProvisioningState value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withCircuitProvisioningState(String circuitProvisioningState) {
        this.circuitProvisioningState = circuitProvisioningState;
        return this;
    }

    /**
     * Get the serviceProviderProvisioningState value.
     *
     * @return the serviceProviderProvisioningState value
     */
    public String serviceProviderProvisioningState() {
        return this.serviceProviderProvisioningState;
    }

    /**
     * Set the serviceProviderProvisioningState value.
     *
     * @param serviceProviderProvisioningState the serviceProviderProvisioningState value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withServiceProviderProvisioningState(String serviceProviderProvisioningState) {
        this.serviceProviderProvisioningState = serviceProviderProvisioningState;
        return this;
    }

    /**
     * Get the authorizations value.
     *
     * @return the authorizations value
     */
    public List<ExpressRouteCircuitAuthorizationInner> authorizations() {
        return this.authorizations;
    }

    /**
     * Set the authorizations value.
     *
     * @param authorizations the authorizations value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withAuthorizations(List<ExpressRouteCircuitAuthorizationInner> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    /**
     * Get the peerings value.
     *
     * @return the peerings value
     */
    public List<ExpressRouteCircuitPeeringInner> peerings() {
        return this.peerings;
    }

    /**
     * Set the peerings value.
     *
     * @param peerings the peerings value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withPeerings(List<ExpressRouteCircuitPeeringInner> peerings) {
        this.peerings = peerings;
        return this;
    }

    /**
     * Get the serviceKey value.
     *
     * @return the serviceKey value
     */
    public String serviceKey() {
        return this.serviceKey;
    }

    /**
     * Set the serviceKey value.
     *
     * @param serviceKey the serviceKey value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
        return this;
    }

    /**
     * Get the serviceProviderNotes value.
     *
     * @return the serviceProviderNotes value
     */
    public String serviceProviderNotes() {
        return this.serviceProviderNotes;
    }

    /**
     * Set the serviceProviderNotes value.
     *
     * @param serviceProviderNotes the serviceProviderNotes value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withServiceProviderNotes(String serviceProviderNotes) {
        this.serviceProviderNotes = serviceProviderNotes;
        return this;
    }

    /**
     * Get the serviceProviderProperties value.
     *
     * @return the serviceProviderProperties value
     */
    public ExpressRouteCircuitServiceProviderProperties serviceProviderProperties() {
        return this.serviceProviderProperties;
    }

    /**
     * Set the serviceProviderProperties value.
     *
     * @param serviceProviderProperties the serviceProviderProperties value to set
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withServiceProviderProperties(ExpressRouteCircuitServiceProviderProperties serviceProviderProperties) {
        this.serviceProviderProperties = serviceProviderProperties;
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
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withProvisioningState(String provisioningState) {
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
     * @return the ExpressRouteCircuitInner object itself.
     */
    public ExpressRouteCircuitInner withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
