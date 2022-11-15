// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.elevation.models.ElevationResult;

public class ElevationClientTestBase extends TestBase {
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

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

    ElevationClientBuilder getElevationAsyncClientBuilder(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationClientBuilder builder = new ElevationClientBuilder()
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

    static void validateGetDataForPoints(ElevationResult expected, ElevationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getElevations().size(), actual.getElevations().size());
    }

    static void validateGetDataForPointsWithResponse(ElevationResult expected, int expectedStatusCode, Response<ElevationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDataForPoints(expected, response.getValue());
    }

    static void validatePostDataForPoints(ElevationResult expected, ElevationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getElevations().size(), actual.getElevations().size());
    }

    static void validatePostDataForPointsWithResponse(ElevationResult expected, int expectedStatusCode, Response<ElevationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDataForPoints(expected, response.getValue());
    }

    static void validateGetDataForPolyline(ElevationResult expected, ElevationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getElevations().size(), actual.getElevations().size());
    }

    static void validateGetDataForPolylineWithResponse(ElevationResult expected, int expectedStatusCode, Response<ElevationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDataForPolyline(expected, response.getValue());
    }

    static void validatePostDataForPolyline(ElevationResult expected, ElevationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getElevations().size(), actual.getElevations().size());
    }

    static void validatePostDataForPolylineWithResponse(ElevationResult expected, int expectedStatusCode, Response<ElevationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validatePostDataForPolyline(expected, response.getValue());
    }

    static void validateGetDataForBoundingBox(ElevationResult expected, ElevationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getElevations().size(), actual.getElevations().size());
    }

    static void validateGetDataForBoundingBoxWithResponse(ElevationResult expected, int expectedStatusCode, Response<ElevationResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDataForBoundingBox(expected, response.getValue());
    }
}
