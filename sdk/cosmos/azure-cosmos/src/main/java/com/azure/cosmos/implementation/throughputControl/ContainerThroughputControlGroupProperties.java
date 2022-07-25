// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ContainerThroughputControlGroupProperties {
    private static Logger logger  = LoggerFactory.getLogger(ContainerThroughputControlGroupProperties.class);

    private final AtomicReference<ThroughputControlGroupInternal> defaultGroup;
    private final Set<ThroughputControlGroupInternal> throughputControlGroupSet;
    private final Set<String> supressInitErrorGroupSet;

    public ContainerThroughputControlGroupProperties() {
        this.defaultGroup = new AtomicReference<>();
        this.throughputControlGroupSet = ConcurrentHashMap.newKeySet();
        this.supressInitErrorGroupSet = ConcurrentHashMap.newKeySet();
    }

    /***
     * Enable a throughput control group.
     *
     * @param group a {@link  ThroughputControlGroupInternal}.
     *
     * @return the total size of distinct throughput control groups enabled on the container.
     */
    public Pair<Integer, Boolean> enableThroughputControlGroup(ThroughputControlGroupInternal group) {
        checkNotNull(group, "Throughput control group should not be null");

        if (group.isDefault()) {
            if (!this.defaultGroup.compareAndSet(null, group)) {
                if (!this.defaultGroup.get().hasSameIdentity(group)) {
                    throw new IllegalArgumentException("A default group already exists");
                }
            }
        }

        if (group.isContinueOnInitError()) {
            this.supressInitErrorGroupSet.add(group.getGroupName());
        }

        final AtomicBoolean updatedGroupConfig = new AtomicBoolean(false);

        // Only throw when two different groups are using the same id (databaseId + containerId + groupName)
        this
            .throughputControlGroupSet
            .stream()
            .forEach(existingGroup -> {
                if (Objects.equals(existingGroup.getIdPrefix(), group.getIdPrefix()) &&
                    !existingGroup.equals(group)) {

                    if (existingGroup.isDefault() != group.isDefault() ||
                        existingGroup.isContinueOnInitError() != group.isContinueOnInitError()) {
                        throw new IllegalArgumentException(
                            "Throughput control group with id " + group.getId() + " already exists with different " +
                                "setting for isDefault or isContinueOnInitError - these properties are immutable and " +
                                "cannot be changed.");
                    }

                    this.throughputControlGroupSet.remove(existingGroup);
                    logger.info(
                        "Throughput control group with id-prefix {} already exists with different config. " +
                            "Will update config.",
                        group.getIdPrefix());
                    updatedGroupConfig.set(true);
                }
            });

        this.throughputControlGroupSet.add(group);

        return Pair.of(this.throughputControlGroupSet.size(), updatedGroupConfig.get());
    }

    public Set<ThroughputControlGroupInternal> getThroughputControlGroupSet() {
        return this.throughputControlGroupSet;
    }

    public boolean allowRequestToContinueOnInitError(RxDocumentServiceRequest request) {
        checkNotNull(request, "Request should not be null");

        String requestGroupName = request.getThroughputControlGroupName();
        if (StringUtils.isEmpty(requestGroupName)) {
            requestGroupName = this.defaultGroup.get().getGroupName();
        }

        return this.supressInitErrorGroupSet.contains(requestGroupName);
    }
}