/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.network.TopologyResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of Topology.
 */
@LangDefinition
class TopologyImpl extends WrapperImpl<TopologyInner> implements Topology {
    private Map<String, TopologyResource> resources;

    TopologyImpl(TopologyInner innerObject) {
        super(innerObject);
        initializeResourceseFromInner();
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public DateTime createdDateTime() {
        return inner().createdDateTime();
    }

    @Override
    public DateTime lastModified() {
        return inner().lastModified();
    }

    @Override
    public Map<String, TopologyResource> resources() {
        return Collections.unmodifiableMap(this.resources);
    }

    private void initializeResourceseFromInner() {
        this.resources = new TreeMap<>();
        List<TopologyResource> topologyResources = this.inner().resources();
        if (topologyResources != null) {
            for (TopologyResource resource : topologyResources) {
                this.resources.put(resource.name(), resource);
            }
        }
    }
}