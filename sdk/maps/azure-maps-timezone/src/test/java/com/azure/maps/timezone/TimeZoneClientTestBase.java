// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

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
import com.azure.maps.timezone.models.IanaId;
import com.azure.maps.timezone.models.TimeZoneIanaVersionResult;
import com.azure.maps.timezone.models.TimeZoneResult;
import com.azure.maps.timezone.models.TimeZoneWindows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TimeZoneClientTestBase extends TestProxyTestBase {
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

    TimeZoneClientBuilder getTimeZoneAsyncClientBuilder(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneClientBuilder builder = new TimeZoneClientBuilder()
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
                TimeZoneClientBuilder.MAPS_SUBSCRIPTION_KEY,
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

    static void validateGetTimezoneById(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByIdWithResponse(TimeZoneResult expected, int expectedStatusCode,
                                                    Response<TimeZoneResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTimezoneById(expected, response.getValue());
    }

    static void validateGetTimezoneByCoordinates(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByCoordinatesWithResponse(TimeZoneResult expected, int expectedStatusCode,
                                                             Response<TimeZoneResult> response) {
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

    static void validateGetWindowsTimezoneIdsWithResponse(List<TimeZoneWindows> expected, int expectedStatusCode,
                                                          Response<List<TimeZoneWindows>> response) {
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

    static void validateGetIanaTimezoneIdsWithResponse(List<IanaId> expected, int expectedStatusCode,
                                                       Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetIanaTimezoneIds(expected, response.getValue());
    }

    static void validateGetIanaVersion(TimeZoneIanaVersionResult actual, TimeZoneIanaVersionResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getVersion().charAt(0), actual.getVersion().charAt(0));
    }

    static void validateGetIanaVersionWithResponse(TimeZoneIanaVersionResult expected, int expectedStatusCode,
                                                   Response<TimeZoneIanaVersionResult> response) {
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

    static void validateConvertWindowsTimezoneToIanaWithResponse(List<IanaId> expected, int expectedStatusCode,
                                                                 Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateConvertWindowsTimezoneToIana(expected, response.getValue());
    }
}
