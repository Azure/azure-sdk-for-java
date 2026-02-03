// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.server;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.server.config.ServerThroughputControlGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ContainerServerThroughputControlGroupProperties {

    private final String containerNameLink;
    private final AtomicReference<ServerThroughputControlGroup> defaultGroup;
    private final Map<String, ServerThroughputControlGroup> throughputControlGroups;

    public ContainerServerThroughputControlGroupProperties(String containerNameLink) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Argument 'containerNameLink' should not be empty");

        this.containerNameLink = containerNameLink;
        this.defaultGroup = new AtomicReference<>();
        this.throughputControlGroups = new ConcurrentHashMap<>();
    }

    /***
     * Enable a server throughput control group.
     *
     * @param group a {@link ServerThroughputControlGroup}.
     *
     * @return the total size of distinct server throughput control groups enabled on the container.
     */
    public int enableThroughputControlGroup(ServerThroughputControlGroup group) {
        checkNotNull(group, "Argument 'group' should not be null'");

        if (group.isDefault()) {
            if (!this.defaultGroup.compareAndSet(null, group)) {
                if (!this.defaultGroup.get().equals(group)) {
                    throw new IllegalArgumentException("A default group already exists");
                }
            }
        }

        ServerThroughputControlGroup serverThroughputControlGroup =
            this.throughputControlGroups.computeIfAbsent(group.getGroupName(), (key) -> group);
        if (!serverThroughputControlGroup.equals(group)) {
            throw new IllegalArgumentException("A group with same name already exists, name: " + group.getGroupName());
        }

        return this.throughputControlGroups.size();
    }

    public Map<String, ServerThroughputControlGroup> getThroughputControlGroups() {
        return this.throughputControlGroups;
    }

    public boolean hasDefaultGroup() {
        return this.defaultGroup.get() != null;
    }

    public boolean hasGroup(String groupName) {
        return this.throughputControlGroups.containsKey(groupName);
    }
}
