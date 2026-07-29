// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.analytics.planetarycomputer.models.GeoJsonFeature;
import com.azure.analytics.planetarycomputer.models.FeatureType;
import com.azure.analytics.planetarycomputer.models.GeoJsonPolygon;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Collection Tiler operations (Group 09).
 * Mirrors sync tests in TestPlanetaryComputer09CollectionTilerTests.
 * Covers: getCollectionInfo, getCollectionPointAssets, getCollectionTile,
 *         getCollectionTileJson, getCollectionBboxCrop, getCollectionWmtsCapabilities,
 *         cropCollectionFeature, getCollectionTilesets, getCollectionAssetsForTile,
 *         getCollectionTilesetMetadata.
 */
@Tag("CollectionTiler")
@Tag("Async")
public class TestPlanetaryComputer09AsyncCollectionTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("Info")
    public void test09_01_GetCollectionInfoAsync() {
        String collectionId = testEnvironment.getCollectionId();

        RequestOptions requestOptions = new RequestOptions();
        StepVerifier.create(dataAsyncClient.getCollectionInfoWithResponse(collectionId, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                assertNotNull(response.getValue(), "Response body should not be null");
            })
            .verifyComplete();
    }

    @Test
    @Tag("Point")
    public void test09_02_GetCollectionPointAssetsAsync() {
        String collectionId = testEnvironment.getCollectionId();
        double longitude = -84.386;
        double latitude = 33.676;

        StepVerifier.create(dataAsyncClient.getCollectionPointAssets(collectionId, longitude, latitude))
            .assertNext(assets -> {
                assertNotNull(assets, "Point assets should not be null");
                System.out.println("Async: Number of point assets: " + assets.size());
            })
            .verifyComplete();
    }

    @Test
    @Tag("Tile")
    public void test09_03_GetCollectionTileAsync() {
        String collectionId = testEnvironment.getCollectionId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient
            .getCollectionTileWithTmsByScaleAndFormatWithResponse(collectionId, "WebMercatorQuad", 14.0, 4349.0, 6564.0,
                1.0, "png", requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                assertTrue(imageBytes.length > 0, "Tile image should not be empty");

                // Verify PNG magic bytes
                byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
                for (int i = 0; i < pngMagic.length; i++) {
                    assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
                }
            }).verifyComplete();
    }

    @Test
    @Tag("TileJson")
    public void test09_04_GetCollectionTileJsonAsync() {
        String collectionId = testEnvironment.getCollectionId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);

        StepVerifier.create(dataAsyncClient.getCollectionTileJsonWithResponse(collectionId, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
            })
            .verifyComplete();
    }

    @Test
    @Tag("BboxCrop")
    public void test09_05_GetCollectionBboxCropAsync() {
        String collectionId = testEnvironment.getCollectionId();

        double minx = -84.39;
        double miny = 33.68;
        double maxx = -84.385;
        double maxy = 33.685;

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient
            .getCollectionBboxCropWithResponse(collectionId, minx, miny, maxx, maxy, "png", requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                assertTrue(imageBytes.length > 0, "Bbox crop image should not be empty");
            }).verifyComplete();
    }

    @Test
    @Tag("WMTS")
    public void test09_06_GetCollectionWmtsCapabilitiesAsync() {
        String collectionId = testEnvironment.getCollectionId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);

        StepVerifier.create(dataAsyncClient.getCollectionWmtsCapabilitiesWithResponse(collectionId, requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(xmlBytes -> {
                assertTrue(xmlBytes.length > 0, "WMTS XML should not be empty");
                String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);
                assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
            }).verifyComplete();
    }

    @Test
    @Tag("Crop")
    public void test09_07_CropCollectionFeatureAsync() {
        String collectionId = testEnvironment.getCollectionId();

        // Small polygon in Atlanta area
        List<List<List<Double>>> coordinates
            = Arrays.asList(Arrays.asList(Arrays.asList(-84.39, 33.68), Arrays.asList(-84.385, 33.68),
                Arrays.asList(-84.385, 33.685), Arrays.asList(-84.39, 33.685), Arrays.asList(-84.39, 33.68)));
        GeoJsonPolygon geometry = new GeoJsonPolygon().setCoordinates(coordinates);
        GeoJsonFeature feature = new GeoJsonFeature(geometry, FeatureType.FEATURE).setProperties(new HashMap<>());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient
            .cropCollectionFeatureWithResponse(collectionId, BinaryData.fromObject(feature), requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                assertTrue(imageBytes.length > 0, "Cropped image should not be empty");
            }).verifyComplete();
    }

    @Test
    @Tag("Tilesets")
    public void test09_08_GetCollectionTilesetsAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(dataAsyncClient.getCollectionTilesets(collectionId)).assertNext(tilesets -> {
            assertNotNull(tilesets, "Tilesets should not be null");
            assertNotNull(tilesets.getTilesets(), "Tilesets list should not be null");
            System.out.println("Async: Number of tilesets: " + tilesets.getTilesets().size());
        }).verifyComplete();
    }

    @Test
    @Tag("Assets")
    public void test09_09_GetCollectionAssetsForTileAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier
            .create(dataAsyncClient.getCollectionAssetsForTileWithTms(collectionId, "WebMercatorQuad", 13.0, 2174.0,
                3282.0))
            .assertNext(assets -> {
                assertNotNull(assets, "Tile assets should not be null");
                System.out.println("Async: Number of tile assets: " + assets.size());
            })
            .verifyComplete();
    }

    @Test
    @Tag("TilesetMetadata")
    public void test09_10_GetCollectionTilesetMetadataAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(dataAsyncClient.getCollectionTilesetMetadata(collectionId, "WebMercatorQuad"))
            .assertNext(metadata -> {
                assertNotNull(metadata, "Tileset metadata should not be null");
            })
            .verifyComplete();
    }
}
