// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import com.azure.resourcemanager.trafficmanager.models.EndpointType;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerAzureEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerNestedProfileEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;

/** Represents an endpoint collection associated with a traffic manager profile. */
class TrafficManagerEndpointsImpl
    extends ExternalChildResourcesCachedImpl<
        TrafficManagerEndpointImpl,
        TrafficManagerEndpoint,
        EndpointInner,
        TrafficManagerProfileImpl,
        TrafficManagerProfile> {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    private final EndpointsClient client;

    /**
     * Creates new EndpointsImpl.
     *
     * @param client the client to perform REST calls on endpoints
     * @param parent the parent traffic manager profile of the endpoints
     */
    TrafficManagerEndpointsImpl(EndpointsClient client, TrafficManagerProfileImpl parent) {
        super(parent, parent.taskGroup(), "Endpoint");
        this.client = client;
        this.cacheCollection();
    }

    List<EndpointInner> allEndpointsInners() {
        List<EndpointInner> allEndpoints = new ArrayList<>();
        for (TrafficManagerEndpointImpl epImpl : this.collection().values()) {
            allEndpoints.add(epImpl.innerModel());
        }
        return allEndpoints;
    }

    /** @return the azure endpoints as a map indexed by name. */
    Map<String, TrafficManagerAzureEndpoint> azureEndpointsAsMap() {
        Map<String, TrafficManagerAzureEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.AZURE) {
                TrafficManagerAzureEndpoint azureEndpoint =
                    new TrafficManagerAzureEndpointImpl(
                        entry.getKey(), this.getParent(), endpoint.innerModel(), this.client);
                result.put(entry.getKey(), azureEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** @return the external endpoints as a map indexed by name. */
    Map<String, TrafficManagerExternalEndpoint> externalEndpointsAsMap() {
        Map<String, TrafficManagerExternalEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.EXTERNAL) {
                TrafficManagerExternalEndpoint externalEndpoint =
                    new TrafficManagerExternalEndpointImpl(
                        entry.getKey(), this.getParent(), endpoint.innerModel(), this.client);
                result.put(entry.getKey(), externalEndpoint);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** @return the nested profile endpoints as a map indexed by name. */
    Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpointsAsMap() {
        Map<String, TrafficManagerNestedProfileEndpoint> result = new HashMap<>();
        for (Map.Entry<String, TrafficManagerEndpointImpl> entry : this.collection().entrySet()) {
            TrafficManagerEndpointImpl endpoint = entry.getValue();
            if (endpoint.endpointType() == EndpointType.NESTED_PROFILE) {
                TrafficManagerNestedProfileEndpoint nestedProfileEndpoint =
                    new TrafficManagerNestedProfileEndpointImpl(
                        entry.getKey(), this.getParent(), endpoint.innerModel(), this.client);
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
        TrafficManagerEndpointImpl endpoint = this.prepareInlineDefine(name);
        endpoint.withEndpointType(EndpointType.AZURE);
        return endpoint;
    }

    /**
     * Starts an external endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl defineExteralTargetEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareInlineDefine(name);
        endpoint.withEndpointType(EndpointType.EXTERNAL);
        return endpoint;
    }

    /**
     * Starts an nested profile endpoint definition chain.
     *
     * @param name the name of the endpoint to be added
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl defineNestedProfileTargetEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareInlineDefine(name);
        endpoint.withEndpointType(EndpointType.NESTED_PROFILE);
        return endpoint;
    }

    /**
     * Starts an azure endpoint update chain.
     *
     * @param name the name of the endpoint to be updated
     * @return the endpoint
     */
    public TrafficManagerEndpointImpl updateAzureEndpoint(String name) {
        TrafficManagerEndpointImpl endpoint = this.prepareInlineUpdate(name);
        if (endpoint.endpointType() != EndpointType.AZURE) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("An azure endpoint with name " + name + " not found in the profile"));
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
        TrafficManagerEndpointImpl endpoint = this.prepareInlineUpdate(name);
        if (endpoint.endpointType() != EndpointType.EXTERNAL) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("An external endpoint with name " + name + " not found in the profile"));
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
        TrafficManagerEndpointImpl endpoint = this.prepareInlineUpdate(name);
        if (endpoint.endpointType() != EndpointType.NESTED_PROFILE) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "A nested profile endpoint with name " + name + " not found in the profile"));
        }
        return endpoint;
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
    public void addEndpoint(TrafficManagerEndpointImpl endpoint) {
        this.addChildResource(endpoint);
    }

    @Override
    protected List<TrafficManagerEndpointImpl> listChildResources() {
        List<TrafficManagerEndpointImpl> childResources = new ArrayList<>();
        if (getParent().innerModel().endpoints() != null) {
            for (EndpointInner inner : getParent().innerModel().endpoints()) {
                childResources.add(new TrafficManagerEndpointImpl(inner.name(), this.getParent(), inner, this.client));
            }
        }
        return childResources;
    }

    @Override
    protected Flux<TrafficManagerEndpointImpl> listChildResourcesAsync() {
        return Flux.fromIterable(listChildResources());
    }

    @Override
    protected TrafficManagerEndpointImpl newChildResource(String name) {
        TrafficManagerEndpointImpl endpoint =
            new TrafficManagerEndpointImpl(name, this.getParent(), new EndpointInner(), this.client);
        return endpoint.withRoutingWeight(1).withTrafficEnabled();
    }
}
