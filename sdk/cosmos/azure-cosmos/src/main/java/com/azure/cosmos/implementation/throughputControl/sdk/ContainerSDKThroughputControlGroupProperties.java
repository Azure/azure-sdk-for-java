// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.throughputControl.sdk.config.SDKThroughputControlGroupInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ContainerSDKThroughputControlGroupProperties {
    private static Logger logger  = LoggerFactory.getLogger(ContainerSDKThroughputControlGroupProperties.class);

    private final AtomicReference<SDKThroughputControlGroupInternal> defaultGroup;
    private final Map<String, SDKThroughputControlGroupInternal> throughputControlGroups;
    private final Set<String> supressInitErrorGroupSet;
    private final AtomicReference<Mono<Integer>> throughputQueryMonoReference;
    private final String containerNameLink;

    public ContainerSDKThroughputControlGroupProperties(String containerNameLink) {
        checkArgument(StringUtils.isNotEmpty(containerNameLink), "Argument 'containerNameLink' should not be empty");

        this.defaultGroup = new AtomicReference<>();
        this.containerNameLink = containerNameLink;
        this.throughputControlGroups = new ConcurrentHashMap<>();
        this.supressInitErrorGroupSet = ConcurrentHashMap.newKeySet();
        this.throughputQueryMonoReference = new AtomicReference<>();
    }

    /***
     * Enable a throughput control group.
     *
     * @param group a {@link  SDKThroughputControlGroupInternal}.
     *
     * @return the total size of distinct throughput control groups enabled on the container.
     */
    public Pair<Integer, Boolean> enableThroughputControlGroup(SDKThroughputControlGroupInternal group, Mono<Integer> throughputQueryMono) {
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
            .throughputControlGroups
            .values()
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

                    this.throughputControlGroups.remove(existingGroup);
                    logger.info(
                        "Throughput control group with id-prefix {} already exists with different config. " +
                            "Will update config.",
                        group.getIdPrefix());
                    updatedGroupConfig.set(true);
                }
            });

        this.throughputControlGroups.put(group.getGroupName(), group);
        if (!this.throughputQueryMonoReference.compareAndSet(null, throughputQueryMono)) {
            logger.debug("ThroughputQueryMono has exists for container {}", containerNameLink);
        }

        return Pair.of(this.throughputControlGroups.size(), updatedGroupConfig.get());
    }

    public Map<String, SDKThroughputControlGroupInternal> getThroughputControlGroups() {
        return this.throughputControlGroups;
    }

    public Mono<Integer> getThroughputQueryMono() {
        return this.throughputQueryMonoReference.get();
    }

    public boolean allowRequestToContinueOnInitError(RxDocumentServiceRequest request) {
        checkNotNull(request, "Request should not be null");

        String requestGroupName = request.getThroughputControlGroupName();
        if (StringUtils.isEmpty(requestGroupName)) {
            if (defaultGroup.get() == null) {
                return true;
            }
            requestGroupName = defaultGroup.get().getGroupName();
        }

        return this.supressInitErrorGroupSet.contains(requestGroupName);
    }

    public boolean hasDefaultGroup() {
        return this.defaultGroup.get() != null;
    }

    public boolean hasGroup(String groupName) {
        return this.throughputControlGroups.containsKey(groupName);
    }
}
