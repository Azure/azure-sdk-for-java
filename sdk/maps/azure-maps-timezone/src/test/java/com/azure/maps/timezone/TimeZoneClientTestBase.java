// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

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
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimeZoneIanaVersionResult;
import com.azure.maps.timezone.models.TimeZoneResult;
import com.azure.maps.timezone.models.TimeZoneWindows;

public class TimeZoneClientTestBase extends TestBase {
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

    TimeZoneClientBuilder getTimeZoneAsyncClientBuilder(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneClientBuilder builder = new TimeZoneClientBuilder()
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

    static void validateGetTimezoneById(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByIdWithResponse(TimeZoneResult expected, int expectedStatusCode, Response<TimeZoneResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTimezoneById(expected, response.getValue());
    }

    static void validateGetTimezoneByCoordinates(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByCoordinatesWithResponse(TimeZoneResult expected, int expectedStatusCode, Response<TimeZoneResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTimezoneByCoordinates(expected, response.getValue());
    }

    static void validateGetWindowsTimezoneIds(List<TimeZoneWindows> actual, List<TimeZoneWindows> expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.size(), actual.size());
        if (actual.size() > 0) {
            assertEquals(expected.get(0).getIanaIds().size(), actual.get(0).getIanaIds().size());
            assertEquals(expected.get(0).getTerritory(), actual.get(0).getTerritory());
        }
    }

    static void validateGetWindowsTimezoneIdsWithResponse(List<TimeZoneWindows> expected, int expectedStatusCode, Response<List<TimeZoneWindows>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetWindowsTimezoneIds(expected, response.getValue());
    }

    static void validateGetIanaTimezoneIds(List<IanaId> actual, List<IanaId> expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        if (actual.size() > 0) {
            assertEquals(expected.get(0).getClass(), actual.get(0).getClass());
        }
    }

    static void validateGetIanaTimezoneIdsWithResponse(List<IanaId> expected, int expectedStatusCode, Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetIanaTimezoneIds(expected, response.getValue());
    }

    static void validateGetIanaVersion(TimeZoneIanaVersionResult actual, TimeZoneIanaVersionResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getVersion().charAt(0), actual.getVersion().charAt(0));
    }

    static void validateGetIanaVersionWithResponse(TimeZoneIanaVersionResult expected, int expectedStatusCode, Response<TimeZoneIanaVersionResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetIanaVersion(expected, response.getValue());
    }

    static void validateConvertWindowsTimezoneToIana(List<IanaId> actual, List<IanaId> expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.size(), actual.size());
        if (actual.size() > 0) {
            assertEquals(expected.get(0).getAlias(), actual.get(0).getAlias());
            assertEquals(expected.get(0).getId(), actual.get(0).getId());
        }
    }

    static void validateConvertWindowsTimezoneToIanaWithResponse(List<IanaId> expected, int expectedStatusCode, Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateConvertWindowsTimezoneToIana(expected, response.getValue());
    }
}
