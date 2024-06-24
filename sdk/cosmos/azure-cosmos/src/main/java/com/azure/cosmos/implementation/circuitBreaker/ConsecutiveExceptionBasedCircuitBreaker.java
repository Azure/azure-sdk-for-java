// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsecutiveExceptionBasedCircuitBreaker implements ICircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(ConsecutiveExceptionBasedCircuitBreaker.class);
    private final PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig;

    public ConsecutiveExceptionBasedCircuitBreaker(PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig) {
        this.partitionLevelCircuitBreakerConfig = partitionLevelCircuitBreakerConfig;
    }

    public LocationSpecificHealthContext handleException(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {

        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getExceptionCountForRead() : locationSpecificHealthContext.getExceptionCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificHealthContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificHealthContext;
            case HealthyWithFailures:
            case HealthyTentative:

                exceptionCountAfterHandling++;
                int successCountAfterHandling = 0;

                if (isReadOnlyRequest) {
                    return new LocationSpecificHealthContext(
                        locationSpecificHealthContext.getSuccessCountForWrite(),
                        locationSpecificHealthContext.getExceptionCountForWrite(),
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificHealthContext(
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getSuccessCountForRead(),
                        locationSpecificHealthContext.getExceptionCountForRead(),
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                }
            case Unavailable:
                throw new IllegalStateException();
            default:
                throw new IllegalArgumentException();
        }
    }

    public LocationSpecificHealthContext handleSuccess(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {
        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getExceptionCountForRead() : locationSpecificHealthContext.getExceptionCountForWrite();

        int successCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getSuccessCountForRead() : locationSpecificHealthContext.getSuccessCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificHealthContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificHealthContext;
            case HealthyWithFailures:

                exceptionCountAfterHandling = 0;

                if (isReadOnlyRequest) {
                    return new LocationSpecificHealthContext(
                        locationSpecificHealthContext.getSuccessCountForWrite(),
                        locationSpecificHealthContext.getExceptionCountForWrite(),
                        locationSpecificHealthContext.getSuccessCountForRead(),
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificHealthContext(
                        locationSpecificHealthContext.getSuccessCountForWrite(),
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getSuccessCountForRead(),
                        locationSpecificHealthContext.getExceptionCountForRead(),
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                }
            case HealthyTentative:

                successCountAfterHandling++;

                if (isReadOnlyRequest) {
                    return new LocationSpecificHealthContext(
                        locationSpecificHealthContext.getSuccessCountForWrite(),
                        locationSpecificHealthContext.getExceptionCountForWrite(),
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificHealthContext(
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificHealthContext.getSuccessCountForRead(),
                        locationSpecificHealthContext.getExceptionCountForRead(),
                        locationSpecificHealthContext.getUnavailableSince(),
                        locationSpecificHealthContext.getLocationHealthStatus(),
                        locationSpecificHealthContext.isExceptionThresholdBreached());
                }
            case Unavailable:
                throw new IllegalStateException();
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean shouldHealthStatusBeDowngraded(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificHealthContext.getExceptionCountForRead() : locationSpecificHealthContext.getExceptionCountForWrite();

        return exceptionCountActual >= getAllowedExceptionCountToMaintainStatus(locationSpecificHealthContext.getLocationHealthStatus(), isReadOnlyRequest);
    }

    public boolean canHealthStatusBeUpgraded(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {

        int successCountActual
            = isReadOnlyRequest ? locationSpecificHealthContext.getSuccessCountForRead() : locationSpecificHealthContext.getSuccessCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificHealthContext.getLocationHealthStatus();

        return successCountActual >= getMinimumSuccessCountForStatusUpgrade(locationHealthStatus, isReadOnlyRequest);
    }

    public int getAllowedExceptionCountToMaintainStatus(LocationHealthStatus status, boolean isReadOnlyRequest) {

        if (isReadOnlyRequest) {
            switch (status) {
                case HealthyWithFailures:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForReads();
                case HealthyTentative:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForReads() / 2;
                case Healthy:
                case Unavailable:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case HealthyWithFailures:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites();
                case HealthyTentative:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites() / 2;
                case Healthy:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }
    }

    public int getMinimumSuccessCountForStatusUpgrade(LocationHealthStatus status, boolean isReadOnlyRequest) {
        if (isReadOnlyRequest) {
            switch (status) {
                case HealthyTentative:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForReads();
                case Unavailable:
                case HealthyWithFailures:
                case Healthy:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case HealthyTentative:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites();
                case Unavailable:
                case HealthyWithFailures:
                case Healthy:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }
    }

    public boolean isPartitionLevelCircuitBreakerEnabled() {
        return this.partitionLevelCircuitBreakerConfig.isPartitionLevelCircuitBreakerEnabled();
    }

    public PartitionLevelCircuitBreakerConfig getPartitionLevelCircuitBreakerConfig() {
        return partitionLevelCircuitBreakerConfig;
    }
}
