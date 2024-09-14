// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.credential.AzureKeyCredential;
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
import com.azure.core.util.Configuration;
import com.azure.maps.search.implementation.models.GeoJsonGeometryCollection;
import com.azure.maps.search.models.Boundary;
import com.azure.maps.search.models.GeocodingBatchResponse;
import com.azure.maps.search.models.GeocodingResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MapsSearchClientTestBase extends TestProxyTestBase {
    Duration durationTestMode;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
    }

    MapsSearchClientBuilder getMapsSearchAsyncClientBuilder(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        MapsSearchClientBuilder builder = modifyBuilder(httpClient, new MapsSearchClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }

        return builder;
    }

    MapsSearchClientBuilder modifyBuilder(HttpClient httpClient, MapsSearchClientBuilder builder) {
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
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY")));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new AzureKeyCredential("REDACTED"));
        } else {
            builder.credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY")));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    static void validateGetPolygons(Boundary response) {
        assertNotNull(response);
        assertEquals(response.getType().toString(), "Feature");
        assertEquals(((GeoJsonGeometryCollection) response.getGeometry()).getGeometries().size(), 1);
    }

    static void validateGetGeocode(Response<GeocodingResponse> response) {
        assertEquals(200, response.getStatusCode());
    }

    static void validateGetGeocodeBatch(GeocodingBatchResponse response) {
        assertEquals(2, response.getSummary().getSuccessfulRequests());
        assertEquals(2, response.getSummary().getTotalRequests());
    }

    static void validateGetReverseGeocoding(Response<GeocodingResponse> response) {
        assertEquals(200, response.getStatusCode());
    }

    static void validateGetReverseGeocodingBatch(GeocodingBatchResponse response) {
        assertEquals(2, response.getSummary().getTotalRequests());
        assertEquals(2, response.getSummary().getSuccessfulRequests());
    }
}
