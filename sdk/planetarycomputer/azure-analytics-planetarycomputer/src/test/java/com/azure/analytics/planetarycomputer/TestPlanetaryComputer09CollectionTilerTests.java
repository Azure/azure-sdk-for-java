// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.GeoJsonFeature;
import com.azure.analytics.planetarycomputer.models.FeatureType;
import com.azure.analytics.planetarycomputer.models.GeoJsonPolygon;
import com.azure.analytics.planetarycomputer.models.StacItemPointAsset;
import com.azure.analytics.planetarycomputer.models.TileSetList;
import com.azure.analytics.planetarycomputer.models.TileSetMetadata;
import com.azure.analytics.planetarycomputer.models.TilerAssetGeoJson;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Collection Tiler operations (Group 09).
 * Covers: getCollectionInfo, getCollectionPointAssets, getCollectionTile,
 *         getCollectionTileJson, getCollectionBboxCrop, getCollectionWmtsCapabilities,
 *         cropCollectionFeature, getCollectionTilesets, getCollectionAssetsForTile,
 *         getCollectionTilesetMetadata.
 * JS equivalent: 08_collectionTiler.spec.ts
 */
@Tag("CollectionTiler")
public class TestPlanetaryComputer09CollectionTilerTests extends PlanetaryComputerTestBase {

    /**
     * Test getting collection info.
     * JS equivalent: should get collection info
     */
    @Test
    @Tag("Info")
    public void test09_01_GetCollectionInfo() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionInfo for collection: " + collectionId);

