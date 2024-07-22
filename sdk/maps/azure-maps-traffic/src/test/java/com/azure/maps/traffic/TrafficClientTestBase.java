// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficIncidentViewport;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrafficClientTestBase extends TestProxyTestBase {
    TrafficClientBuilder getTrafficAsyncClientBuilder(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficClientBuilder builder = modifyBuilder(httpClient, new TrafficClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    TrafficClientBuilder modifyBuilder(HttpClient httpClient, TrafficClientBuilder builder) {
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
                .trafficClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential())
                .trafficClientId("trafficClientId");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .trafficClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetTrafficFlowTile(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficFlowTileWithResponse(Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTrafficFlowTile(response.getValue().toBytes());
    }

    static void validateGetTrafficFlowSegment(TrafficFlowSegmentData expected, TrafficFlowSegmentData actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getConfidence(), actual.getConfidence());
    }

    static void validateGetTrafficFlowSegmentWithResponse(TrafficFlowSegmentData expected,
        Response<TrafficFlowSegmentData> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTrafficFlowSegment(expected, response.getValue());
    }

    static void validateGetTrafficIncidentTile(byte[] actual) {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficIncidentTileWithResponse(Response<BinaryData> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTrafficIncidentTile(response.getValue().toBytes());
    }

    static void validateTrafficIncidentDetail(TrafficIncidentDetail expected, TrafficIncidentDetail actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getPointsOfInterest().size(), actual.getPointsOfInterest().size());
    }

    static void validateTrafficIncidentDetailWithResponse(TrafficIncidentDetail expected,
        Response<TrafficIncidentDetail> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateTrafficIncidentDetail(expected, response.getValue());
    }

    static void validateTrafficIncidentViewport(TrafficIncidentViewport expected, TrafficIncidentViewport actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getViewportResponse().getCopyrightInformation(),
            actual.getViewportResponse().getCopyrightInformation());
        assertEquals(expected.getViewportResponse().getMaps(), actual.getViewportResponse().getMaps());
    }

    static void validateTrafficIncidentViewportWithResponse(TrafficIncidentViewport expected,
        Response<TrafficIncidentViewport> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateTrafficIncidentViewport(expected, response.getValue());
    }
}
