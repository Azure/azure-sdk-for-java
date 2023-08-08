// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.CustomDomainInner;
import com.azure.resourcemanager.cdn.fluent.models.EndpointInner;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.DeepCreatedOrigin;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a CDN manager profile.
 */
class CdnEndpointsImpl extends
    ExternalChildResourcesNonCachedImpl<CdnEndpointImpl,
        CdnEndpoint,
        EndpointInner,
        CdnProfileImpl,
        CdnProfile> {

    CdnEndpointsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "Endpoint");
    }

    /**
     * @return the azure endpoints as a map indexed by name.
     */
    Map<String, CdnEndpoint> endpointsAsMap() {
        Map<String, CdnEndpoint> result = new HashMap<>();
        for (EndpointInner endpointInner : this.getParent().manager().serviceClient().getEndpoints()
            .listByProfile(this.getParent().resourceGroupName(), this.getParent().name())) {
            CdnEndpointImpl endpoint = new CdnEndpointImpl(endpointInner.name(), this.getParent(), endpointInner);
            for (CustomDomainInner customDomainInner : this.getParent().manager().serviceClient().getCustomDomains()
                .listByEndpoint(this.getParent().resourceGroupName(), this.getParent().name(), endpoint.name())) {
                endpoint.withCustomDomain(customDomainInner.hostname());
            }
            result.put(endpoint.name(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Mark the endpoint with given name as to be removed.
     *
     * @param name the name of the endpoint to be removed
     */
    public void remove(String name) {
        this.prepareInlineRemove(new CdnEndpointImpl(name, getParent(), new EndpointInner()));
    }

    /**
     * Adds the endpoint to the collection.
     *
     * @param endpoint the endpoint
     */
    public void addEndpoint(CdnEndpointImpl endpoint) {
        this.childCollection.put(endpoint.name(), endpoint);
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String originName, String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.defineNewEndpoint(endpointName);
        endpoint.innerModel().origins().add(
                new DeepCreatedOrigin()
                        .withName(originName)
                        .withHostname(endpointOriginHostname));
        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String endpointOriginHostname) {
        return this.defineNewEndpoint(endpointName, "origin", endpointOriginHostname);
    }

    public CdnEndpointImpl defineNewEndpoint(String name) {
        CdnEndpointImpl endpoint = this.prepareInlineDefine(
            new CdnEndpointImpl(name, this.getParent(), new EndpointInner()));
        endpoint.innerModel().withLocation(endpoint.parent().region().toString());
        endpoint.innerModel().withOrigins(new ArrayList<>());
        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint() {
        String endpointName = this.generateUniqueEndpointName("Endpoint");
        return this.defineNewEndpoint(endpointName);
    }

    public CdnEndpointImpl defineNewEndpointWithOriginHostname(String endpointOriginHostname) {
        String endpointName = this.generateUniqueEndpointName("Endpoint");
        CdnEndpointImpl endpoint = this.defineNewEndpoint(endpointName, "origin", endpointOriginHostname);
        return endpoint;
    }

    public CdnEndpointImpl updateEndpoint(String name) {
        EndpointInner endpointInner = this.getParent().manager().serviceClient().getEndpoints()
            .get(this.getParent().resourceGroupName(), this.getParent().name(), name);
        CdnEndpointImpl endpoint = this.prepareInlineUpdate(
            new CdnEndpointImpl(name, this.getParent(), endpointInner));
        return endpoint;
    }

    private String generateUniqueEndpointName(String endpointNamePrefix) {
        String endpointName;
        CheckNameAvailabilityResult result;

        do {
            endpointName = this.getParent().manager().resourceManager().internalContext()
                .randomResourceName(endpointNamePrefix, 50);
            result = this.getParent().checkEndpointNameAvailability(endpointName);
        } while (!result.nameAvailable());

        return endpointName;
    }
}