        // Use protocol method to avoid deserialization issues with the response format
        RequestOptions requestOptions = new RequestOptions();
        com.azure.core.http.rest.Response<BinaryData> response
            = dataClient.getCollectionInfoWithResponse(collectionId, requestOptions);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        assertNotNull(response.getValue(), "Response body should not be null");
        System.out.println("Collection info retrieved successfully");
    }

    /**
     * Test getting collection point assets.
     * JS equivalent: should get collection point assets
     */
    @Test
    @Tag("Point")
    public void test09_02_GetCollectionPointAssets() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        double longitude = -84.386;
        double latitude = 33.676;

        System.out.println(String.format("Testing getCollectionPointAssets: lon=%f, lat=%f", longitude, latitude));

        List<StacItemPointAsset> assets = dataClient.getCollectionPointAssets(collectionId, longitude, latitude);

        assertNotNull(assets, "Point assets should not be null");
        System.out.println("Number of point assets: " + assets.size());
        System.out.println("Collection point assets retrieved successfully");
    }

    /**
     * Test getting a collection tile as PNG.
     * JS equivalent: should get collection tile as PNG
     */
    @Test
    @Tag("Tile")
    public void test09_03_GetCollectionTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionTileWithTmsByScaleAndFormat: z=14, x=4349, y=6564");

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData = dataClient
            .getCollectionTileWithTmsByScaleAndFormatWithResponse(collectionId, "WebMercatorQuad", 14.0, 4349.0, 6564.0,
                1.0, "png", requestOptions)
            .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        assertTrue(imageBytes.length > 0, "Tile image should not be empty");

        // Verify PNG magic bytes
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
        }

        System.out.println("Collection tile PNG validated successfully");
    }

    /**
     * Test getting collection tile JSON.
     * JS equivalent: should get collection tile JSON
     */
    @Test
    @Tag("TileJson")
    public void test09_04_GetCollectionTileJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionTileJson for collection: " + collectionId);

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        com.azure.core.http.rest.Response<BinaryData> response
            = dataClient.getCollectionTileJsonWithResponse(collectionId, requestOptions);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        System.out.println("Collection tile JSON retrieved successfully");
    }

    /**
     * Test getting collection bbox crop.
     * JS equivalent: should get collection bbox crop
     */
    @Test
    @Tag("BboxCrop")
    public void test09_05_GetCollectionBboxCrop() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        double minx = -84.39;
        double miny = 33.68;
        double maxx = -84.385;
        double maxy = 33.685;

        System.out.println(String.format("Testing getCollectionBboxCrop: [%f, %f, %f, %f]", minx, miny, maxx, maxy));

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData
            = dataClient.getCollectionBboxCropWithResponse(collectionId, minx, miny, maxx, maxy, "png", requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        assertTrue(imageBytes.length > 0, "Bbox crop image should not be empty");
        System.out.println("Collection bbox crop retrieved successfully");
    }

    /**
     * Test getting collection WMTS capabilities.
     * JS equivalent: should get collection WMTS capabilities
     */
    @Test
    @Tag("WMTS")
    public void test09_06_GetCollectionWmtsCapabilities() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionWmtsCapabilities for collection: " + collectionId);

        // Use protocol method to get XML response properly
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        byte[] xmlBytes
            = dataClient.getCollectionWmtsCapabilitiesWithResponse(collectionId, requestOptions).getValue().toBytes();

        String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);

        assertTrue(xmlBytes.length > 0, "WMTS XML should not be empty");
        assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");

        System.out.println("XML size: " + xmlBytes.length + " bytes");
        System.out.println("Collection WMTS capabilities retrieved successfully");
    }

    /**
     * Test cropping collection feature from GeoJSON.
     * JS equivalent: should crop collection feature from GeoJSON
     */
    @Test
    @Tag("Crop")
    public void test09_07_CropCollectionFeature() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // Small polygon in Atlanta area
        List<List<List<Double>>> coordinates
            = Arrays.asList(Arrays.asList(Arrays.asList(-84.39, 33.68), Arrays.asList(-84.385, 33.68),
                Arrays.asList(-84.385, 33.685), Arrays.asList(-84.39, 33.685), Arrays.asList(-84.39, 33.68)));
        GeoJsonPolygon geometry = new GeoJsonPolygon().setCoordinates(coordinates);
        GeoJsonFeature feature = new GeoJsonFeature(geometry, FeatureType.FEATURE).setProperties(new HashMap<>());

        System.out.println("Testing cropCollectionFeature for collection: " + collectionId);

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData
            = dataClient.cropCollectionFeatureWithResponse(collectionId, BinaryData.fromObject(feature), requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        assertTrue(imageBytes.length > 0, "Cropped image should not be empty");
        System.out.println("Collection feature crop retrieved successfully");
    }

    /**
     * Test listing collection tilesets.
     * JS equivalent: should list collection tilesets
     */
    @Test
    @Tag("Tilesets")
    public void test09_08_GetCollectionTilesets() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionTilesets for collection: " + collectionId);

        TileSetList tilesets = dataClient.getCollectionTilesets(collectionId);

        assertNotNull(tilesets, "Tilesets should not be null");
        assertNotNull(tilesets.getTilesets(), "Tilesets list should not be null");

        System.out.println("Number of tilesets: " + tilesets.getTilesets().size());
        System.out.println("Collection tilesets retrieved successfully");
    }

    /**
     * Test getting collection assets for a tile.
     * JS equivalent: should get collection assets for a tile
     */
    @Test
    @Tag("Assets")
    public void test09_09_GetCollectionAssetsForTile() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionAssetsForTile: z=13, x=2174, y=3282");

        List<TilerAssetGeoJson> assets
            = dataClient.getCollectionAssetsForTileWithTms(collectionId, "WebMercatorQuad", 13.0, 2174.0, 3282.0);

        assertNotNull(assets, "Tile assets should not be null");
        System.out.println("Number of tile assets: " + assets.size());
        System.out.println("Collection assets for tile retrieved successfully");
    }

    /**
     * Test getting collection tileset metadata.
     * JS equivalent: should get collection tileset metadata
     */
    @Test
    @Tag("TilesetMetadata")
    public void test09_10_GetCollectionTilesetMetadata() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionTilesetMetadata for WebMercatorQuad");

        TileSetMetadata metadata = dataClient.getCollectionTilesetMetadata(collectionId, "WebMercatorQuad");

        assertNotNull(metadata, "Tileset metadata should not be null");
        System.out.println("Collection tileset metadata retrieved successfully");
    }
}
