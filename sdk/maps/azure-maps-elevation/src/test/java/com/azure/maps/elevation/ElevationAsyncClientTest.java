// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElevationAsyncClientTest extends ElevationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private ElevationAsyncClient getElevationAsyncClient(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        return getElevationAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get data for points
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForPoints(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPoints(Arrays.asList(new GeoPosition(-121.66853362143818, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127))))
            .assertNext(actualResults -> {
                try {
                    validateGetDataForPoints(TestUtils.getExpectedDataForPoints(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for points");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get data for points with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForPointsWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPointsWithResponse(Arrays.asList(new GeoPosition(-121.66853362143818, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127)), null))
            .assertNext(response -> {
                try {
                    validateGetDataForPointsWithResponse(TestUtils.getExpectedDataForPoints(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for points");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPointsWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPointsWithResponse(Arrays.asList(new GeoPosition(-100000000, 46.84646479863713), new GeoPosition(-121.68853362143818, 46.856464798637127)), null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get data for polyline
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForPolyline(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPolyline(Arrays.asList(
                new GeoPosition(-121.66853362143818, 46.84646479863713),
                new GeoPosition(-121.65853362143818, 46.85646479863713)), 5))
            .assertNext(actualResults -> {
                try {
                    validateGetDataForPolyline(TestUtils.getExpectedDataForPolyline(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for polyline");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get data for polyline with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForPolylineWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPolylineWithResponse(Arrays.asList(
                new GeoPosition(-121.66853362143818, 46.84646479863713),
                new GeoPosition(-121.65853362143818, 46.85646479863713)), 5, null))
            .assertNext(response -> {
                try {
                    validateGetDataForPolylineWithResponse(TestUtils.getExpectedDataForPolyline(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for polyline");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForPolylineWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForPolylineWithResponse(Arrays.asList(
                new GeoPosition(-1000000, 46.84646479863713),
                new GeoPosition(-121.65853362143818, 46.85646479863713)), 5, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test get data for bounding box
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForBoundingBox(HttpClient httpClient, ElevationServiceVersion serviceVersion) throws IOException {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForBoundingBox(new GeoBoundingBox(-121.668533621438f, 46.8464647986371f,
                -121.658533621438f, 46.8564647986371f), 3, 3))
            .assertNext(actualResults -> {
                try {
                    validateGetDataForBoundingBox(TestUtils.getExpectedDataForBoundingBox(), actualResults);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for bounding box");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get data for bounding box with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncGetDataForBoundingBoxWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForBoundingBoxWithResponse(new GeoBoundingBox(-121.668533621438f, 46.8464647986371f,
                -121.658533621438f, 46.8564647986371f), 3, 3, null))
            .assertNext(response -> {
                try {
                    validateGetDataForBoundingBoxWithResponse(TestUtils.getExpectedDataForBoundingBox(), 200, response);
                } catch (IOException e) {
                    Assertions.fail("Unable to get data for bounding box");
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.elevation.TestUtils#getTestParameters")
    public void testAsyncInvalidGetDataForBoundingBoxWithResponse(HttpClient httpClient, ElevationServiceVersion serviceVersion) {
        ElevationAsyncClient client = getElevationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDataForBoundingBoxWithResponse(new GeoBoundingBox(-121.668533621438f, 46.8464647986371f,
                -10000000f, 46.8564647986371f), 3, 3, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }
}
