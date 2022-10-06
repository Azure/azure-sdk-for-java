// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Duration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

public class GeoLocationAsyncClientTest extends GeoLocationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private GeoLocationAsyncClient getGeoLocationAsyncClient(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) {
        return getGeoLocationAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get location
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncGetLocation(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) throws IOException {
        GeoLocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocation("131.107.0.89"))
            .assertNext(actualResults -> {
                try {
                    validateGetLocation(TestUtils.getExpectedLocation(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get location");
                }
            }).verifyComplete();
    }

    // Test async get location with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncGetLocationWithResponse(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) {
        GeoLocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocationWithResponse("131.107.0.89"))
            .assertNext(response -> {
                try {
                    validateGetLocationWithResponse(TestUtils.getExpectedLocation(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get location");
                }
            }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPointsWithResponse(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) {
        GeoLocationAsyncClient client = getGeoLocationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getLocationWithResponse("0000000adfasfwe"))
            .verifyErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            });
    }
}
