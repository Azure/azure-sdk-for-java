// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsecutiveExceptionBasedCircuitBreaker {

    private static final Logger logger = LoggerFactory.getLogger(ConsecutiveExceptionBasedCircuitBreaker.class);
    private final PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig;

    public ConsecutiveExceptionBasedCircuitBreaker(PartitionLevelCircuitBreakerConfig partitionLevelCircuitBreakerConfig) {
        this.partitionLevelCircuitBreakerConfig = partitionLevelCircuitBreakerConfig;
    }

    public LocationSpecificHealthContext handleException(
        LocationSpecificHealthContext locationSpecificHealthContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        String regionWithException,
        boolean isReadOnlyRequest) {

        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking();

        LocationHealthStatus locationHealthStatus = locationSpecificHealthContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificHealthContext;
            case HealthyWithFailures:
            case HealthyTentative:

                exceptionCountAfterHandling++;
                int successCountAfterHandling = 0;

                LocationSpecificHealthContext.Builder builder = new LocationSpecificHealthContext.Builder()
                    .withUnavailableSince(locationSpecificHealthContext.getUnavailableSince())
                    .withLocationHealthStatus(locationSpecificHealthContext.getLocationHealthStatus())
                    .withExceptionThresholdBreached(locationSpecificHealthContext.isExceptionThresholdBreached());

                if (isReadOnlyRequest) {

                    return builder
                        .withSuccessCountForWriteForRecovery(locationSpecificHealthContext.getSuccessCountForWriteForRecovery())
                        .withExceptionCountForWriteForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking())
                        .withSuccessCountForReadForRecovery(successCountAfterHandling)
                        .withExceptionCountForReadForCircuitBreaking(exceptionCountAfterHandling)
                        .build();

                } else {

                    return builder
                        .withSuccessCountForWriteForRecovery(successCountAfterHandling)
                        .withExceptionCountForWriteForCircuitBreaking(exceptionCountAfterHandling)
                        .withSuccessCountForReadForRecovery(locationSpecificHealthContext.getSuccessCountForReadForRecovery())
                        .withExceptionCountForReadForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking())
                        .build();
                }
            case Unavailable:
                // the tests done so far view this as an unreachable piece of code - but not failing the operation
                // with IllegalStateException and simply logging that a presumed unreachable code path seems to make sense for now
                logger.warn("Region {} should not be handling failures in {} health status for partition key range : {} and collection RID : {}",
                    regionWithException,
                    locationHealthStatus.getStringifiedLocationHealthStatus(),
                    partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive() + "-" + partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                    partitionKeyRangeWrapper.getCollectionResourceId());
                return locationSpecificHealthContext;
            default:
                throw new IllegalArgumentException("Unsupported health status : " + locationHealthStatus);
        }
    }

    public LocationSpecificHealthContext handleSuccess(
        LocationSpecificHealthContext locationSpecificHealthContext,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        String regionWithSuccess,
        boolean isReadOnlyRequest) {

        int exceptionCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking();

        int successCountAfterHandling
            = (isReadOnlyRequest) ? locationSpecificHealthContext.getSuccessCountForReadForRecovery() : locationSpecificHealthContext.getSuccessCountForWriteForRecovery();

        LocationHealthStatus locationHealthStatus = locationSpecificHealthContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificHealthContext;
            case HealthyWithFailures:

                exceptionCountAfterHandling = 0;

                LocationSpecificHealthContext.Builder builder = new LocationSpecificHealthContext.Builder()
                    .withUnavailableSince(locationSpecificHealthContext.getUnavailableSince())
                    .withLocationHealthStatus(locationSpecificHealthContext.getLocationHealthStatus())
                    .withExceptionThresholdBreached(locationSpecificHealthContext.isExceptionThresholdBreached());

                if (isReadOnlyRequest) {

                    return builder
                        .withSuccessCountForWriteForRecovery(locationSpecificHealthContext.getSuccessCountForWriteForRecovery())
                        .withExceptionCountForWriteForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking())
                        .withSuccessCountForReadForRecovery(locationSpecificHealthContext.getSuccessCountForReadForRecovery())
                        .withExceptionCountForReadForCircuitBreaking(exceptionCountAfterHandling)
                        .build();

                } else {

                    return builder
                        .withSuccessCountForWriteForRecovery(locationSpecificHealthContext.getSuccessCountForWriteForRecovery())
                        .withExceptionCountForWriteForCircuitBreaking(exceptionCountAfterHandling)
                        .withSuccessCountForReadForRecovery(locationSpecificHealthContext.getSuccessCountForReadForRecovery())
                        .withExceptionCountForReadForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking())
                        .build();
                }
            case HealthyTentative:

                successCountAfterHandling++;

                builder = new LocationSpecificHealthContext.Builder()
                    .withUnavailableSince(locationSpecificHealthContext.getUnavailableSince())
                    .withLocationHealthStatus(locationSpecificHealthContext.getLocationHealthStatus())
                    .withExceptionThresholdBreached(locationSpecificHealthContext.isExceptionThresholdBreached());

                if (isReadOnlyRequest) {

                    return builder
                        .withSuccessCountForWriteForRecovery(locationSpecificHealthContext.getSuccessCountForWriteForRecovery())
                        .withExceptionCountForWriteForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking())
                        .withSuccessCountForReadForRecovery(successCountAfterHandling)
                        .withExceptionCountForReadForCircuitBreaking(exceptionCountAfterHandling)
                        .build();

                } else {

                    return builder
                        .withSuccessCountForWriteForRecovery(successCountAfterHandling)
                        .withExceptionCountForWriteForCircuitBreaking(exceptionCountAfterHandling)
                        .withSuccessCountForReadForRecovery(locationSpecificHealthContext.getSuccessCountForReadForRecovery())
                        .withExceptionCountForReadForCircuitBreaking(locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking())
                        .build();

                }
            case Unavailable:
                // the tests done so far view this as an unreachable piece of code - but not failing the operation
                // and simply logging that a presumed unreachable code path seems to make sense for now
                logger.warn("Region {} should not be handling successes in {} health status for partition key range : {} and collection RID : {}",
                    regionWithSuccess,
                    locationHealthStatus.getStringifiedLocationHealthStatus(),
                    partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive() + "-" + partitionKeyRangeWrapper.getPartitionKeyRange().getMinInclusive(),
                    partitionKeyRangeWrapper.getCollectionResourceId());
                return locationSpecificHealthContext;
            default:
                throw new IllegalArgumentException("Unsupported health status : " + locationHealthStatus);
        }
    }

    public boolean shouldHealthStatusBeDowngraded(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() : locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking();

        return exceptionCountActual >= getAllowedExceptionCountToMaintainStatus(locationSpecificHealthContext.getLocationHealthStatus(), isReadOnlyRequest);
    }

    public boolean canHealthStatusBeUpgraded(LocationSpecificHealthContext locationSpecificHealthContext, boolean isReadOnlyRequest) {

        int successCountActual
            = isReadOnlyRequest ? locationSpecificHealthContext.getSuccessCountForReadForRecovery() : locationSpecificHealthContext.getSuccessCountForWriteForRecovery();

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
                    throw new IllegalArgumentException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case HealthyWithFailures:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites();
                case HealthyTentative:
                    return this.partitionLevelCircuitBreakerConfig.getConsecutiveExceptionCountToleratedForWrites() / 2;
                case Healthy:
                case Unavailable:
                    return 0;
                default:
                    throw new IllegalArgumentException("Unsupported health status: " + status);
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
                    throw new IllegalArgumentException("Unsupported health status: " + status);
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
                    throw new IllegalArgumentException("Unsupported health status: " + status);
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
