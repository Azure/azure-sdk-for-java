/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.Topology;
import com.azure.management.network.TopologyParameters;
import com.azure.management.network.TopologyResource;
import com.azure.management.network.models.TopologyInner;
import com.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of Topology.
 */
class TopologyImpl extends ExecutableImpl<Topology>
        implements Topology,
        Topology.Definition {
    private Map<String, TopologyResource> resources;
    private final NetworkWatcherImpl parent;
    private TopologyParameters parameters = new TopologyParameters();
    private TopologyInner inner;

    TopologyImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public String id() {
        return inner().getId();
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
                this.resources.put(resource.getId(), resource);
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
        parameters.withTargetVirtualNetwork(new SubResource().setId(networkId));
        return this;
    }

    @Override
    public TopologyImpl withTargetSubnet(String subnetName) {
        parameters.withTargetSubnet(new SubResource().setId(parameters.targetVirtualNetwork().getId() + "/subnets/" + subnetName));
        return this;
    }

    @Override
    public TopologyInner inner() {
        return this.inner;
    }

    @Override
    public Mono<Topology> executeWorkAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getTopologyAsync(parent().resourceGroupName(), parent().name(), parameters)
                .map(topologyInner -> {
                    TopologyImpl.this.inner = topologyInner;
                    TopologyImpl.this.initializeResourcesFromInner();
                    return TopologyImpl.this;
                });
    }
}