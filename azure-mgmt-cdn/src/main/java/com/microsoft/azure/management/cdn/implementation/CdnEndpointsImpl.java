/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.DeepCreatedOrigin;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a traffic manager profile.
 */
class CdnEndpointsImpl extends
        ExternalChildResourcesImpl<CdnEndpointImpl,
                CdnEndpoint,
                EndpointInner,
                CdnProfileImpl,
                CdnProfile> {
    private final EndpointsInner client;

    /**
     * Creates new EndpointsImpl.
     *
     * @param client the client to perform REST calls on endpoints
     * @param parent the parent traffic manager profile of the endpoints
     */
    CdnEndpointsImpl(EndpointsInner client, CdnProfileImpl parent) {
        super(parent, "Endpoint");
        this.client = client;
        this.initializeCollection();
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
        this.prepareRemove(name);
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
        /*if (parent().inner().endpoints() != null) {
            for (EndpointInner inner : parent().inner().endpoints()) {
                childResources.add(new CdnEndpointImpl(inner.name(),
                    this.parent(),
                    inner,
                    this.client));
            }
        }*/
        return childResources;
    }

    @Override
    protected CdnEndpointImpl newChildResource(String name) {
        CdnEndpointImpl endpoint = new CdnEndpointImpl(name,
                this.parent(),
                new EndpointInner(),
                this.client);

        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.prepareDefine("someEndpointNameGenerator");
        endpoint.inner().withOrigins( new ArrayList<DeepCreatedOrigin>());
        endpoint.inner().origins().add(
                new DeepCreatedOrigin()
                    .withName("someOriginNameGenerator")
                    .withHostName(endpointOriginHostname));
        return endpoint;
    }
}
