// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficIncidentViewport;

public class TrafficClientTestBase extends TestBase {
    static final String FAKE_API_KEY = "fakeApiKey";

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

    TrafficClientBuilder getTrafficAsyncClientBuilder(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficClientBuilder builder = new TrafficClientBuilder()
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

    static void validateGetTrafficFlowTile(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficFlowTileWithResponse(int expectedStatusCode, Response<BinaryData> response) throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficFlowTile(response.getValue().toBytes());
    }

    static void validateGetTrafficFlowSegment(TrafficFlowSegmentData expected, TrafficFlowSegmentData actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getConfidence(), actual.getConfidence());
    }

    static void validateGetTrafficFlowSegmentWithResponse(TrafficFlowSegmentData expected, int expectedStatusCode, Response<TrafficFlowSegmentData> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficFlowSegment(expected, response.getValue());
    }

    static void validateGetTrafficIncidentTile(byte[] actual) throws IOException {
        assertNotNull(actual);
        assertTrue(actual.length > 0);
    }

    static void validateGetTrafficIncidentTileWithResponse(int expectedStatusCode, Response<BinaryData> response) throws IOException {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTrafficIncidentTile(response.getValue().toBytes());
    }

    static void validateTrafficIncidentDetail(TrafficIncidentDetail expected, TrafficIncidentDetail actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getPointsOfInterest().size(), actual.getPointsOfInterest().size());
    }

    static void validateTrafficIncidentDetailWithResponse(TrafficIncidentDetail expected, int expectedStatusCode, Response<TrafficIncidentDetail> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateTrafficIncidentDetail(expected, response.getValue());
    }

    static void validateTrafficIncidentViewport(TrafficIncidentViewport expected, TrafficIncidentViewport actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getViewportResponse().getCopyrightInformation(), actual.getViewportResponse().getCopyrightInformation());
        assertEquals(expected.getViewportResponse().getMaps(), actual.getViewportResponse().getMaps());
    }

    static void validateTrafficIncidentViewportWithResponse(TrafficIncidentViewport expected, int expectedStatusCode, Response<TrafficIncidentViewport> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateTrafficIncidentViewport(expected, response.getValue());
    }
}
