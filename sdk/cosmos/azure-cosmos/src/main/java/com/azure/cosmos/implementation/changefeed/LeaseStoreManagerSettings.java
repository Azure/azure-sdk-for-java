// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Captures LeaseStoreManager properties.
 */
public class LeaseStoreManagerSettings {
    String containerNamePrefix;

    CosmosAsyncContainer leaseContainer;
    CosmosAsyncContainer monitoredContainer;
    String monitoredContainerRid;

    String hostName;

    public String getContainerNamePrefix() {
        return this.containerNamePrefix;
    }

    public LeaseStoreManagerSettings withContainerNamePrefix(String containerNamePrefix) {
        this.containerNamePrefix = containerNamePrefix;
        return this;
    }

    public CosmosAsyncContainer getLeaseContainer() {
        return this.leaseContainer;
    }

    public LeaseStoreManagerSettings withLeaseContainer(CosmosAsyncContainer leaseContainer) {
        this.leaseContainer = leaseContainer;
        return this;
    }

    public String getHostName() {
        return this.hostName;
    }

    public LeaseStoreManagerSettings withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public LeaseStoreManagerSettings withMonitoredContainer(CosmosAsyncContainer monitoredContainer) {
        this.monitoredContainer = monitoredContainer;
        return this;
    }

    public CosmosAsyncContainer getMonitoredContainer() {
        return this.monitoredContainer;
    }

    public LeaseStoreManagerSettings withMonitoredContainerRid(String monitoredContainerRid) {
        this.monitoredContainerRid = monitoredContainerRid;
        return this;
    }

    public String getMonitoredContainerRid() {
        return this.monitoredContainerRid;
    }

    public void validate() {
        checkNotNull(this.containerNamePrefix, "containerNamePrefix can not be null");
        checkNotNull(this.leaseContainer, "leaseContainer can not be null");
        checkNotNull(this.hostName, "hostName can not be null");
        checkNotNull(this.monitoredContainer, "monitored container can not be null");
        checkArgument(!StringUtils.isEmpty(this.monitoredContainerRid), "monitoredContainerRid can not be null nor empty");
    }
}
