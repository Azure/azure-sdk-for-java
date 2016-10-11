/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesImpl;
import com.microsoft.azure.management.trafficmanager.AzureEndpoint;
import com.microsoft.azure.management.trafficmanager.Endpoint;
import com.microsoft.azure.management.trafficmanager.EndpointType;
import com.microsoft.azure.management.trafficmanager.ExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.NestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.Profile;
import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a traffic manager profile.
 */
class EndpointsImpl extends
        ExternalChildResourcesImpl<EndpointImpl,
                Endpoint,
                EndpointInner,
                ProfileImpl,
                Profile> {
    private final EndpointsInner client;

    /**
     * Creates new EndpointsImpl.
     *
     * @param client the client to perform REST calls on endpoints
     * @param parent the parent traffic manager profile of the endpoints
     */
    EndpointsImpl(EndpointsInner client, ProfileImpl parent) {
        super(parent, "Endpoint");
        this.client = client;
        this.initializeCollection();
    }

    /**
     * @return the azure endpoints as a map indexed by name.
     */
    public Map<String, AzureEndpoint> azureEndpointsAsMap() {
        Map<String, AzureEndpoint> result = new HashMap<>();
        for (Map.Entry<String, EndpointImpl> entry : this.collection().entrySet()) {
            EndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.AZURE) {
                AzureEndpoint azureEndpoint = new AzureEndpointImpl(entry.getKey(),
                        this.parent(),
                        endpoint.inner(),
                        this.client);
                result.put(entry.getKey(), azureEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return the external endpoints as a map indexed by name.
     */
    public Map<String, ExternalEndpoint> externalEndpointsAsMap() {
        Map<String, ExternalEndpoint> result = new HashMap<>();
        for (Map.Entry<String, EndpointImpl> entry : this.collection().entrySet()) {
            EndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.EXTERNAL) {
                ExternalEndpoint externalEndpoint = new ExternalEndpointImpl(entry.getKey(),
                        this.parent(),
                        endpoint.inner(),
                        this.client);
                result.put(entry.getKey(), externalEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return the nested profile endpoints as a map indexed by name.
     */
    public Map<String, NestedProfileEndpoint> nestedProfileEndpointsAsMap() {
        Map<String, NestedProfileEndpoint> result = new HashMap<>();
        for (Map.Entry<String, EndpointImpl> entry : this.collection().entrySet()) {
            EndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.NESTEDPROFILE) {
                NestedProfileEndpoint nestedProfileEndpoint = new NestedProfileEndpointImpl(entry.getKey(),
                        this.parent(),
                        endpoint.inner(),
                        this.client);
                result.put(entry.getKey(), nestedProfileEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Starts an endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public EndpointImpl define(String name) {
        return this.prepareDefine(name);
    }

    /**
     * Starts an azure endpoint update chain.
     *
     * @param name the name of the endpoint to be updated
     * @return the endpoint
     */
    public EndpointImpl updateAzureEndpoint(String name) {
        EndpointImpl endpoint = this.prepareUpdate(name);
        if (endpoint.endpointType() != EndpointType.AZURE) {
            throw new IllegalArgumentException("An azure endpoint with name " + name + " not found in the profile");
        }
        return endpoint;
    }

    /**
     * Starts an external endpoint update chain.
     *
     * @param name the name of the endpoint to be updated
     * @return the endpoint
     */
    public EndpointImpl updateExternalEndpoint(String name) {
        EndpointImpl endpoint = this.prepareUpdate(name);
        if (endpoint.endpointType() != EndpointType.EXTERNAL) {
            throw new IllegalArgumentException("An external endpoint with name " + name + " not found in the profile");
        }
        return endpoint;
    }

    /**
     * Starts a nested profile endpoint update chain.
     *
     * @param name the name of the endpoint to be updated
     * @return the endpoint
     */
    public EndpointImpl updateNestedProfileEndpoint(String name) {
        EndpointImpl endpoint = this.prepareUpdate(name);
        if (endpoint.endpointType() != EndpointType.NESTEDPROFILE) {
            throw new IllegalArgumentException("A nested profile endpoint with name " + name + " not found in the profile");
        }
        return endpoint;
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
    public void addEndpoint(EndpointImpl endpoint) {
        this.addChildResource(endpoint);
    }

    @Override
    protected List<EndpointImpl> listChildResources() {
        List<EndpointImpl> childResources = new ArrayList<>();
        if (parent().inner().endpoints() != null) {
            for (EndpointInner inner : parent().inner().endpoints()) {
                if (inner.name() == null) {
                    childResources.add(new EndpointImpl(inner.name(),
                            this.parent(),
                            inner,
                            this.client));
                }
            }
        }
        return childResources;
    }

    @Override
    protected EndpointImpl newChildResource(String name) {
        return new EndpointImpl(name,
                this.parent(),
                new EndpointInner().withName(name),
                this.client);
    }
}
