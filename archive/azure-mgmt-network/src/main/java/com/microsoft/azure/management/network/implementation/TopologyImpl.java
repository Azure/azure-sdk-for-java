/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.network.TopologyResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of Topology.
 */
@LangDefinition
class TopologyImpl extends RefreshableWrapperImpl<TopologyInner, Topology>
        implements Topology {
    private Map<String, TopologyResource> resources;
    private final NetworkWatcherImpl parent;
    private final String groupName;

    TopologyImpl(NetworkWatcherImpl parent, TopologyInner innerObject, String groupName) {
        super(innerObject);
        this.parent = parent;
        this.groupName = groupName;
        initializeResourcesFromInner();
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public String resourceGroupName() {
        return groupName;
    }

    @Override
    public DateTime createdTime() {
        return inner().createdDateTime();
    }

    @Override
    public DateTime lastModifiedTime() {
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
    public Observable<Topology> refreshAsync() {
        return super.refreshAsync().map(new Func1<Topology, Topology>() {
            @Override
            public Topology call(Topology topology) {
                TopologyImpl impl = (TopologyImpl) topology;
                impl.initializeResourcesFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<TopologyInner> getInnerAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getTopologyAsync(parent().resourceGroupName(), parent().name(), groupName);
    }
}