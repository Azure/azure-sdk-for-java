// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GeolocationAsyncClientTest extends GeolocationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private GeolocationAsyncClient getGeoLocationAsyncClient(HttpClient httpClient,
        GeolocationServiceVersion serviceVersion) {
        return getGeoLocationAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get location
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncGetLocation(HttpClient httpClient, GeolocationServiceVersion serviceVersion) {
        GeolocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocation("131.107.0.89"))
            .assertNext(actualResults -> validateGetLocation(TestUtils.getExpectedLocation(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get location with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncGetLocationWithResponse(HttpClient httpClient, GeolocationServiceVersion serviceVersion) {
        GeolocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocationWithResponse("131.107.0.89"))
            .assertNext(response -> validateGetLocationWithResponse(TestUtils.getExpectedLocation(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPointsWithResponse(HttpClient httpClient,
        GeolocationServiceVersion serviceVersion) {
        GeolocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocationWithResponse("0000000adfasfwe")).expectErrorSatisfies(ex -> {
            HttpResponseException httpResponseException = assertInstanceOf(HttpResponseException.class, ex);
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }).verify(DEFAULT_TIMEOUT);
    }
}
