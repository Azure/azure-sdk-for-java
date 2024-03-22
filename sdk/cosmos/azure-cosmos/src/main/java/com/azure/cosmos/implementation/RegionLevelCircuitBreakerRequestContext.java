// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionLevelCircuitBreakerRequestContext {

    private final Map<URI, String> failuresPerRegion;
    private final boolean isRegionLevelCircuitBreakerEnabled;

    public RegionLevelCircuitBreakerRequestContext(boolean isRegionLevelCircuitBreakerEnabled) {
        this.failuresPerRegion = new ConcurrentHashMap<>();
        this.isRegionLevelCircuitBreakerEnabled = isRegionLevelCircuitBreakerEnabled;
    }

    public boolean tryRecordRegionScopedFailure(URI locationEndpointToRoute, int statusCode, int subStatusCode) {
        if (isRegionScopedFailure(statusCode, subStatusCode)) {
            // add to map
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
}
