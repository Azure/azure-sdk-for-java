// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class LocationSpecificHealthContextTransitionHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocationSpecificHealthContextTransitionHandler.class);

    private final ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker;

    public LocationSpecificHealthContextTransitionHandler(ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker) {
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
                        logger.debug("PartitionKeyRange : " +
                            partitionKeyRangeWrapper.getPartitionKeyRange() +
                            " of collectionResourceId : " +
                            partitionKeyRangeWrapper.getCollectionResourceId() +
                            " marked as Healthy from HealthyTentative for region : " +
                            regionWithSuccess);
                        return this.transitionHealthStatus(LocationHealthStatus.Healthy, isReadOnlyRequest);
                    } else {
                        return locationSpecificHealthContextInner;
                    }
                }
                break;
            case Unavailable:
                Instant unavailableSinceActual = locationSpecificHealthContext.getUnavailableSince();
                if (!forceStatusChange) {
                    if (Duration.between(unavailableSinceActual, Instant.now()).compareTo(Duration.ofSeconds(Configs.getAllowedPartitionUnavailabilityDurationInSeconds())) > 0) {
                        logger.debug("PartitionKeyRange : " +
                            partitionKeyRangeWrapper.getPartitionKeyRange() +
                            " of collectionResourceId : " +
                            partitionKeyRangeWrapper.getCollectionResourceId() +
                            " marked as HealthyTentative from Unavailable for region :" +
                            regionWithSuccess);
                        return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative, isReadOnlyRequest);
                    }
                } else {
                    logger.debug("PartitionKeyRange " + partitionKeyRangeWrapper.getPartitionKeyRange() + " and collectionResourceId : " + partitionKeyRangeWrapper.getCollectionResourceId() + " marked as HealthyTentative from Unavailable for region : " + regionWithSuccess);;
                    return this.transitionHealthStatus(LocationHealthStatus.HealthyTentative, isReadOnlyRequest);
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
        String regionWithException,
        boolean isReadOnlyRequest) {

        LocationHealthStatus currentLocationHealthStatusSnapshot = locationSpecificHealthContext.getLocationHealthStatus();

        switch (currentLocationHealthStatusSnapshot) {
            case Healthy:
                logger.debug("PartitionKeyRange " + partitionKeyRangeWrapper.getPartitionKeyRange() + " of collectionResourceId : " + partitionKeyRangeWrapper.getCollectionResourceId() + " marked as HealthyWithFailures from Healthy for region : " + regionWithException);
                return this.transitionHealthStatus(LocationHealthStatus.HealthyWithFailures, isReadOnlyRequest);
            case HealthyWithFailures:
                if (!this.consecutiveExceptionBasedCircuitBreaker.shouldHealthStatusBeDowngraded(locationSpecificHealthContext, isReadOnlyRequest)) {

                    LocationSpecificHealthContext locationSpecificHealthContextInner
                        = this.consecutiveExceptionBasedCircuitBreaker
                        .handleException(
                            locationSpecificHealthContext,
                            partitionKeyRangeWrapper,
                            regionWithException,
                            isReadOnlyRequest);

                    logger.debug("PartitionKeyRange " +
                        partitionKeyRangeWrapper.getPartitionKeyRange() +
                        " of collectionResourceId " +
                        partitionKeyRangeWrapper.getCollectionResourceId() +
                        " has exception count of " +
                        (isReadOnlyRequest ? locationSpecificHealthContextInner.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContextInner.getExceptionCountForWriteForCircuitBreaking()) +
                        " for region : " +
                        regionWithException);

                    return locationSpecificHealthContextInner;
                } else {

                    logger.warn("PartitionKeyRange " +
                        partitionKeyRangeWrapper.getPartitionKeyRange() +
                        " of collectionResourceId " +
                        partitionKeyRangeWrapper.getCollectionResourceId() +
                        " marked as Unavailable from HealthyWithFailures for region : " +
                        regionWithException);
                    return this.transitionHealthStatus(LocationHealthStatus.Unavailable, isReadOnlyRequest);
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
                    logger.warn("PartitionKeyRange " +
                        partitionKeyRangeWrapper.getPartitionKeyRange() +
                        " of collectionResourceId " +
                        partitionKeyRangeWrapper.getCollectionResourceId() +
                        " marked as Unavailable from HealthyTentative for region : " +
                        regionWithException);
                    return this.transitionHealthStatus(LocationHealthStatus.Unavailable, isReadOnlyRequest);
                }
            case Unavailable:
                return this.consecutiveExceptionBasedCircuitBreaker
                    .handleException(
                        locationSpecificHealthContext,
                        partitionKeyRangeWrapper,
                        regionWithException,
                        isReadOnlyRequest);
            default:
                throw new IllegalStateException("Unsupported health status: " + currentLocationHealthStatusSnapshot);
        }
    }

    public LocationSpecificHealthContext transitionHealthStatus(
        LocationHealthStatus newStatus,
        boolean isReadOnlyRequest) {

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

                builder = builder
                    .withUnavailableSince(Instant.MAX)
                    .withLocationHealthStatus(LocationHealthStatus.HealthyWithFailures)
                    .withExceptionThresholdBreached(false);

                if (isReadOnlyRequest) {
                    return builder
                        .withExceptionCountForReadForCircuitBreaking(1)
                        .build();
                } else {
                    return builder
                        .withExceptionCountForWriteForCircuitBreaking(1)
                        .build();
                }

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
