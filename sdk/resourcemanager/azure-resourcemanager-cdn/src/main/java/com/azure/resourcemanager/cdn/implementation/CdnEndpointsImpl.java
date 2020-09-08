// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.inner.CustomDomainInner;
import com.azure.resourcemanager.cdn.fluent.inner.EndpointInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.DeepCreatedOrigin;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a CDN manager profile.
 */
class CdnEndpointsImpl extends
    ExternalChildResourcesCachedImpl<CdnEndpointImpl,
        CdnEndpoint,
        EndpointInner,
        CdnProfileImpl,
        CdnProfile> {

    CdnEndpointsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "Endpoint");
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    /**
     * @return the azure endpoints as a map indexed by name.
     */
    Map<String, CdnEndpoint> endpointsAsMap() {
        Map<String, CdnEndpoint> result = new HashMap<>();
        for (Map.Entry<String, CdnEndpointImpl> entry : this.collection().entrySet()) {
            CdnEndpointImpl endpoint = entry.getValue();
            result.put(entry.getKey(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Mark the endpoint with given name as to be removed.
     *
     * @param name the name of the endpoint to be removed
     */
    public void remove(String name) {
        this.prepareInlineRemove(name);
    }

    /**
     * Adds the endpoint to the collection.
     *
     * @param endpoint the endpoint
     */
    public void addEndpoint(CdnEndpointImpl endpoint) {
        this.addChildResource(endpoint);
    }

    @Override
    protected List<CdnEndpointImpl> listChildResources() {
        List<CdnEndpointImpl> childResources = new ArrayList<>();
        for (EndpointInner innerEndpoint : this.getParent().manager().inner().getEndpoints().listByProfile(
                                        this.getParent().resourceGroupName(),
                                        this.getParent().name())) {
            CdnEndpointImpl endpointResource = new CdnEndpointImpl(
                innerEndpoint.name(), this.getParent(), innerEndpoint);
            for (CustomDomainInner customDomain : this.getParent().manager().inner().getCustomDomains()
                .listByEndpoint(
                    this.getParent().resourceGroupName(),
                    this.getParent().name(),
                    innerEndpoint.name())) {
                endpointResource.withCustomDomain(customDomain.hostname());
            }
            childResources.add(endpointResource);
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected Flux<CdnEndpointImpl> listChildResourcesAsync() {
        return Flux.fromIterable(listChildResources());
    }

    @Override
    protected CdnEndpointImpl newChildResource(String name) {
        CdnEndpointImpl endpoint = new CdnEndpointImpl(name, this.getParent(), new EndpointInner());
        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String originName, String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.defineNewEndpoint(endpointName);
        endpoint.inner().origins().add(
                new DeepCreatedOrigin()
                        .withName(originName)
                        .withHostname(endpointOriginHostname));
        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String endpointOriginHostname) {
        return this.defineNewEndpoint(endpointName, "origin", endpointOriginHostname);
    }

    public CdnEndpointImpl defineNewEndpoint(String name) {
        CdnEndpointImpl endpoint = this.prepareInlineDefine(name);
        endpoint.inner().withLocation(endpoint.parent().region().toString());
        endpoint.inner().withOrigins(new ArrayList<DeepCreatedOrigin>());
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
        CdnEndpointImpl endpoint = this.prepareInlineUpdate(name);
        return endpoint;
    }

    private String generateUniqueEndpointName(String endpointNamePrefix) {
        String endpointName;
        CheckNameAvailabilityResult result;

        do {
            endpointName = this.getParent().manager().sdkContext().randomResourceName(endpointNamePrefix, 50);
            result = this.getParent().checkEndpointNameAvailability(endpointName);
        } while (!result.nameAvailable());

        return endpointName;
    }
}
