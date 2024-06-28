// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

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
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
    TimeZoneClientBuilder getTimeZoneAsyncClientBuilder(HttpClient httpClient, TimeZoneServiceVersion serviceVersion) {
        TimeZoneClientBuilder builder = modifyBuilder(httpClient, new TimeZoneClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    TimeZoneClientBuilder modifyBuilder(HttpClient httpClient, TimeZoneClientBuilder builder) {
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
                .timezoneClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential())
                .timezoneClientId("timezoneClientId");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .timezoneClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetTimezoneById(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByIdWithResponse(TimeZoneResult expected, Response<TimeZoneResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTimezoneById(expected, response.getValue());
    }

    static void validateGetTimezoneByCoordinates(TimeZoneResult actual, TimeZoneResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getTimeZones().size(), actual.getTimeZones().size());
    }

    static void validateGetTimezoneByCoordinatesWithResponse(TimeZoneResult expected,
        Response<TimeZoneResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetTimezoneByCoordinates(expected, response.getValue());
    }

    static void validateGetWindowsTimezoneIds(List<TimeZoneWindows> actual, List<TimeZoneWindows> expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.size(), actual.size());

        if (!actual.isEmpty()) {
            assertEquals(expected.get(0).getIanaIds().size(), actual.get(0).getIanaIds().size());
            assertEquals(expected.get(0).getTerritory(), actual.get(0).getTerritory());
        }
    }

    static void validateGetWindowsTimezoneIdsWithResponse(List<TimeZoneWindows> expected,
        Response<List<TimeZoneWindows>> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetWindowsTimezoneIds(expected, response.getValue());
    }

    static void validateGetIanaTimezoneIds(List<IanaId> actual, List<IanaId> expected) {
        assertNotNull(actual);
        assertNotNull(expected);

        if (!actual.isEmpty()) {
            assertEquals(expected.get(0).getClass(), actual.get(0).getClass());
        }
    }

    static void validateGetIanaTimezoneIdsWithResponse(List<IanaId> expected, Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetIanaTimezoneIds(expected, response.getValue());
    }

    static void validateGetIanaVersion(TimeZoneIanaVersionResult actual, TimeZoneIanaVersionResult expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getVersion().charAt(0), actual.getVersion().charAt(0));
    }

    static void validateGetIanaVersionWithResponse(TimeZoneIanaVersionResult expected,
        Response<TimeZoneIanaVersionResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetIanaVersion(expected, response.getValue());
    }

    static void validateConvertWindowsTimezoneToIana(List<IanaId> actual, List<IanaId> expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.size(), actual.size());

        if (!actual.isEmpty()) {
            assertEquals(expected.get(0).getAlias(), actual.get(0).getAlias());
            assertEquals(expected.get(0).getId(), actual.get(0).getId());
        }
    }

    static void validateConvertWindowsTimezoneToIanaWithResponse(List<IanaId> expected,
        Response<List<IanaId>> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateConvertWindowsTimezoneToIana(expected, response.getValue());
    }
}
