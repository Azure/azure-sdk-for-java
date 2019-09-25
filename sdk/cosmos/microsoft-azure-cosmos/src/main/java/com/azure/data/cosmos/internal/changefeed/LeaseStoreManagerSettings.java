// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosAsyncContainer;

/**
 * Captures LeaseStoreManager properties.
 */
public class LeaseStoreManagerSettings {
    String containerNamePrefix;

    CosmosAsyncContainer leaseCollectionLink;

    String hostName;

    public String getContainerNamePrefix() {
        return this.containerNamePrefix;
    }

    public LeaseStoreManagerSettings withContainerNamePrefix(String containerNamePrefix) {
        this.containerNamePrefix = containerNamePrefix;
        return this;
    }

    public CosmosAsyncContainer getLeaseCollectionLink() {
        return this.leaseCollectionLink;
    }

    public LeaseStoreManagerSettings withLeaseCollectionLink(CosmosAsyncContainer collectionLink) {
        this.leaseCollectionLink = collectionLink;
        return this;
    }

    public String getHostName() {
        return this.hostName;
    }

    public LeaseStoreManagerSettings withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }
}
