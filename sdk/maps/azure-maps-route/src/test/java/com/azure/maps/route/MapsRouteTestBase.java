// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
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
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";

    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    static InterceptorManager interceptorManagerTestBase;

    Duration durationTestMode;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }

        interceptorManagerTestBase = interceptorManager;
    }

    MapsRouteClientBuilder getRouteAsyncClientBuilder(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder()
            .pipeline(getHttpPipeline(httpClient))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        httpClient = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(
                Collections.singletonList(
                    new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))));
        policies.add(
            new AzureKeyCredentialPolicy(
                MapsRouteClientBuilder.MAPS_SUBSCRIPTION_KEY,
                new AzureKeyCredential(interceptorManager.isPlaybackMode()
                                       ? FAKE_API_KEY
                                       : Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
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

        if (actualDateTimeList != null && actualDateTimeList.size() > 0) {
            assertTrue(expectedDateTimeList.contains(actualDateTimeList.get(0)));
        }

        assertEquals(expected.getOptimizedWaypoints(), actual.getOptimizedWaypoints());
    }

    static void validateGetRouteDirectionsWithResponse(RouteDirections expected, int expectedStatusCode,
                                                       Response<RouteDirections> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateGetRouteRangeWithResponse(RouteRangeResult expected, int expectedStatusCode,
                                                  Response<RouteRangeResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetRouteRange(expected, response.getValue());
    }

    static void validateBeginRequestRouteDirections(RouteDirectionsBatchResult expected,
                                                    RouteDirectionsBatchResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}
