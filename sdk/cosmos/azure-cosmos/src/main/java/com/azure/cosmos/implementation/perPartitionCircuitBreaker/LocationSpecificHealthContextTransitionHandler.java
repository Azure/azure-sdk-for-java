// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class LocationSpecificHealthContextTransitionHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocationSpecificHealthContextTransitionHandler.class);

    private final GlobalEndpointManager globalEndpointManager;
    private final ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;

    public LocationSpecificHealthContextTransitionHandler(
        GlobalEndpointManager globalEndpointManager,
        ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker) {

        this.globalEndpointManager = globalEndpointManager;
        this.consecutiveExceptionBasedCircuitBreaker = consecutiveExceptionBasedCircuitBreaker;
    }

    public LocationSpecificHealthContext handleSuccess(
        LocationSpecificHealthContext locationSpecificHealthContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        String regionWithSuccess,
        boolean forceStatusChange,
        boolean isReadOnlyRequest) {

        LocationHealthStatus currentLocationHealthStatusSnapshot = locationSpecificHealthContext.getLocationHealthStatus();

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking();

        switch (currentLocationHealthStatusSnapshot) {
            case Healthy:
                break;
            case HealthyWithFailures:
                if (!forceStatusChange) {
                    if (exceptionCountActual > 0) {
                        return this.consecutiveExceptionBasedCircuitBreaker
                            .handleSuccess(
                                locationSpecificHealthContext,
                                partitionKeyRangeWrapper,
                                regionWithSuccess,
                                isReadOnlyRequest);
                    }
                }
                break;

            case HealthyTentative:
                if (!forceStatusChange) {

                    LocationSpecificHealthContext locationSpecificHealthContextInner
                        = this.consecutiveExceptionBasedCircuitBreaker
                        .handleSuccess(
                            locationSpecificHealthContext,
                            partitionKeyRangeWrapper,
                            regionWithSuccess,
                            isReadOnlyRequest);

                    if (this.consecutiveExceptionBasedCircuitBreaker.canHealthStatusBeUpgraded(locationSpecificHealthContextInner, isReadOnlyRequest)) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Partition {}-{} of collection : {} marked as Healthy from HealthyTentative for region : {}",
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                                partitionKeyRangeWrapper.getCollectionResourceId(),
                                regionWithSuccess);
                        }

                        return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.Healthy);
                    } else {
                        return locationSpecificHealthContextInner;
                    }
                }
                break;
            case Unavailable:
                Instant unavailableSinceActual = locationSpecificHealthContext.getUnavailableSince();
                if (!forceStatusChange) {
                    if (Duration.between(unavailableSinceActual, Instant.now()).compareTo(Duration.ofSeconds(Configs.getAllowedPartitionUnavailabilityDurationInSeconds())) > 0) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                                partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                                partitionKeyRangeWrapper.getCollectionResourceId(),
                                regionWithSuccess);
                        }

                        return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.HealthyTentative);
                    }
                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} marked as HealthyTentative from Unavailable for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getCollectionResourceId(),
                            regionWithSuccess);
                    }

                    return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.HealthyTentative);
                }
                break;
            default:
                throw new IllegalStateException("Unsupported health status: " + currentLocationHealthStatusSnapshot);
        }

        return locationSpecificHealthContext;
    }

    public LocationSpecificHealthContext handleException(
        LocationSpecificHealthContext locationSpecificHealthContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        ConcurrentHashMap<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithPossibleUnavailableRegions,
        String regionWithException,
        boolean isReadOnlyRequest) {

        LocationHealthStatus currentLocationHealthStatusSnapshot = locationSpecificHealthContext.getLocationHealthStatus();

        switch (currentLocationHealthStatusSnapshot) {
            case Healthy:

                if (logger.isDebugEnabled()) {
                    logger.debug("Partition {}-{} of collection : {} marked as HealthyWithFailures from Healthy for region : {}",
                        partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                        partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                        partitionKeyRangeWrapper.getCollectionResourceId(),
                        regionWithException);
                }

                return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.HealthyWithFailures);
            case HealthyWithFailures:
                if (!this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificHealthContext, isReadOnlyRequest)) {

                    LocationSpecificHealthContext locationSpecificHealthContextInner
                        = this.consecutiveExceptionBasedCircuitBreaker
                        .handleException(
                            locationSpecificHealthContext,
                            partitionKeyRangeWrapper,
                            regionWithException,
                            isReadOnlyRequest);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} has exception count of {} for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getCollectionResourceId(),
                            isReadOnlyRequest ? locationSpecificHealthContextInner.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContextInner.getExceptionCountForWriteForCircuitBreaking(),
                            regionWithException);
                    }

                    return locationSpecificHealthContextInner;
                } else {
                    partitionKeyRangesWithPossibleUnavailableRegions.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);

                    if (logger.isDebugEnabled()) {
                        logger.info("Partition {}-{} of collection : {} marked as Unavailable from HealthyWithFailures for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange(),
                            regionWithException);
                    }

                    return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.Unavailable);
                }
            case HealthyTentative:
                if (!this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificHealthContext, isReadOnlyRequest)) {
                    return this.consecutiveExceptionBasedCircuitBreaker
                        .handleException(
                            locationSpecificHealthContext,
                            partitionKeyRangeWrapper,
                            regionWithException,
                            isReadOnlyRequest);
                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Partition {}-{} of collection : {} marked as Unavailable from HealthyTentative for region : {}",
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                            partitionKeyRangeWrapper.getPartitionKeyRange().getMaxExclusive(),
                            partitionKeyRangeWrapper.getCollectionResourceId(),
                            regionWithException);
                    }

                    return this.transitionHealthStatus(locationSpecificHealthContext, LocationHealthStatus.Unavailable);
                }
            default:
                throw new IllegalStateException("Unsupported health status: " + currentLocationHealthStatusSnapshot);
        }
    }

    public LocationSpecificHealthContext transitionHealthStatus(LocationSpecificHealthContext locationSpecificHealthContext, LocationHealthStatus newStatus) {

        LocationSpecificHealthContext.Builder builder = new LocationSpecificHealthContext.Builder()
            .withSuccessCountForWriteForRecovery(0)
            .withExceptionCountForWriteForCircuitBreaking(0)
            .withSuccessCountForReadForRecovery(0)
            .withExceptionCountForReadForCircuitBreaking(0);

        switch (newStatus) {
            case Healthy:

                return builder
                    .withUnavailableSince(Instant.MAX)
                    .withLocationHealthStatus(LocationHealthStatus.Healthy)
                    .withExceptionThresholdBreached(false)
                    .build();

            case HealthyWithFailures:

                return builder
                    .withUnavailableSince(Instant.MAX)
                    .withLocationHealthStatus(LocationHealthStatus.HealthyWithFailures)
                    .withExceptionThresholdBreached(false)
                    .build();

            case Unavailable:

                return builder
                    .withUnavailableSince(Instant.now())
                    .withLocationHealthStatus(LocationHealthStatus.Unavailable)
                    .withExceptionThresholdBreached(true)
                    .build();

            case HealthyTentative:

                return builder
                    .withUnavailableSince(Instant.now())
                    .withLocationHealthStatus(LocationHealthStatus.HealthyTentative)
                    .withExceptionThresholdBreached(false)
                    .build();

            default:
                throw new IllegalStateException("Unsupported health status: " + newStatus);
        }
    }
}
