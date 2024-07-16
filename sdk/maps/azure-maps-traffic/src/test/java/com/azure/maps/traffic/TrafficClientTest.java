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
import com.azure.maps.traffic.models.TrafficFlowSegmentData;
import com.azure.maps.traffic.models.TrafficFlowSegmentOptions;
import com.azure.maps.traffic.models.TrafficFlowSegmentStyle;
import com.azure.maps.traffic.models.TrafficFlowTileOptions;
import com.azure.maps.traffic.models.TrafficFlowTileStyle;
import com.azure.maps.traffic.models.TrafficIncidentDetail;
import com.azure.maps.traffic.models.TrafficIncidentDetailOptions;
import com.azure.maps.traffic.models.TrafficIncidentTileOptions;
import com.azure.maps.traffic.models.TrafficIncidentTileStyle;
import com.azure.maps.traffic.models.TrafficIncidentViewport;
import com.azure.maps.traffic.models.TrafficIncidentViewportOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrafficClientTest extends TrafficClientTestBase {
    private TrafficClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private TrafficClient getTrafficClient(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        return getTrafficAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get traffic flow tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficFlowTile(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions();
        trafficFlowTileOptions.setZoom(12)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(2044).setY(1360));
        validateGetTrafficFlowTile(client.getTrafficFlowTile(trafficFlowTileOptions).toBytes());
    }

    // Test get traffic flow tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficFlowTileWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions().setZoom(12)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(2044).setY(1360));
        validateGetTrafficFlowTileWithResponse(client.getTrafficFlowTileWithResponse(trafficFlowTileOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testInvalidGetTrafficFlowTileWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowTileOptions trafficFlowTileOptions = new TrafficFlowTileOptions().setZoom(-1000)
            .setFormat(TileFormat.PNG)
            .setTrafficFlowTileStyle(TrafficFlowTileStyle.RELATIVE_DELAY)
            .setTileIndex(new TileIndex().setX(2044).setY(1360));
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTrafficFlowTileWithResponse(trafficFlowTileOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get traffic flow segment
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficFlowSegment(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions = new TrafficFlowSegmentOptions();
        trafficFlowSegmentOptions.setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(10)
            .setCoordinates(new GeoPosition(4.84239, 52.41072))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        TrafficFlowSegmentData actualResult = client.getTrafficFlowSegment(trafficFlowSegmentOptions);
        TrafficFlowSegmentData expectedResult = TestUtils.getExpectedTrafficFlowSegment();
        validateGetTrafficFlowSegment(actualResult, expectedResult);
    }

    // Test get traffic flow segment with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficFlowSegmentWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions = new TrafficFlowSegmentOptions();
        trafficFlowSegmentOptions.setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(10)
            .setCoordinates(new GeoPosition(4.84239, 52.41072))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        validateGetTrafficFlowSegmentWithResponse(TestUtils.getExpectedTrafficFlowSegment(),
            client.getTrafficFlowSegmentWithResponse(trafficFlowSegmentOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testInvalidGetTrafficFlowSegmentWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficFlowSegmentOptions trafficFlowSegmentOptions
            = new TrafficFlowSegmentOptions().setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle.ABSOLUTE)
            .setOpenLr(false)
            .setZoom(-1000)
            .setCoordinates(new GeoPosition(45, 45))
            .setThickness(2)
            .setUnit(SpeedUnit.MPH);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTrafficFlowSegmentWithResponse(trafficFlowSegmentOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get traffic incident tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentTile(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentTileOptions trafficIncidentTileOptions = new TrafficIncidentTileOptions();
        trafficIncidentTileOptions.setFormat(TileFormat.PNG)
            .setTrafficIncidentTileStyle(TrafficIncidentTileStyle.NIGHT)
            .setTileIndex(new TileIndex().setX(175).setY(408))
            .setZoom(10);
        validateGetTrafficIncidentTile(client.getTrafficIncidentTile(trafficIncidentTileOptions).toBytes());
    }

    // Test get traffic incident tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentTileWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentTileOptions trafficIncidentTileOptions = new TrafficIncidentTileOptions();
        trafficIncidentTileOptions.setFormat(TileFormat.PNG)
            .setTrafficIncidentTileStyle(TrafficIncidentTileStyle.NIGHT)
            .setTileIndex(new TileIndex().setX(175).setY(408))
            .setZoom(10);
        validateGetTrafficIncidentTileWithResponse(
            client.getTrafficIncidentTileWithResponse(trafficIncidentTileOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testInvalidGetTrafficIncidentTileWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentTileOptions trafficIncidentTileOptions = new TrafficIncidentTileOptions().setFormat(
                TileFormat.PNG)
            .setTrafficIncidentTileStyle(TrafficIncidentTileStyle.NIGHT)
            .setTileIndex(new TileIndex().setX(175).setY(408))
            .setZoom(-1000);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTrafficIncidentTileWithResponse(trafficIncidentTileOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get traffic incident detail empty poi
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentDetail(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
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
        TrafficIncidentDetail actualResult = client.getTrafficIncidentDetail(trafficIncidentDetailOptions);
        TrafficIncidentDetail expectedResult = TestUtils.getExpectedTrafficIncidentDetail();
        validateTrafficIncidentDetail(actualResult, expectedResult);
    }

    // Test get traffic incident detail empty poi with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentDetailWithResponse(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
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
        validateTrafficIncidentDetailWithResponse(TestUtils.getExpectedTrafficIncidentDetail(),
            client.getTrafficIncidentDetailWithResponse(trafficIncidentDetailOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testInvalidGetTrafficIncidentDetailWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentDetailOptions trafficIncidentDetailOptions
            = new TrafficIncidentDetailOptions().setIncidentDetailStyle(IncidentDetailStyle.S3)
            .setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(-1000)
            .setTrafficmodelId("1335294634919")
            .setExpandCluster(false)
            .setOriginalPosition(false)
            .setIncidentGeometryType(IncidentGeometryType.ORIGINAL)
            .setLanguage("en")
            .setProjectionStandard(ProjectionStandard.EPSG900913);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTrafficIncidentDetailWithResponse(trafficIncidentDetailOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get traffic incident viewport
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentViewport(HttpClient httpClient, TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions = new TrafficIncidentViewportOptions();
        trafficIncidentViewportOptions.setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(2)
            .setOverviewZoom(2)
            .setCopyright(true);
        TrafficIncidentViewport actualResult = client.getTrafficIncidentViewport(trafficIncidentViewportOptions);
        TrafficIncidentViewport expectedResult = TestUtils.getExpectedTrafficIncidentViewport();
        validateTrafficIncidentViewport(actualResult, expectedResult);
    }

    // Test get traffic incident viewport with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testGetTrafficIncidentViewportWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions = new TrafficIncidentViewportOptions();
        trafficIncidentViewportOptions.setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(2)
            .setOverviewZoom(2)
            .setCopyright(true);
        validateTrafficIncidentViewportWithResponse(TestUtils.getExpectedTrafficIncidentViewport(),
            client.getTrafficIncidentViewportWithResponse(trafficIncidentViewportOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.traffic.TestUtils#getTestParameters")
    public void testInvalidGetTrafficIncidentViewportWithResponse(HttpClient httpClient,
        TrafficServiceVersion serviceVersion) {
        client = getTrafficClient(httpClient, serviceVersion);
        TrafficIncidentViewportOptions trafficIncidentViewportOptions
            = new TrafficIncidentViewportOptions().setBoundingBox(new GeoBoundingBox(45, 45, 45, 45))
            .setOverview(new GeoBoundingBox(45, 45, 45, 45))
            .setBoundingZoom(-1000)
            .setOverviewZoom(2)
            .setCopyright(true);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getTrafficIncidentViewportWithResponse(trafficIncidentViewportOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }
}
