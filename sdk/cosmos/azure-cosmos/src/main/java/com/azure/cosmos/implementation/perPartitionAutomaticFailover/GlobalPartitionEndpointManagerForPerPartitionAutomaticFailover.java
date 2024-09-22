// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover {
    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelFailoverInfo> partitionKeyRangeToLocation;
    private final GlobalEndpointManager globalEndpointManager;

    public GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(GlobalEndpointManager globalEndpointManager) {
        this.globalEndpointManager = globalEndpointManager;
        this.partitionKeyRangeToLocation = new ConcurrentHashMap<>();
    }

    public boolean tryAddPartitionLevelLocationOverride(RxDocumentServiceRequest request) {

        if (!Configs.isPerPartitionAutomaticFailoverEnabled()) {
            return false;
        }

        ConnectionPolicy connectionPolicy = this.globalEndpointManager.getConnectionPolicy();

        if (connectionPolicy.getConnectionMode() != ConnectionMode.DIRECT) {
            return false;
        }

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            return false;
        }

        if (StringUtils.isNotEmpty(resolvedCollectionRid)) {
            return false;
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
        PartitionLevelFailoverInfo partitionLevelFailoverInfo = this.partitionKeyRangeToLocation.get(partitionKeyRangeWrapper);

        if (partitionLevelFailoverInfo != null) {
            request.requestContext.routeToLocation(partitionLevelFailoverInfo.current);
            return true;
        }

        return false;
    }

    public boolean tryMarkEndpointAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request) {

        if (!Configs.isPerPartitionAutomaticFailoverEnabled()) {
            return false;
        }

        ConnectionPolicy connectionPolicy = this.globalEndpointManager.getConnectionPolicy();

        if (connectionPolicy.getConnectionMode() != ConnectionMode.DIRECT) {
            return false;
        }

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");

        if (request.isReadOnlyRequest()) {
            return false;
        }

        if (!isPerPartitionAutomaticFailoverApplicable(request)) {
            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRange;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            return false;
        }

        if (StringUtils.isNotEmpty(resolvedCollectionRid)) {
            return false;
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
        URI failedLocation = request.requestContext.locationEndpointToRoute;

        if (failedLocation == null) {
            return false;
        }

        PartitionLevelFailoverInfo partitionLevelFailoverInfo
            = this.partitionKeyRangeToLocation.putIfAbsent(partitionKeyRangeWrapper, new PartitionLevelFailoverInfo(failedLocation));

        // Rely on account-level read endpoints for new write region discovery
        List<URI> accountLevelReadEndpoints = this.globalEndpointManager.getAvailableReadEndpoints();

        if (partitionLevelFailoverInfo != null
            && partitionLevelFailoverInfo.tryMoveToNextLocation(accountLevelReadEndpoints, failedLocation)) {
            return true;
        }

        this.partitionKeyRangeToLocation.remove(partitionKeyRangeWrapper);
        return false;
    }

    private boolean isPerPartitionAutomaticFailoverApplicable(RxDocumentServiceRequest request) {
        if (this.globalEndpointManager.getApplicableReadEndpoints(Collections.emptyList()).size() <= 1) {
            return false;
        }

        checkNotNull(request, "Argument 'request' cannot be null!");

        ResourceType resourceType = request.getResourceType();
        OperationType operationType = request.getOperationType();

        checkNotNull(resourceType, "Argument 'resourceType' cannot be null!");
        checkNotNull(operationType, "Argument 'operationType' cannot be null!");

        if (resourceType == ResourceType.Document ||
            (resourceType == ResourceType.StoredProcedure && operationType == OperationType.ExecuteJavaScript)) {

            if (!this.globalEndpointManager.canUseMultipleWriteLocations(request)) {
                return true;
            }
        }

        return false;
    }

    static class PartitionLevelFailoverInfo {
        // failedLocations -> as the name suggests, write regions which would have responded with 403/3
        private final Set<URI> failedLocations = ConcurrentHashMap.newKeySet();
        // current -> available / failed over write region
        private URI current;

        PartitionLevelFailoverInfo(URI current) {
            this.current = current;
        }

        synchronized boolean tryMoveToNextLocation(List<URI> readLocations, URI failedLocation) {

            for (URI location : readLocations) {
                if (this.failedLocations.contains(location)) {
                    continue;
                }

                if (failedLocation.equals(this.current)) {
                    continue;
                }

                this.current = location;
                this.failedLocations.add(failedLocation);

                return true;
            }

            return false;
        }
    }
}
