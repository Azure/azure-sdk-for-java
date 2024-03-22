// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class LocationLevelCircuitBreakerRequestContext {

    private final ConcurrentHashMap<URI, ConcurrentHashMap<ErrorKey, Integer>> failuresForAllLocations;
    private final boolean isRegionLevelCircuitBreakerEnabled;

    public LocationLevelCircuitBreakerRequestContext(boolean isRegionLevelCircuitBreakerEnabled) {
        this.failuresForAllLocations = new ConcurrentHashMap<>();
        this.isRegionLevelCircuitBreakerEnabled = isRegionLevelCircuitBreakerEnabled;
    }

    public boolean tryRecordRegionScopedFailure(URI locationEndpointToRoute, int statusCode, int subStatusCode) {
        if (isRegionScopedFailure(statusCode, subStatusCode)) {
            failuresForAllLocations.compute(locationEndpointToRoute, ((uri, errorKeyToCount) -> {

                if (errorKeyToCount == null) {
                    errorKeyToCount = new ConcurrentHashMap<>();
                    errorKeyToCount.put(new ErrorKey(statusCode, subStatusCode), 1);
                    return errorKeyToCount;
                }

                errorKeyToCount.compute(new ErrorKey(statusCode, subStatusCode), (errorKey, count) -> {

                    if (count == null) {
                        count = 1;
                        return count;
                    }

                    return count + 1;
                });

                return errorKeyToCount;
            }));

            return true;
        }
        return false;
    }

    private static boolean isRegionScopedFailure(int statusCode, int subStatusCode) {

        if (statusCode == HttpConstants.StatusCodes.GONE
            && subStatusCode == HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION) {
            return true;
        }

        if (statusCode == HttpConstants.StatusCodes.GONE
            && subStatusCode == HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION_EXCEEDED_RETRY_LIMIT) {
            return true;
        }

        if (statusCode == HttpConstants.StatusCodes.GONE
            && subStatusCode == HttpConstants.SubStatusCodes.SERVER_GENERATED_410) {
            return true;
        }

        if (statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
            && subStatusCode == HttpConstants.SubStatusCodes.SERVER_GENERATED_503) {
            return true;
        }

        if (statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
            && subStatusCode == HttpConstants.SubStatusCodes.SERVER_GENERATED_503) {
            return true;
        }

        return false;
    }

    public ConcurrentHashMap<URI, ConcurrentHashMap<ErrorKey, Integer>> getFailuresForAllLocations() {
        return this.failuresForAllLocations;
    }
}
