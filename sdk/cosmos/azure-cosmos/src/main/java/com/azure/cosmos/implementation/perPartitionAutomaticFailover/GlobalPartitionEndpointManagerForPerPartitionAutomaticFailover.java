// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover {
    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
    private ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelFailoverInfo> partitionKeyRangeToLocation = new ConcurrentHashMap<>();

    public void tryAddPartitionLevelLocationOverride(RxDocumentServiceRequest request) {

        checkNotNull(request, "Argument 'request' cannot be null!");

    }

    public void tryMarkEndpointAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request) {}

    static class PartitionLevelFailoverInfo {
        private final Set<URI> failedLocations = ConcurrentHashMap.newKeySet();
    }
}
