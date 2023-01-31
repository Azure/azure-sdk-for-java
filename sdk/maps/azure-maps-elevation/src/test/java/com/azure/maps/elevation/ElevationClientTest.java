// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.maps.elevation.models.ElevationResult;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ElevationClientTest extends ElevationClientTestBase {
    private ElevationClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private ElevationClient getElevationClient(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        return getElevationAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get data for points
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForPoints(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        ElevationResult actualResult = client.getDataForPoints(Arrays.asList(new GeoPosition(-121.66853362143818, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127)));
        ElevationResult expectedResult = TestUtils.getExpectedDataForPoints();
        validateGetDataForPoints(actualResult, expectedResult);
    }

    // Test get data for points with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForPointsWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        validateGetDataForPointsWithResponse(TestUtils.getExpectedDataForPoints(), 200, client.getDataForPointsWithResponse(Arrays.asList(new GeoPosition(-121.66853362143818, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127)), null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testInvalidGetDataForPointsWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getDataForPointsWithResponse(Arrays.asList(new GeoPosition(-100000000, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127)), null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get data for polyline
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForPolyline(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        ElevationResult actualResult = client.getDataForPolyline(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713), 
            new GeoPosition(-121.65853362143818, 46.85646479863713)), 5);
        ElevationResult expectedResult = TestUtils.getExpectedDataForPolyline();
        validateGetDataForPolyline(actualResult, expectedResult);
    }

    // Test get data for polyline with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForPolylineWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        validateGetDataForPolylineWithResponse(TestUtils.getExpectedDataForPolyline(), 200, client.getDataForPolylineWithResponse(Arrays.asList(
            new GeoPosition(-121.66853362143818, 46.84646479863713), 
            new GeoPosition(-121.65853362143818, 46.85646479863713)), 5, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testInvalidGetDataForPolylineWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getDataForPolylineWithResponse(Arrays.asList(
                new GeoPosition(-1000000000, 46.84646479863713), 
                new GeoPosition(-121.65853362143818, 46.85646479863713)), 5, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get data for bounding box
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForBoundingBox(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        ElevationResult actualResult = client.getDataForBoundingBox(new GeoBoundingBox(-121.668533621438, 46.8464647986371, 
            -121.658533621438, 46.8564647986371), 3, 3);
        ElevationResult expectedResult = TestUtils.getExpectedDataForBoundingBox();
        validateGetDataForBoundingBox(actualResult, expectedResult);
    }

    // Test get data for bounding box with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testGetDataForBoundingBoxWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        validateGetDataForBoundingBoxWithResponse(TestUtils.getExpectedDataForBoundingBox(), 200, client.getDataForBoundingBoxWithResponse(new GeoBoundingBox(-121.668533621438f, 46.8464647986371f, 
            -121.658533621438f, 46.8564647986371f), 3, 3, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testInvalidGetDataForBoundingBoxWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        client = getElevationClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getDataForBoundingBoxWithResponse(new GeoBoundingBox(-10000000f, 46.8464647986371f, 
            -121.658533621438f, 46.8564647986371f), 3, 3, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
