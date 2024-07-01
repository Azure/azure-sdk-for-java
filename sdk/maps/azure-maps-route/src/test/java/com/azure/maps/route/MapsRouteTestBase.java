// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.route.models.MapsSearchRoute;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeResult;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapsRouteTestBase extends TestProxyTestBase {
    Duration durationTestMode;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
    }

    MapsRouteClientBuilder getRouteAsyncClientBuilder(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteClientBuilder builder = modifyBuilder(httpClient, new MapsRouteClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    MapsRouteClientBuilder modifyBuilder(HttpClient httpClient, MapsRouteClientBuilder builder) {
        httpClient = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build())
                .mapsClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential())
                .mapsClientId("testRouteClient");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .mapsClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    protected <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return syncPoller.setPollInterval(durationTestMode);
    }

    static void validateBeginRequestRouteMatrix(RouteMatrixResult expected, RouteMatrixResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getMatrix().size(), actual.getMatrix().size());
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getSummary().getSuccessfulRoutes(), actual.getSummary().getSuccessfulRoutes());
        assertEquals(expected.getSummary().getTotalRoutes(), actual.getSummary().getTotalRoutes());
    }

    static void validateGetRouteDirections(RouteDirections expected, RouteDirections actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getRoutes().size(), actual.getRoutes().size());

        List<MapsSearchRoute> actualRouteList = actual.getRoutes();
        List<OffsetDateTime> actualDateTimeList = new ArrayList<>();

        if (actualRouteList != null) {
            for (MapsSearchRoute r : actualRouteList) {
                actualDateTimeList.add(r.getSummary().getArrivalTime());
            }
        }

        List<MapsSearchRoute> expectedRouteList = actual.getRoutes();
        List<OffsetDateTime> expectedDateTimeList = new ArrayList<>();

        if (expectedRouteList != null) {
            for (MapsSearchRoute r : expectedRouteList) {
                expectedDateTimeList.add(r.getSummary().getArrivalTime());
            }
        }

        if (!actualDateTimeList.isEmpty()) {
            assertTrue(expectedDateTimeList.contains(actualDateTimeList.get(0)));
        }

        assertEquals(expected.getOptimizedWaypoints(), actual.getOptimizedWaypoints());
    }

    static void validateGetRouteDirectionsWithResponse(RouteDirections expected, Response<RouteDirections> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetRouteDirections(expected, response.getValue());
    }

    static void validateGetRouteRange(RouteRangeResult expected, RouteRangeResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getReachableRange().getBoundary().size(),
            actual.getReachableRange().getBoundary().size());
        assertEquals(expected.getReachableRange().getCenter().getLatitude(),
            actual.getReachableRange().getCenter().getLatitude());
        assertEquals(expected.getReachableRange().getCenter().getLongitude(),
            actual.getReachableRange().getCenter().getLongitude());
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getReport(), actual.getReport());
    }

    static void validateGetRouteRangeWithResponse(RouteRangeResult expected, Response<RouteRangeResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetRouteRange(expected, response.getValue());
    }

    static void validateBeginRequestRouteDirections(RouteDirectionsBatchResult expected,
        RouteDirectionsBatchResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}
