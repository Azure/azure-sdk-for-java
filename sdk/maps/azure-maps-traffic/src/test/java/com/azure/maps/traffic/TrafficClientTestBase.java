// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficIncidentViewport;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrafficClientTestBase extends TestProxyTestBase {
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

    TrafficClientBuilder getTrafficAsyncClientBuilder(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficClientBuilder builder = new TrafficClientBuilder()
            .pipeline(getHttpPipeline(httpClient))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(
                Collections.singletonList(
                    new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))));
        policies.add(
            new AzureKeyCredentialPolicy(
                TrafficClientBuilder.MAPS_SUBSCRIPTION_KEY,
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

    static void validateGetTrafficFlowTile(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficFlowTileWithResponse(int expectedStatusCode, Response<BinaryData> response)
        throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficFlowTile(response.getValue().toBytes());
    }

    static void validateGetTrafficFlowSegment(TrafficFlowSegmentData expected, TrafficFlowSegmentData actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getConfidence(), actual.getConfidence());
    }

    static void validateGetTrafficFlowSegmentWithResponse(TrafficFlowSegmentData expected, int expectedStatusCode,
                                                          Response<TrafficFlowSegmentData> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficFlowSegment(expected, response.getValue());
    }

    static void validateGetTrafficIncidentTile(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficIncidentTileWithResponse(int expectedStatusCode, Response<BinaryData> response)
        throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficIncidentTile(response.getValue().toBytes());
    }

    static void validateTrafficIncidentDetail(TrafficIncidentDetail expected, TrafficIncidentDetail actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getPointsOfInterest().size(), actual.getPointsOfInterest().size());
    }

    static void validateTrafficIncidentDetailWithResponse(TrafficIncidentDetail expected, int expectedStatusCode,
                                                          Response<TrafficIncidentDetail> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateTrafficIncidentDetail(expected, response.getValue());
    }

    static void validateTrafficIncidentViewport(TrafficIncidentViewport expected, TrafficIncidentViewport actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getViewportResponse().getCopyrightInformation(),
            actual.getViewportResponse().getCopyrightInformation());
        assertEquals(expected.getViewportResponse().getMaps(), actual.getViewportResponse().getMaps());
    }

    static void validateTrafficIncidentViewportWithResponse(TrafficIncidentViewport expected, int expectedStatusCode,
                                                            Response<TrafficIncidentViewport> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateTrafficIncidentViewport(expected, response.getValue());
    }
}
