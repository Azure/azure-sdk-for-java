// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.maps.geolocation.models.IpAddressToLocationResult;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class GeoLocationClientTest extends GeoLocationClientTestBase {
    private GeoLocationClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private GeoLocationClient getGeoLocationClient(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) {
        return getGeoLocationAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get location 
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testGetLocation(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) throws IOException {
        client = getGeoLocationClient(httpClient, serviceVersion);
        IpAddressToLocationResult actualResult = client.getLocation("131.107.0.89");
        IpAddressToLocationResult expectedResult = TestUtils.getExpectedLocation();
        validateGetLocation(actualResult, expectedResult);
    }

    // Test get location with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testGetLocationWithResponse(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) throws IOException {
        client = getGeoLocationClient(httpClient, serviceVersion);
        validateGetLocationWithResponse(TestUtils.getExpectedLocation(), 200, client.getLocationWithResponse("131.107.0.89", null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testInvalidGetLocationWithResponse(HttpClient httpClient, GeoLocationServiceVersion serviceVersion) throws IOException {
        client = getGeoLocationClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getLocationWithResponse("0000000asdfsdf", null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
