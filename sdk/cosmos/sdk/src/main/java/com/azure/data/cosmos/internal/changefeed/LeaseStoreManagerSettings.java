// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosContainer;

/**
 * Captures LeaseStoreManager properties.
 */
public class LeaseStoreManagerSettings {
    String containerNamePrefix;

    CosmosContainer leaseCollectionLink;

    String hostName;

    public String getContainerNamePrefix() {
        return this.containerNamePrefix;
    }

    public LeaseStoreManagerSettings withContainerNamePrefix(String containerNamePrefix) {
        this.containerNamePrefix = containerNamePrefix;
        return this;
    }

    public CosmosContainer getLeaseCollectionLink() {
        return this.leaseCollectionLink;
    }

    public LeaseStoreManagerSettings withLeaseCollectionLink(CosmosContainer collectionLink) {
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
