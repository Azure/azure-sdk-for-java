// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapStaticImageOptions;
import com.azure.maps.render.models.MapTileOptions;
import com.azure.maps.render.models.MapTileset;
import com.azure.maps.render.models.StaticMapLayer;
import com.azure.maps.render.models.TileIndex;
import com.azure.maps.render.models.TilesetId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class MapsRenderClientTest extends MapsRenderClientTestBase {
    private MapsRenderClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private MapsRenderClient getRenderClient(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        return getRenderAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get map tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTile(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(6));
        validateGetMapTile(client.getMapTile(mapTileOptions).toBytes());
    }

    // Test get map tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTileWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(6));
        validateGetMapTileWithResponse(client.getMapTileWithResponse(mapTileOptions, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapTileWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(-1000));
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getMapTileWithResponse(mapTileOptions, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get map tileset
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTileset(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileset result = client.getMapTileset(TilesetId.MICROSOFT_BASE_HYBRID);
        assertNotNull(result);
        assertEquals("microsoft.base.hybrid", result.getName());
        assertEquals("xyz", result.getScheme());
    }

    // Test get map tileset with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTilesetWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        Response<MapTileset> response = client.getMapTilesetWithResponse(TilesetId.MICROSOFT_BASE_HYBRID, null);
        assertEquals(200, response.getStatusCode());
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapTilesetWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getMapTilesetWithResponse(new TilesetId(), null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get map attribution
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapAttribution(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        MapAttribution actualResult = client.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds);
        MapAttribution expectedResult = TestUtils.getExpectedMapAttribution();
        validateGetMapAttribution(expectedResult, actualResult);
    }

    // Test get map attribution with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapAttributionWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162, 47.57949, -122.247157, 47.668372);
        validateGetMapAttributionWithResponse(TestUtils.getExpectedMapAttribution(),
            client.getMapAttributionWithResponse(TilesetId.MICROSOFT_BASE, 6, bounds, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapAttributionWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(47.579490, -122.414162, 47.668372, -122.247157);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getMapAttributionWithResponse(new TilesetId(), -100, bounds, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright caption
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightCaption(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        CopyrightCaption actualResult = client.getCopyrightCaption();
        CopyrightCaption expectedResult = TestUtils.getExpectedCopyrightCaption();
        validateGetCopyrightCaption(expectedResult, actualResult);
    }

    // Test get copyright caption with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightCaptionWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightCaptionWithResponse(TestUtils.getExpectedCopyrightCaption(),
            client.getCopyrightCaptionWithResponse(null));
    }

    // Test get mapstatic image
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapStaticImage(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bbox = new GeoBoundingBox(1.355233, 42.982261, 24.980233, 56.526017);
        MapStaticImageOptions mapStaticImageOptions = new MapStaticImageOptions().setStaticMapLayer(
                StaticMapLayer.BASIC)
            .setZoom(2)
            .setBoundingBox(bbox);
        validateGetMapStaticImage(client.getMapStaticImage(mapStaticImageOptions).toBytes());
    }

    // Test get mapstatic image with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapStaticImageWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bbox = new GeoBoundingBox(1.355233, 42.982261, 24.980233, 56.526017);
        MapStaticImageOptions mapStaticImageOptions = new MapStaticImageOptions().setStaticMapLayer(
                StaticMapLayer.BASIC)
            .setZoom(2)
            .setBoundingBox(bbox);
        validateGetMapStaticImageWithResponse(client.getMapStaticImageWithResponse(mapStaticImageOptions, null));
    }

    // Test get copyright from bounding box
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightFromBoundingBox(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        Copyright result = client.getCopyrightFromBoundingBox(boundingBox, true);
        assertNotNull(result);
        assertEquals("0.0.1", result.getFormatVersion());
        assertEquals(3, result.getGeneralCopyrights().size());
        assertEquals(3, result.getRegions().size());
    }

    // Test get copyright from bounding box with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        Response<Copyright> response = client.getCopyrightFromBoundingBoxWithResponse(boundingBox, true, null);
        assertEquals(200, response.getStatusCode());
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getCopyrightFromBoundingBoxWithResponse(new GeoBoundingBox(-100, -100, -100, -100), true,
                null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright for tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForTile(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        Copyright result = client.getCopyrightForTile(new TileIndex().setX(9).setY(22).setZ(6), true);
        validateGetCopyrightForTile(result);
    }

    // Test get copyright for tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForTileWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightForTileWithResponse(client.getCopyrightForTileWithResponse(new TileIndex().setX(9).setY(22).setZ(6), true, null));
    }

    // Case 2: Response 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetCopyrightForTileWithResponse(HttpClient httpClient,
        MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
            () -> client.getCopyrightForTileWithResponse(new TileIndex().setX(-1000), false, null));
        assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright for world
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForWorld(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        Copyright result = client.getCopyrightForWorld(true);
        validateGetCopyrightForWorld(result);
    }

    // Test get copyright for world with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForWorldWithResponse(HttpClient httpClient, MapsRenderServiceVersion serviceVersion) {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightForWorldWithResponse(client.getCopyrightForWorldWithResponse(true, null));
    }
}
