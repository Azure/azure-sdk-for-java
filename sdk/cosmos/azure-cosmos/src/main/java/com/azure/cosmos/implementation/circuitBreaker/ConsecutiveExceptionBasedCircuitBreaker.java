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

        GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus locationHealthStatus
            = locationSpecificContext.getLocationHealthStatus();

        switch (locationHealthStatus) {
            case Healthy:
                return locationSpecificContext;
            case HealthyWithFailures:
            case HealthyTentative:

                exceptionCountAfterHandling++;

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

        GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus locationHealthStatus
            = locationSpecificContext.getLocationHealthStatus();

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

        int exceptionCountActual
            = isReadOnlyRequest ? locationSpecificContext.getExceptionCountForRead() : locationSpecificContext.getExceptionCountForWrite();

        GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus locationHealthStatus = locationSpecificContext.getLocationHealthStatus();

        return successCountActual >= getMinimumSuccessCountForStatusUpgrade(locationHealthStatus, isReadOnlyRequest) &&
            (double) exceptionCountActual / (double) successCountActual < getAllowedExceptionToSuccessRatio(locationHealthStatus, isReadOnlyRequest);
    }

    private static double getAllowedExceptionToSuccessRatio(GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus status, boolean isReadOnlyRequest) {

        if (isReadOnlyRequest) {
            switch (status) {
                case HealthyWithFailures:
                    return 0.3d;
                case HealthyTentative:
                    return 0.1d;
                default:
                    return 0d;
            }
        } else {
            switch (status) {
                case HealthyWithFailures:
                    return 0.2d;
                case HealthyTentative:
                    return 0.05d;
                default:
                    return 0d;
            }
        }
    }

    public int getAllowedExceptionCountToMaintainStatus(GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus status, boolean isReadOnlyRequest) {

        if (isReadOnlyRequest) {
            switch (status) {
                case HealthyWithFailures:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 20;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 40;
                    }
                case HealthyTentative:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 5;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 20;
                    }
                case Healthy:
                    return 0;
                case Unavailable:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case HealthyWithFailures:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 5;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 20;
                    } else {
                        throw new IllegalArgumentException("Unsupported tolerance setting " + this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance());
                    }
                case HealthyTentative:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 5;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 3;
                    } else {
                        throw new IllegalArgumentException("Unsupported tolerance setting " + this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance());
                    }
                case Healthy:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        }
    }

    public int getMinimumSuccessCountForStatusUpgrade(GlobalPartitionEndpointManagerForCircuitBreaker.LocationHealthStatus status, boolean isReadOnlyRequest) {
        if (isReadOnlyRequest) {
            switch (status) {
                case HealthyTentative:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 5;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 3;
                    } else {
                        throw new IllegalArgumentException("Unsupported tolerance setting " + this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance());
                    }
                case Unavailable:
                    return 0;
                case HealthyWithFailures:
                    return 0;
                case Healthy:
                    return 0;
                default:
                    throw new IllegalStateException("Unsupported health status: " + status);
            }
        } else {
            switch (status) {
                case HealthyTentative:
                    if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("LOW")) {
                        return 20;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("MEDIUM")) {
                        return 10;
                    } else if (this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance().equals("HIGH")) {
                        return 5;
                    } else {
                        throw new IllegalArgumentException("Unsupported tolerance setting " + this.partitionLevelCircuitBreakerConfig.getCircuitBreakerFailureTolerance());
                    }
                case Unavailable:
                    return 0;
                case HealthyWithFailures:
                    return 0;
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
}
