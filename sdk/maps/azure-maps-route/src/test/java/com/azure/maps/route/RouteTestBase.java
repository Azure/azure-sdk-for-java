package com.azure.maps.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.route.models.Route;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeResult;

public class RouteTestBase extends TestBase{
    static final String FAKE_API_KEY = "1234567890";

    private final String endpoint = Configuration.getGlobalConfiguration().get("API-LEARN_ENDPOINT");
    Duration durationTestMode;
    static InterceptorManager interceptorManagerTestBase;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }
    
    RouteClientBuilder getRouteAsyncClientBuilder(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteClientBuilder builder = new RouteClientBuilder()
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        .serviceVersion(serviceVersion);
        String endpoint = getEndpoint();
        if (getEndpoint() != null) {
            builder.endpoint(endpoint);
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY)).httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.credential((new AzureKeyCredential(
                Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));
        }
        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, endpoint.replaceFirst("/$", "") + "/.default"));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
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
        List<Route> actualRouteList = actual.getRoutes();
        List<OffsetDateTime> actualDateTimeList = new ArrayList<>();
        if (actualRouteList != null) {
            for (Route r : actualRouteList) {
                actualDateTimeList.add(r.getSummary().getArrivalTime());
            }
        }
        List<Route> expectedRouteList = actual.getRoutes();
        List<OffsetDateTime> expectedDateTimeList = new ArrayList<>();
        if (expectedRouteList != null) {
            for (Route r : expectedRouteList) {
                expectedDateTimeList.add(r.getSummary().getArrivalTime());
            }
        }
        if (actualDateTimeList != null && actualDateTimeList.size() > 0) {
            assertTrue(expectedDateTimeList.contains(actualDateTimeList.get(0)));
        }
        assertEquals(expected.getOptimizedWaypoints(), actual.getOptimizedWaypoints());
    }

    static void validateGetRouteDirectionsWithResponse(RouteDirections expected, int expectedStatusCode, Response<RouteDirections> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetRouteDirections(expected, response.getValue());
    }

    static void validateGetRouteRange(RouteRangeResult expected, RouteRangeResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getReachableRange().getBoundary().size(), actual.getReachableRange().getBoundary().size());
        assertEquals(expected.getReachableRange().getCenter().getLatitude(), actual.getReachableRange().getCenter().getLatitude());
        assertEquals(expected.getReachableRange().getCenter().getLongitude(), actual.getReachableRange().getCenter().getLongitude());
        assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
        assertEquals(expected.getReport(), actual.getReport());
    }

    static void validateGetRouteRangeWithResponse(RouteRangeResult expected, int expectedStatusCode, Response<RouteRangeResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetRouteRange(expected, response.getValue());
    }

    static void validateBeginRequestRouteDirections(RouteDirectionsBatchResult expected, RouteDirectionsBatchResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}