// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.Topology;
import com.azure.resourcemanager.network.models.TopologyParameters;
import com.azure.resourcemanager.network.models.TopologyResource;
import com.azure.resourcemanager.network.fluent.inner.TopologyInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** The implementation of Topology. */
class TopologyImpl extends ExecutableImpl<Topology> implements Topology, Topology.Definition {
    private Map<String, TopologyResource> resources;
    private final NetworkWatcherImpl parent;
    private TopologyParameters parameters = new TopologyParameters();
    private TopologyInner inner;

    TopologyImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public TopologyParameters topologyParameters() {
        return parameters;
    }

    @Override
    public OffsetDateTime createdTime() {
        return inner().createdDateTime();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return inner().lastModified();
    }

    @Override
    public Map<String, TopologyResource> resources() {
        return Collections.unmodifiableMap(this.resources);
    }

    private void initializeResourcesFromInner() {
        this.resources = new TreeMap<>();
        List<TopologyResource> topologyResources = this.inner().resources();
        if (topologyResources != null) {
            for (TopologyResource resource : topologyResources) {
                this.resources.put(resource.id(), resource);
            }
        }
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }

    @Override
    public TopologyImpl withTargetResourceGroup(String resourceGroupName) {
        parameters.withTargetResourceGroupName(resourceGroupName);
        return this;
    }

    @Override
    public TopologyImpl withTargetNetwork(String networkId) {
        parameters.withTargetVirtualNetwork(new SubResource().withId(networkId));
        return this;
    }

    @Override
    public TopologyImpl withTargetSubnet(String subnetName) {
        parameters
            .withTargetSubnet(
                new SubResource().withId(parameters.targetVirtualNetwork().id() + "/subnets/" + subnetName));
        return this;
    }

    @Override
    public TopologyInner inner() {
        return this.inner;
    }

    @Override
    public Mono<Topology> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
            .getTopologyAsync(parent().resourceGroupName(), parent().name(), parameters)
            .map(
                topologyInner -> {
                    TopologyImpl.this.inner = topologyInner;
                    TopologyImpl.this.initializeResourcesFromInner();
                    return TopologyImpl.this;
                });
    }
}
