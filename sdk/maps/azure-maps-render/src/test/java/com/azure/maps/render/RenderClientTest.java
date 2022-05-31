package com.azure.maps.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoBoundingBox;
import com.azure.maps.render.models.Copyright;
import com.azure.maps.render.models.CopyrightCaption;
import com.azure.maps.render.models.MapAttribution;
import com.azure.maps.render.models.MapImageStyle;
import com.azure.maps.render.models.MapStaticImageOptions;
import com.azure.maps.render.models.MapTileOptions;
import com.azure.maps.render.models.MapTileset;
import com.azure.maps.render.models.RasterTileFormat;
import com.azure.maps.render.models.StaticMapLayer;
import com.azure.maps.render.models.TileIndex;
import com.azure.maps.render.models.TilesetId;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RenderClientTest extends RenderClientTestBase {
    private RenderClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private RenderClient getRenderClient(HttpClient httpClient, RenderServiceVersion serviceVersion) {
        return getRenderAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get map tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTile(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(6));
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            client.getMapTile(stream, mapTileOptions);
            validateGetMapTile(stream.toByteArray());
        }
    }

    // Test get map tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTileWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(6));
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            validateGetMapTileWithResponse(200, client.getMapTileWithResponse(stream, mapTileOptions, null), stream);
        }
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapTileWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        MapTileOptions mapTileOptions = new MapTileOptions();
        mapTileOptions.setTilesetId(TilesetId.MICROSOFT_BASE_ROAD);
        mapTileOptions.setTileIndex(new TileIndex().setX(10).setY(22).setZ(-1000));
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getMapTileWithResponse(stream, mapTileOptions, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }
    }

    // Test get map tileset
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTileset(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        new TilesetId();
        MapTileset actualResult = client.getMapTileset(TilesetId.MICROSOFT_BASE);
        MapTileset expectedResult = TestUtils.getExpectedMapTileset();
        validateGetMapTileset(expectedResult, actualResult);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
    }

    // Test get map tileset with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapTilesetWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        new TilesetId();
        validateGetMapTilesetWithResponse(TestUtils.getExpectedMapTileset(), 200, client.getMapTilesetWithResponse(TilesetId.MICROSOFT_BASE, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapTilesetWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getMapTilesetWithResponse(new TilesetId(), null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get map attribution
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapAttribution(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162,47.57949,-122.247157,47.668372);
        new TilesetId();
        MapAttribution actualResult = client.getMapAttribution(TilesetId.MICROSOFT_BASE, 6, bounds);
        MapAttribution expectedResult = TestUtils.getExpectedMapAttribution();
        validateGetMapAttribution(expectedResult, actualResult);
    }

    // Test get map attribution with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapAttributionWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds = new GeoBoundingBox(-122.414162,47.57949,-122.247157,47.668372);
        new TilesetId();
        validateGetMapAttributionWithResponse(TestUtils.getExpectedMapAttribution(), 200, client.getMapAttributionWithResponse(TilesetId.MICROSOFT_BASE, 6, bounds, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetMapAttributionWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bounds =  new GeoBoundingBox(47.579490,-122.414162,47.668372,-122.247157);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getMapAttributionWithResponse(new TilesetId(), -100, bounds, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright caption
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightCaption(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        CopyrightCaption actualResult = client.getCopyrightCaption();
        CopyrightCaption expectedResult = TestUtils.getExpectedCopyrightCaption();
        validateGetCopyrightCaption(expectedResult, actualResult);
    }

    // Test get copyright caption with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightCaptionWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightCaptionWithResponse(TestUtils.getExpectedCopyrightCaption(), 200, client.getCopyrightCaptionWithResponse(null));
    }

    // Test get mapstatic image
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapStaticImage(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bbox = new GeoBoundingBox(1.355233, 42.982261, 24.980233, 56.526017);
        new StaticMapLayer();
        new RasterTileFormat();
        MapStaticImageOptions mapStaticImageOptions = new MapStaticImageOptions().setStaticMapLayer(StaticMapLayer.BASIC)
            .setMapImageStyle(MapImageStyle.MAIN).setZoom(2)
            .setBoundingBox(bbox).setRasterTileFormat(RasterTileFormat.PNG);
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            client.getMapStaticImage(stream, mapStaticImageOptions);
            validateGetMapStaticImage(stream.toByteArray());
        }
    }

    // Test get mapstatic image with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetMapStaticImageWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox bbox = new GeoBoundingBox(1.355233, 42.982261, 24.980233, 56.526017);
        new StaticMapLayer();
        new RasterTileFormat();
        MapStaticImageOptions mapStaticImageOptions = new MapStaticImageOptions().setStaticMapLayer(StaticMapLayer.BASIC)
        .setMapImageStyle(MapImageStyle.MAIN).setZoom(2)
        .setBoundingBox(bbox).setRasterTileFormat(RasterTileFormat.PNG);
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            validateGetMapStaticImageWithResponse(200, client.getMapStaticImageWithResponse(stream, mapStaticImageOptions, null), stream);
        }
    }

    // Test get copyright from bounding box
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightFromBoundingBox(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        Copyright actualResult = client.getCopyrightFromBoundingBox(boundingBox, true);
        Copyright expectedResult = TestUtils.getExpectedCopyrightFromBoundingBox();
        validateGetCopyrightCaptionFromBoundingBox(expectedResult, actualResult);
    }

    // Test get copyright from bounding box with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        GeoBoundingBox boundingBox = new GeoBoundingBox(52.41064, 4.84228, 52.41072, 4.84239);
        validateGetCopyrightCaptionFromBoundingBoxWithResponse(TestUtils.getExpectedCopyrightFromBoundingBox(), 200, client.getCopyrightFromBoundingBoxWithResponse(boundingBox, true, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetCopyrightFromBoundingBoxWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getCopyrightFromBoundingBoxWithResponse(new GeoBoundingBox(-100, -100, -100, -100), true, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright for tile
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForTile(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        Copyright actualResult = client.getCopyrightForTile(new TileIndex().setX(9).setY(22).setZ(6), true);
        Copyright expectedResult = TestUtils.getExpectedCopyrightForTile();
        validateGetCopyrightForTile(expectedResult, actualResult);
    }

    // Test get copyright for tile with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForTileWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightForTileWithResponse(TestUtils.getExpectedCopyrightForTile(), 200, client.getCopyrightForTileWithResponse(new TileIndex().setX(9).setY(22).setZ(6), true, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testInvalidGetCopyrightForTileWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getCopyrightForTileWithResponse(new TileIndex().setX(-1000), false, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get copyright for world
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForWorld(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        Copyright actualResult = client.getCopyrightForWorld(true);
        Copyright expectedResult = TestUtils.getExpectedCopyrightForWorld();
        validateGetCopyrightForWorld(expectedResult, actualResult);
    }

    // Test get copyright for world with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.render.TestUtils#getTestParameters")
    public void testGetCopyrightForWorldWithResponse(HttpClient httpClient, RenderServiceVersion serviceVersion) throws IOException {
        client = getRenderClient(httpClient, serviceVersion);
        validateGetCopyrightForWorldWithResponse(TestUtils.getExpectedCopyrightForWorld(), 200, client.getCopyrightForWorldWithResponse(true, null));
    }
}