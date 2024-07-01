// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

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
import com.azure.maps.geolocation.models.IpAddressToLocationResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeolocationClientTestBase extends TestProxyTestBase {
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    GeolocationClientBuilder getGeoLocationAsyncClientBuilder(HttpClient httpClient,
        GeolocationServiceVersion serviceVersion) {
        GeolocationClientBuilder builder = modifyBuilder(httpClient, new GeolocationClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    GeolocationClientBuilder modifyBuilder(HttpClient httpClient, GeolocationClientBuilder builder) {
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
                .clientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential())
                .clientId("testGeoClient");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .clientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetLocation(IpAddressToLocationResult expected, IpAddressToLocationResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getCountryRegion().getIsoCode(), actual.getCountryRegion().getIsoCode());
        assertEquals(expected.getIpAddress(), actual.getIpAddress());
    }

    static void validateGetLocationWithResponse(IpAddressToLocationResult expected,
        Response<IpAddressToLocationResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetLocation(expected, response.getValue());
    }
}
