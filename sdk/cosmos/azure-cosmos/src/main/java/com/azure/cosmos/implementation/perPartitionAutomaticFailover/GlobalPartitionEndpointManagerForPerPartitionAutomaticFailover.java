// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover {
    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelFailoverInfo> partitionKeyRangeToLocation;
    private final GlobalEndpointManager globalEndpointManager;
    private final boolean isPerPartitionAutomaticFailoverEnabled;

    public GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(
        GlobalEndpointManager globalEndpointManager,
        boolean isPerPartitionAutomaticFailoverEnabled) {

        this.globalEndpointManager = globalEndpointManager;
        this.partitionKeyRangeToLocation = new ConcurrentHashMap<>();
        this.isPerPartitionAutomaticFailoverEnabled = isPerPartitionAutomaticFailoverEnabled;
    }

    public boolean tryAddPartitionLevelLocationOverride(RxDocumentServiceRequest request) {

        if (!this.isPerPartitionAutomaticFailoverEnabled) {
            return false;
        }

        checkNotNull(request, "Argument 'request' cannot be null!");
        checkNotNull(request.requestContext, "Argument 'request.requestContext' cannot be null!");


        if (request.getResourceType() != ResourceType.Document) {
            return false;
        }

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            return false;
        }

        if (StringUtils.isEmpty(resolvedCollectionRid)) {
            return false;
        }

        if (request.isReadOnlyRequest()) {
            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest
                = request.requestContext.getCrossRegionAvailabilityContext();

            checkNotNull(crossRegionAvailabilityContextForRequest, "Argument 'crossRegionAvailabilityContextForRequest' cannot be null!");

            if (!crossRegionAvailabilityContextForRequest.shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable()) {
                return false;
            }

            // apply PPAF override for reads once - in retry flows stick to applicable regions
            if (crossRegionAvailabilityContextForRequest.hasPerPartitionAutomaticFailoverBeenAppliedForReads()) {
                return false;
            }
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
        PartitionLevelFailoverInfo partitionLevelFailoverInfo = this.partitionKeyRangeToLocation.get(partitionKeyRangeWrapper);

        if (partitionLevelFailoverInfo != null) {

            if (request.isReadOnlyRequest()) {

                CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest
                    = request.requestContext.getCrossRegionAvailabilityContext();

                checkNotNull(crossRegionAvailabilityContextForRequest, "Argument 'crossRegionAvailabilityContextForRequest' cannot be null!");

                crossRegionAvailabilityContextForRequest.setPerPartitionAutomaticFailoverAppliedStatusForReads(true);
            }

            request.requestContext.routeToLocation(partitionLevelFailoverInfo.getCurrent());
            request.requestContext.setPerPartitionAutomaticFailoverInfoHolder(partitionLevelFailoverInfo);
            return true;
        }

        return false;
    }

    public boolean tryMarkEndpointAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request) {

        if (!this.isPerPartitionAutomaticFailoverEnabled) {
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

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            return false;
        }

        if (StringUtils.isEmpty(resolvedCollectionRid)) {
            return false;
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
        RegionalRoutingContext failedRegionalRoutingContext = request.requestContext.regionalRoutingContextToRoute;

        if (failedRegionalRoutingContext == null) {
            return false;
        }

        PartitionLevelFailoverInfo partitionLevelFailoverInfo
            = this.partitionKeyRangeToLocation.computeIfAbsent(partitionKeyRangeWrapper, partitionKeyRangeWrapper1 -> new PartitionLevelFailoverInfo(failedRegionalRoutingContext, this.globalEndpointManager));

        // Rely on account-level read endpoints for new write region discovery
        List<RegionalRoutingContext> accountLevelReadRoutingContexts = this.globalEndpointManager.getAvailableReadRoutingContexts();

        if (partitionLevelFailoverInfo.tryMoveToNextLocation(accountLevelReadRoutingContexts, failedRegionalRoutingContext)) {

            request.requestContext.setPerPartitionAutomaticFailoverInfoHolder(partitionLevelFailoverInfo);
            return true;
        }

        this.partitionKeyRangeToLocation.remove(partitionKeyRangeWrapper);
        return false;
    }

    public boolean isPerPartitionAutomaticFailoverEnabled() {
        return this.isPerPartitionAutomaticFailoverEnabled;
    }

    public boolean isPerPartitionAutomaticFailoverApplicable(RxDocumentServiceRequest request) {

        if (!this.isPerPartitionAutomaticFailoverEnabled) {
            return false;
        }

        if (this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(Collections.emptyList()).size() <= 1) {
            return false;
        }

        checkNotNull(request, "Argument 'request' cannot be null!");

        ResourceType resourceType = request.getResourceType();
        OperationType operationType = request.getOperationType();

        checkNotNull(resourceType, "Argument 'resourceType' cannot be null!");
        checkNotNull(operationType, "Argument 'operationType' cannot be null!");

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        if (resourceType == ResourceType.Document ||
            (resourceType == ResourceType.StoredProcedure && operationType == OperationType.ExecuteJavaScript)) {

            if (!this.globalEndpointManager.canUseMultipleWriteLocations(request)) {
                return true;
            }
        }

        return false;
    }
}
