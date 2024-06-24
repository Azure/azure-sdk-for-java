// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import java.time.Instant;

public class LocationSpecificHealthContext {
    private final int exceptionCountForWrite;
    private final int successCountForWrite;
    private final int exceptionCountForRead;
    private final int successCountForRead;
    private final Instant unavailableSince;
    private final LocationHealthStatus locationHealthStatus;
    private final boolean isExceptionThresholdBreached;

    public LocationSpecificHealthContext(
        int successCountForWrite,
        int exceptionCountForWrite,
        int successCountForRead,
        int exceptionCountForRead,
        Instant unavailableSince,
        LocationHealthStatus locationHealthStatus,
        boolean isExceptionThresholdBreached) {

        this.successCountForWrite = successCountForWrite;
        this.exceptionCountForWrite = exceptionCountForWrite;
        this.exceptionCountForRead = exceptionCountForRead;
        this.successCountForRead = successCountForRead;
        this.unavailableSince = unavailableSince;
        this.locationHealthStatus = locationHealthStatus;
        this.isExceptionThresholdBreached = isExceptionThresholdBreached;
    }

    public boolean isExceptionThresholdBreached() {
        return this.isExceptionThresholdBreached;
    }

    public boolean isRegionAvailableToProcessRequests() {
        return this.locationHealthStatus == LocationHealthStatus.Healthy ||
            this.locationHealthStatus == LocationHealthStatus.HealthyWithFailures ||
            this.locationHealthStatus == LocationHealthStatus.HealthyTentative;
    }

    public int getExceptionCountForWrite() {
        return exceptionCountForWrite;
    }

    public int getSuccessCountForWrite() {
        return successCountForWrite;
    }

    public int getExceptionCountForRead() {
        return exceptionCountForRead;
    }

    public int getSuccessCountForRead() {
        return successCountForRead;
    }

    public Instant getUnavailableSince() {
        return unavailableSince;
    }

    public LocationHealthStatus getLocationHealthStatus() {
        return locationHealthStatus;
    }
}
