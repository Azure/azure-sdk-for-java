// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import com.azure.core.http.HttpClient;
import com.azure.maps.geolocation.models.IpAddressToLocationResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GeolocationClientTest extends GeolocationClientTestBase {
    private GeolocationClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private GeolocationClient getGeoLocationClient(HttpClient httpClient, GeolocationServiceVersion serviceVersion) {
        return getGeoLocationAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get location
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testGetLocation(HttpClient httpClient, GeolocationServiceVersion serviceVersion) {
        client = getGeoLocationClient(httpClient, serviceVersion);
        IpAddressToLocationResult actualResult = null;
        try {
            actualResult = client.getLocation(InetAddress.getByName("131.107.0.89"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        IpAddressToLocationResult expectedResult = TestUtils.getExpectedLocation();
        validateGetLocation(actualResult, expectedResult);
    }

    // Test get location with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.geolocation.TestUtils#getTestParameters")
    public void testGetLocationWithResponse(HttpClient httpClient, GeolocationServiceVersion serviceVersion) {
        client = getGeoLocationClient(httpClient, serviceVersion);
        try {
            validateGetLocationWithResponse(TestUtils.getExpectedLocation(),
                client.getLocationWithResponse(InetAddress.getByName("131.107.0.89"), null));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
