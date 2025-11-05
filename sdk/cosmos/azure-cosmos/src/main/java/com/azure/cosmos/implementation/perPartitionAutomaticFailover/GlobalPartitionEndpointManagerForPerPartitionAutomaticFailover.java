// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.Configs;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover {
    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class);
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionLevelFailoverInfo> partitionKeyRangeToFailoverInfo;
    private final ConcurrentHashMap<PartitionKeyRangeWrapper, EndToEndTimeoutErrorTracker> partitionKeyRangeToEndToEndTimeoutErrorTracker;
    private final GlobalEndpointManager globalEndpointManager;
    private final AtomicBoolean isPerPartitionAutomaticFailoverEnabled;
    private final AtomicInteger warnLevelLoggedCounts = new AtomicInteger(0);

    public GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(
        GlobalEndpointManager globalEndpointManager,
        boolean isPerPartitionAutomaticFailoverEnabled) {

        this.globalEndpointManager = globalEndpointManager;
        this.partitionKeyRangeToFailoverInfo = new ConcurrentHashMap<>();
        this.partitionKeyRangeToEndToEndTimeoutErrorTracker = new ConcurrentHashMap<>();
        this.isPerPartitionAutomaticFailoverEnabled = new AtomicBoolean(isPerPartitionAutomaticFailoverEnabled);
    }

    public boolean resetEndToEndTimeoutErrorCountIfPossible(RxDocumentServiceRequest request) {

        boolean isPerPartitionAutomaticFailoverEnabledSnapshot = this.isPerPartitionAutomaticFailoverEnabled.get();

        if (isPerPartitionAutomaticFailoverEnabledSnapshot) {
            return false;
        }

        if (request == null) {
            logAsWarnOrDebug("Argument 'request' is null, " +
                "hence resetEndToEndTimeoutErrorCountIfPossible cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.requestContext == null) {
            logAsWarnOrDebug("Argument 'request.requestContext' is null, " +
                "hence resetEndToEndTimeoutErrorCountIfPossible cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (!isPerPartitionAutomaticFailoverApplicable(request)) {
            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover' " +
                "is null, hence resetEndToEndTimeoutErrorCountIfPossible cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (StringUtils.isEmpty(resolvedCollectionRid)) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedCollectionRid' is null, " +
                "hence resetEndToEndTimeoutErrorCountIfPossible cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);

        if (!this.partitionKeyRangeToEndToEndTimeoutErrorTracker.containsKey(partitionKeyRangeWrapper)) {
            return true;
        }

        this.partitionKeyRangeToEndToEndTimeoutErrorTracker.remove(partitionKeyRangeWrapper);

        return true;
    }

    public boolean tryAddPartitionLevelLocationOverride(RxDocumentServiceRequest request) {

        boolean isPerPartitionAutomaticFailoverEnabledSnapshot = this.isPerPartitionAutomaticFailoverEnabled.get();

        if (!isPerPartitionAutomaticFailoverEnabledSnapshot) {
            return false;
        }

        if (request == null) {
            logAsWarnOrDebug("Argument 'request' is null, " +
                "hence tryAddPartitionLevelLocationOverride cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.requestContext == null) {
            logAsWarnOrDebug("Argument 'request.requestContext' is null, " +
                "hence tryAddPartitionLevelLocationOverride cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.getResourceType() != ResourceType.Document) {
            return false;
        }

        if (request.getOperationType() == OperationType.QueryPlan) {
            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover' " +
                "is null, hence tryAddPartitionLevelLocationOverride cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (StringUtils.isEmpty(resolvedCollectionRid)) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedCollectionRid' is null, " +
                "hence tryAddPartitionLevelLocationOverride cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.isReadOnlyRequest()) {
            CrossRegionAvailabilityContextForRxDocumentServiceRequest crossRegionAvailabilityContextForRequest
                = request.requestContext.getCrossRegionAvailabilityContext();

            if (crossRegionAvailabilityContextForRequest == null) {
                logAsWarnOrDebug("Argument 'request.requestContext.getCrossRegionAvailabilityContext()' is null, " +
                    "hence tryAddPartitionLevelLocationOverride cannot be performed", this.warnLevelLoggedCounts);
                return false;
            }

            if (!crossRegionAvailabilityContextForRequest.shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable()) {
                return false;
            }

            // apply PPAF override for reads once - in retry flows stick to applicable regions
            if (crossRegionAvailabilityContextForRequest.hasPerPartitionAutomaticFailoverBeenAppliedForReads()) {
                return false;
            }
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);
        PartitionLevelFailoverInfo partitionLevelFailoverInfo = this.partitionKeyRangeToFailoverInfo.get(partitionKeyRangeWrapper);

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

    public boolean tryMarkEndpointAsUnavailableForPartitionKeyRange(RxDocumentServiceRequest request, boolean isEndToEndTimeoutHit) {

        boolean isPerPartitionAutomaticFailoverEnabledSnapshot = this.isPerPartitionAutomaticFailoverEnabled.get();

        if (!isPerPartitionAutomaticFailoverEnabledSnapshot) {
            return false;
        }

        if (!isPerPartitionAutomaticFailoverApplicable(request)) {
            return false;
        }

        PartitionKeyRange partitionKeyRange = request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover;
        String resolvedCollectionRid = request.requestContext.resolvedCollectionRid;

        if (partitionKeyRange == null) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover' " +
                "is null, hence tryMarkEndpointAsUnavailableForPartitionKeyRange cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (StringUtils.isEmpty(resolvedCollectionRid)) {
            logAsWarnOrDebug("Argument 'request.requestContext.resolvedCollectionRid' is null, " +
                "hence tryMarkEndpointAsUnavailableForPartitionKeyRange cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resolvedCollectionRid);

        if (isEndToEndTimeoutHit) {
            EndToEndTimeoutErrorTracker endToEndTimeoutErrorTrackerSnapshot = this.partitionKeyRangeToEndToEndTimeoutErrorTracker.get(partitionKeyRangeWrapper);

            if (endToEndTimeoutErrorTrackerSnapshot != null) {

                Instant failureWindowStart = endToEndTimeoutErrorTrackerSnapshot.getFailureWindowStart();
                int errorCount = endToEndTimeoutErrorTrackerSnapshot.getErrorCount();

                if (Duration.between(failureWindowStart, Instant.now()).getSeconds() >= Configs.getAllowedTimeWindowForE2ETimeoutHitCountTrackingInSecsForPPAF()) {
                    this.partitionKeyRangeToEndToEndTimeoutErrorTracker.put(partitionKeyRangeWrapper, new EndToEndTimeoutErrorTracker(Instant.now(), 1));
                    return false;
                } else if (errorCount < Configs.getAllowedE2ETimeoutHitCountForPPAF()) {
                    this.partitionKeyRangeToEndToEndTimeoutErrorTracker.put(partitionKeyRangeWrapper, new EndToEndTimeoutErrorTracker(failureWindowStart, errorCount + 1));
                    return false;
                }
            } else {
                this.partitionKeyRangeToEndToEndTimeoutErrorTracker.put(partitionKeyRangeWrapper, new EndToEndTimeoutErrorTracker(Instant.now(), 1));
                return false;
            }
        }

        RegionalRoutingContext failedRegionalRoutingContext = request.requestContext.regionalRoutingContextToRoute;

        if (failedRegionalRoutingContext == null) {
            return false;
        }

        PartitionLevelFailoverInfo partitionLevelFailoverInfo
            = this.partitionKeyRangeToFailoverInfo.computeIfAbsent(partitionKeyRangeWrapper, partitionKeyRangeWrapper1 -> new PartitionLevelFailoverInfo(failedRegionalRoutingContext, this.globalEndpointManager));

        // Rely on account-level read endpoints for new write region discovery
        List<RegionalRoutingContext> accountLevelReadRoutingContexts = this.globalEndpointManager.getAvailableReadRoutingContexts();

        if (partitionLevelFailoverInfo.tryMoveToNextLocation(accountLevelReadRoutingContexts, failedRegionalRoutingContext)) {

            if (logger.isWarnEnabled()) {
                logger.warn("Marking region {} as failed for partition key range {} and collection rid {}",
                    request.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint(),
                    partitionKeyRangeWrapper.getPartitionKeyRange(),
                    partitionKeyRangeWrapper.getCollectionResourceId());
            }

            request.requestContext.setPerPartitionAutomaticFailoverInfoHolder(partitionLevelFailoverInfo);

            this.partitionKeyRangeToEndToEndTimeoutErrorTracker.remove(partitionKeyRangeWrapper);
            return true;
        }

        this.partitionKeyRangeToFailoverInfo.remove(partitionKeyRangeWrapper);
        this.partitionKeyRangeToEndToEndTimeoutErrorTracker.remove(partitionKeyRangeWrapper);

        return false;
    }

    public boolean isPerPartitionAutomaticFailoverEnabled() {
        return this.isPerPartitionAutomaticFailoverEnabled.get();
    }

    public boolean isPerPartitionAutomaticFailoverApplicable(RxDocumentServiceRequest request) {

        boolean isPerPartitionAutomaticFailoverEnabledSnapshot = this.isPerPartitionAutomaticFailoverEnabled.get();

        if (!isPerPartitionAutomaticFailoverEnabledSnapshot) {
            return false;
        }

        if (request == null) {
            logAsWarnOrDebug("Argument 'request' is null, " +
                "hence isPerPartitionAutomaticFailoverApplicable cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.requestContext == null) {
            logAsWarnOrDebug("Argument 'request.requestContext' is null, " +
                "hence isPerPartitionAutomaticFailoverApplicable cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.isReadOnlyRequest()) {
            return false;
        }

        if (this.globalEndpointManager.getApplicableReadRegionalRoutingContexts(Collections.emptyList()).size() <= 1) {
            return false;
        }

        if (request.getResourceType() == null) {
            logAsWarnOrDebug("Argument 'request.getResourceType()' is null, " +
                "hence isPerPartitionAutomaticFailoverApplicable cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        if (request.getOperationType() == null) {
            logAsWarnOrDebug("Argument 'request.getOperationType()' is null, " +
                "hence isPerPartitionAutomaticFailoverApplicable cannot be performed", this.warnLevelLoggedCounts);
            return false;
        }

        ResourceType resourceType = request.getResourceType();
        OperationType operationType = request.getOperationType();

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

    public synchronized void resetPerPartitionAutomaticFailoverEnabled(boolean isPerPartitionAutomaticFailoverEnabled) {
        this.isPerPartitionAutomaticFailoverEnabled.set(isPerPartitionAutomaticFailoverEnabled);
        this.clear();
    }

    private void clear() {
        this.partitionKeyRangeToFailoverInfo.clear();
        this.partitionKeyRangeToEndToEndTimeoutErrorTracker.clear();
        this.warnLevelLoggedCounts.set(0);
    }

    private static void logAsWarnOrDebug(String message, AtomicInteger warnLogThreshold) {
        // warnLogThreshold is not atomic still but with interleaved
        // updates there would be few extra warn logs in the worst case
        if (warnLogThreshold.get() < Configs.getWarnLevelLoggingThresholdForPpaf()) {
            logger.warn(message);
            warnLogThreshold.incrementAndGet();
        } else {
            logger.debug(message);
        }
    }
}
