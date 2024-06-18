// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationContextTransitionHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocationContextTransitionHandler.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;

    public LocationContextTransitionHandler(
        GlobalEndpointManager globalEndpointManager,
        ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker) {

        this.globalEndpointManager = globalEndpointManager;
        this.consecutiveExceptionBasedCircuitBreaker = consecutiveExceptionBasedCircuitBreaker;
    }

    public LocationSpecificContext handleSuccess(
        LocationSpecificContext locationSpecificContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        URI locationWithSuccess,
        boolean forceStatusChange,
        boolean isReadOnlyRequest) {

        LocationHealthStatus currentLocationHealthStatusSnapshot = locationSpecificContext.getLocationHealthStatus();

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();

        switch (currentLocationHealthStatusSnapshot) {
            case Healthy:
                break;
            case HealthyWithFailures:
                if (!forceStatusChange) {
                    if (exceptionCountActual > 0) {
                        return this.consecutiveExceptionBasedCircuitBreaker
                            .handleSuccess(locationSpecificContext, isReadOnlyRequest);
                    }
                }
                break;

            case HealthyTentative:
                if (!forceStatusChange) {

                    LocationSpecificContext locationSpecificContextInner
                        = this.consecutiveExceptionBasedCircuitBreaker.handleSuccess(locationSpecificContext, isReadOnlyRequest);

                    if (this.consecutiveExceptionBasedCircuitBreaker.canHealthStatusBeUpgraded(locationSpecificContextInner, isReadOnlyRequest)) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Partition {}-{} of collection : {} marked as Healthy from HealthyTentative for region : {}",
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                                partitionKeyRangeWrapper.getResourceId(),
                                this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                        }

                        return this.transitionHealthStatus(LocationHealthStatus.Healthy);
                    } else {
                        return locationSpecificContextInner;
                    }
                }
                break;
            case Unavailable:
                Instant unavailableSinceActual = locationSpecificContext.getUnavailableSince();
                if (!forceStatusChange) {
                    if (Duration.between(unavailableSinceActual, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                                partitionKeyRangeWrapper.getResourceId(),
                                this.globalEndpointManager
                                    .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                        }

                        return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative);
                    }
                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getResourceId(),
                            this.globalEndpointManager
                                .getRegionName(locationWithSuccess, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                    }

                    return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative);
                }
                break;
            default:
                throw new IllegalStateException("Unsupported health status: " + currentLocationHealthStatusSnapshot);
        }

        return locationSpecificContext;
    }

    public LocationSpecificContext handleException(
        LocationSpecificContext locationSpecificContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithPossibleUnavailableRegions,
        URI locationWithException,
        boolean isReadOnlyRequest) {

        LocationHealthStatus currentLocationHealthStatusSnapshot = locationSpecificContext.getLocationHealthStatus();

        switch (currentLocationHealthStatusSnapshot) {
            case Healthy:

                if (logger.isDebugEnabled()) {
                    logger.debug("Partition {}-{} of collection : {} marked as HealthyWithFailures from Healthy for region : {}",
                        partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                        partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                        partitionKeyRangeWrapper.getResourceId(),
                        this.globalEndpointManager
                            .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                }

                return this.transitionHealthStatus(LocationHealthStatus.HealthyWithFailures);
            case HealthyWithFailures:
                if (!this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificContext, isReadOnlyRequest)) {

                    LocationSpecificContext locationSpecificContextInner
                        = this.consecutiveExceptionBasedCircuitBreaker.handleException(locationSpecificContext, isReadOnlyRequest);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} has exception count of {} for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getResourceId(),
                            isReadOnlyRequest ? locationSpecificContextInner.getExceptionCountForRead() : locationSpecificContextInner.getExceptionCountForWrite(),
                            this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                    }

                    return locationSpecificContextInner;
                } else {
                    partitionKeyRangesWithPossibleUnavailableRegions.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);

                    if (logger.isDebugEnabled()) {
                        logger.info("Partition {}-{} of collection : {} marked as Unavailable from HealthyWithFailures for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange(),
                            this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                    }

                    return this.transitionHealthStatus(LocationHealthStatus.Unavailable);
                }
            case HealthyTentative:
                if (!this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificContext, isReadOnlyRequest)) {
                    return this.consecutiveExceptionBasedCircuitBreaker.handleException(locationSpecificContext, isReadOnlyRequest);
                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} marked as Unavailable from HealthyTentative for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getResourceId(),
                            this.globalEndpointManager
                                .getRegionName(locationWithException, (isReadOnlyRequest) ? OperationType.Read : OperationType.Create));
                    }

                    return this.transitionHealthStatus(LocationHealthStatus.Unavailable);
                }
            default:
                throw new IllegalStateException("Unsupported health status: " + currentLocationHealthStatusSnapshot);
        }
    }

    public LocationSpecificContext transitionHealthStatus(LocationHealthStatus newStatus) {

        switch (newStatus) {
            case Healthy:
                return new LocationSpecificContext(
                    0,
                    0,
                    0,
                    0,
                    Instant.MAX,
                    LocationHealthStatus.Healthy,
                    false);
            case HealthyWithFailures:
                return new LocationSpecificContext(
                    0,
                    0,
                    0,
                    0,
                    Instant.MAX,
                    LocationHealthStatus.HealthyWithFailures,
                    false);
            case Unavailable:
                return new LocationSpecificContext(
                    0,
                    0,
                    0,
                    0,
                    Instant.now(),
                    LocationHealthStatus.Unavailable,
                    true);
            case HealthyTentative:
                return new LocationSpecificContext(
                    0,
                    0,
                    0,
                    0,
                    Instant.MAX,
                    LocationHealthStatus.HealthyTentative,
                    false);
            default:
                throw new IllegalStateException("Unsupported health status: " + newStatus);
        }
    }
}
