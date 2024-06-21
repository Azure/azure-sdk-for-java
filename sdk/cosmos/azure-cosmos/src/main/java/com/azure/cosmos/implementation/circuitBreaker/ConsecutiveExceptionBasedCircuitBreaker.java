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

    public LocationSpecificContext handleException(LocationSpecificContext locationSpecificContext, boolean isReadOnlyRequest) {

        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificContext;
            case HealthyWithFailures:
            case HealthyTentative:

                exceptionCountAfterHandling++;
                int successCountAfterHandling = 0;

                if (isReadOnlyRequest) {
                    return new LocationSpecificContext(
                        locationSpecificContext.getSuccessCountForWrite(),
                        locationSpecificContext.getExceptionCountForWrite(),
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificContext(
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificContext.getSuccessCountForRead(),
                        locationSpecificContext.getExceptionCountForRead(),
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                }
            case Unavailable:
                throw new IllegalStateException();
            default:
                throw new IllegalArgumentException();
        }
    }

    public LocationSpecificContext handleSuccess(LocationSpecificContext locationSpecificContext, boolean isReadOnlyRequest) {
        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();

        int successCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificContext.getSuccessCountForRead() : locationSpecificContext.getSuccessCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificContext;
            case HealthyWithFailures:

                exceptionCountAfterHandling = 0;

                if (isReadOnlyRequest) {
                    return new LocationSpecificContext(
                        locationSpecificContext.getSuccessCountForWrite(),
                        locationSpecificContext.getExceptionCountForWrite(),
                        locationSpecificContext.getSuccessCountForRead(),
                        exceptionCountAfterHandling,
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificContext(
                        locationSpecificContext.getSuccessCountForWrite(),
                        exceptionCountAfterHandling,
                        locationSpecificContext.getSuccessCountForRead(),
                        locationSpecificContext.getExceptionCountForRead(),
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                }
            case HealthyTentative:

                successCountAfterHandling++;

                if (isReadOnlyRequest) {
                    return new LocationSpecificContext(
                        locationSpecificContext.getSuccessCountForWrite(),
                        locationSpecificContext.getExceptionCountForWrite(),
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                } else {
                    return new LocationSpecificContext(
                        successCountAfterHandling,
                        exceptionCountAfterHandling,
                        locationSpecificContext.getSuccessCountForRead(),
                        locationSpecificContext.getExceptionCountForRead(),
                        locationSpecificContext.getUnavailableSince(),
                        locationSpecificContext.getLocationHealthStatus(),
                        locationSpecificContext.isExceptionThresholdBreached());
                }
            case Unavailable:
                throw new IllegalStateException();
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean shouldHealthStatusBeDowngraded(LocationSpecificContext locationSpecificContext, boolean isReadOnlyRequest) {

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();

        return exceptionCountActual >= getAllowedExceptionCountToMaintainStatus(locationSpecificContext.getLocationHealthStatus(), isReadOnlyRequest);
    }

    public boolean canHealthStatusBeUpgraded(LocationSpecificContext locationSpecificContext, boolean isReadOnlyRequest) {

        int successCountActual
            = isReadOnlyRequest ? locationSpecificContext.getSuccessCountForRead() : locationSpecificContext.getSuccessCountForWrite();

        LocationHealthStatus locationHealthStatus = locationSpecificContext.getLocationHealthStatus();

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
