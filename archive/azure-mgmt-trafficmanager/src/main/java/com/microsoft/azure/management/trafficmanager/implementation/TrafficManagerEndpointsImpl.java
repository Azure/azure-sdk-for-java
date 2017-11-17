/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import com.microsoft.azure.management.trafficmanager.TrafficManagerAzureEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerEndpoint;
import com.microsoft.azure.management.trafficmanager.EndpointType;
import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a traffic manager profile.
 */
@LangDefinition
class TrafficManagerEndpointsImpl extends
        ExternalChildResourcesCachedImpl<TrafficManagerEndpointImpl,
                TrafficManagerEndpoint,
                EndpointInner,
                TrafficManagerProfileImpl,
                TrafficManagerProfile> {
    private final EndpointsInner client;

    /**
     * Creates new EndpointsImpl.
     *
     * @param client the client to perform REST calls on endpoints
     * @param parent the parent traffic manager profile of the endpoints
     */
    TrafficManagerEndpointsImpl(EndpointsInner client, TrafficManagerProfileImpl parent) {
        super(parent, "Endpoint");
        this.client = client;
        this.cacheCollection();
    }

    /**
     * @return the azure endpoints as a map indexed by name.
     */
    Map<String, TrafficManagerAzureEndpoint> azureEndpointsAsMap() {
        Map<String, TrafficManagerAzureEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.AZURE) {
                TrafficManagerAzureEndpoint azureEndpoint = new TrafficManagerAzureEndpointImpl(entry.getKey(),
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
    Map<String, TrafficManagerExternalEndpoint> externalEndpointsAsMap() {
        Map<String, TrafficManagerExternalEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.EXTERNAL) {
                TrafficManagerExternalEndpoint externalEndpoint = new TrafficManagerExternalEndpointImpl(entry.getKey(),
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
    Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpointsAsMap() {
        Map<String, TrafficManagerNestedProfileEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.NESTED_PROFILE) {
                TrafficManagerNestedProfileEndpoint nestedProfileEndpoint = new TrafficManagerNestedProfileEndpointImpl(entry.getKey(),
                        this.parent(),
                        endpoint.inner(),
                        this.client);
                result.put(entry.getKey(), nestedProfileEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Starts an Azure endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl defineAzureTargetEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareDefine(name);
        endpoint.inner().withType(EndpointType.AZURE.toString());
        return endpoint;
    }

    /**
     * Starts an external endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl defineExteralTargetEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareDefine(name);
        endpoint.inner().withType(EndpointType.EXTERNAL.toString());
        return endpoint;
    }

    /**
     * Starts an nested profile endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl defineNestedProfileTargetEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareDefine(name);
        endpoint.inner().withType(EndpointType.NESTED_PROFILE.toString());
        return endpoint;
    }

    /**
     * Starts an azure endpoint update chain.
     *
     * @param name the name of the endpoint to be updated
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl updateAzureEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareUpdate(name);
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
    public TrafficManagerEndpointImpl updateExternalEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareUpdate(name);
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
    public TrafficManagerEndpointImpl updateNestedProfileEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareUpdate(name);
        if (endpoint.endpointType() != EndpointType.NESTED_PROFILE) {
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
    public void addEndpoint(TrafficManagerEndpointImpl endpoint) {
        this.addChildResource(endpoint);
    }

    @Override
    protected List<TrafficManagerEndpointImpl> listChildResources() {
        List<TrafficManagerEndpointImpl> childResources = new ArrayList<>();
        if (parent().inner().endpoints() != null) {
            for (EndpointInner inner : parent().inner().endpoints()) {
                childResources.add(new TrafficManagerEndpointImpl(inner.name(),
                    this.parent(),
                    inner,
                    this.client));
            }
        }
        return childResources;
    }

    @Override
    protected TrafficManagerEndpointImpl newChildResource(String name) {
        TrafficManagerEndpointImpl endpoint = new TrafficManagerEndpointImpl(name,
                this.parent(),
                new EndpointInner(),
                this.client);
        return endpoint
                .withRoutingWeight(1)
                .withTrafficEnabled();
    }
}
