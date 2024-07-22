// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.maps.traffic.models.IncidentDetailStyle;
import com.azure.maps.traffic.models.IncidentGeometryType;
import com.azure.maps.traffic.models.ProjectionStandard;
import com.azure.maps.traffic.models.SpeedUnit;
import com.azure.maps.traffic.models.TileFormat;
import com.azure.maps.traffic.models.TileIndex;
import com.azure.maps.traffic.models.TrafficFlowSegmentOptions;
import com.azure.maps.traffic.models.TrafficFlowSegmentStyle;
import com.azure.maps.traffic.models.TrafficFlowTileOptions;
import com.azure.maps.traffic.models.TrafficFlowTileStyle;
import com.azure.maps.traffic.models.TrafficIncidentDetailOptions;
import com.azure.maps.traffic.models.TrafficIncidentViewportOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrafficAsyncClientTest extends TrafficClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private TrafficAsyncClient getTrafficAsyncClient(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        return getTrafficAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get traffic flow tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficFlowTile(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions().setZoom(12)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(50).setY(50));
        StepVerifier.create(client.getTrafficFlowTile(trafficFlowTileOptions))
            .assertNext(actualResults -> validateGetTrafficFlowTile(actualResults.toBytes()))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic flow tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficFlowTileWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions().setZoom(12)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(50).setY(50));
        StepVerifier.create(client.getTrafficFlowTileWithResponse(trafficFlowTileOptions))
            .assertNext(TrafficClientTestBase::validateGetTrafficFlowTileWithResponse)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTrafficFlowTileWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions().setZoom(-1000)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(2044).setY(1360));
        StepVerifier.create(client.getTrafficFlowTileWithResponse(trafficFlowTileOptions)).expectErrorSatisfies(ex -> {
            final HttpResponseException httpResponseException = (HttpResponseException) ex;
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }).verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic flow segment
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficFlowSegment(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions
            = new TrafficFlowSegmentOptions().setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(10)
            .setCoordinates(new GeoPosition(4.84239, 52.41072))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        StepVerifier.create(client.getTrafficFlowSegment(trafficFlowSegmentOptions))
            .assertNext(actualResults -> validateGetTrafficFlowSegment(TestUtils.getExpectedTrafficFlowSegment(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic flow segment with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficFlowSegmentWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions
            = new TrafficFlowSegmentOptions().setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(10)
            .setCoordinates(new GeoPosition(4.84239, 52.41072))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        StepVerifier.create(client.getTrafficFlowSegmentWithResponse(trafficFlowSegmentOptions))
            .assertNext(response -> validateGetTrafficFlowSegmentWithResponse(TestUtils.getExpectedTrafficFlowSegment(),
                response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTrafficFlowSegmentWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions
            = new TrafficFlowSegmentOptions().setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(-1000)
            .setCoordinates(new GeoPosition(45, 45))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        StepVerifier.create(client.getTrafficFlowSegmentWithResponse(trafficFlowSegmentOptions))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic incident detail empty poi
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficIncidentDetail(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentDetailOptions trafficIncidentDetailOptions = new TrafficIncidentDetailOptions();
        trafficIncidentDetailOptions.setIncidentDetailStyle(IncidentDetailStyle.S3)
            .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(11)
            .setTrafficmodelId("1335294634919")
            .setExpandCluster(false)
            .setOriginalPosition(false)
            .setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
            .setLanguage("en")
            .setProjectionStandard(ProjectionStandard.EPSG900913);
        StepVerifier.create(client.getTrafficIncidentDetail(trafficIncidentDetailOptions))
            .assertNext(actualResults -> validateTrafficIncidentDetail(TestUtils.getExpectedTrafficIncidentDetail(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic incident detail empty poi with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficIncidentDetailWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentDetailOptions trafficIncidentDetailOptions = new TrafficIncidentDetailOptions();
        trafficIncidentDetailOptions.setIncidentDetailStyle(IncidentDetailStyle.S3)
            .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(11)
            .setTrafficmodelId("1335294634919")
            .setExpandCluster(false)
            .setOriginalPosition(false)
            .setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
            .setLanguage("en")
            .setProjectionStandard(ProjectionStandard.EPSG900913);
        StepVerifier.create(client.getTrafficIncidentDetailWithResponse(trafficIncidentDetailOptions))
            .assertNext(
                response -> validateTrafficIncidentDetailWithResponse(TestUtils.getExpectedTrafficIncidentDetail(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTrafficIncidentDetailWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentDetailOptions trafficIncidentDetailOptions = new TrafficIncidentDetailOptions();
        trafficIncidentDetailOptions.setIncidentDetailStyle(IncidentDetailStyle.S3)
            .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(-1000)
            .setTrafficmodelId("1335294634919")
            .setExpandCluster(false)
            .setOriginalPosition(false)
            .setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
            .setLanguage("en")
            .setProjectionStandard(ProjectionStandard.EPSG900913);
        StepVerifier.create(client.getTrafficIncidentDetailWithResponse(trafficIncidentDetailOptions))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic incident viewport
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficIncidentViewport(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions
            = new TrafficIncidentViewportOptions().setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(2)
            .setOverviewZoom(2)
            .setCopyright(true);
        StepVerifier.create(client.getTrafficIncidentViewport(trafficIncidentViewportOptions))
            .assertNext(actualResults -> validateTrafficIncidentViewport(TestUtils.getExpectedTrafficIncidentViewport(),
                actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get traffic incident viewport with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncGetTrafficIncidentViewportWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions
            = new TrafficIncidentViewportOptions().setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(2)
            .setOverviewZoom(2)
            .setCopyright(true);
        StepVerifier.create(client.getTrafficIncidentViewportWithResponse(trafficIncidentViewportOptions))
            .assertNext(
                response -> validateTrafficIncidentViewportWithResponse(TestUtils.getExpectedTrafficIncidentViewport(),
                    response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testAsyncInvalidGetTrafficIncidentViewportWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        TrafficAsyncClient client = getTrafficAsyncClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions
            = new TrafficIncidentViewportOptions().setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(-1000)
            .setOverviewZoom(2)
            .setCopyright(true);
        StepVerifier.create(client.getTrafficIncidentViewportWithResponse(trafficIncidentViewportOptions))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }
}
