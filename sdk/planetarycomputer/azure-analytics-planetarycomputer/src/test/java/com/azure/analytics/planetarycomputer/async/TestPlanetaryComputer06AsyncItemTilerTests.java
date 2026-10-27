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
 * Async tests for STAC Item Tiler operations (Groups 06a+06b+06c+06d).
 * Mirrors sync tests in TestPlanetaryComputer06aStacItemTilerTests,
 * TestPlanetaryComputer06bStacItemTilerTests, TestPlanetaryComputer06cStacItemTilerTests,
 * and TestPlanetaryComputer06dStacItemTilerTests.
 * Covers: getTileMatrixDefinitions, getTileMatrices, getItemAvailableAssets,
 * getItemBounds, getItemPreview, getItemInfoGeoJson, getItemStatistics,
 * getItemWmtsCapabilities, cropFeature, getItemBboxCrop, getItemPoint,
 * getItemPreviewWithFormat, getItemTileJson, getTileWithTmsByFormat.
 */
@Tag("ItemTiler")
@Tag("Async")
public class TestPlanetaryComputer06AsyncItemTilerTests extends PlanetaryComputerTestBase {

    // ---- 06a tests ----

    @Test
    @Tag("TileMatrices")
    @Tag("TileMatrixDefinitions")
    public void test06_01_GetTileMatrixDefinitionsAsync() {
        StepVerifier.create(dataAsyncClient.getTileMatrixDefinitions("WebMercatorQuad")).assertNext(tileMatrixSet -> {
            assertNotNull(tileMatrixSet, "TileMatrixSet should not be null");
            assertNotNull(tileMatrixSet.getId(), "TileMatrixSet ID should not be null");
            assertNotNull(tileMatrixSet.getTileMatrices(), "TileMatrices should not be null");
            assertTrue(tileMatrixSet.getTileMatrices().size() > 0, "Should have at least one tile matrix");

            assertEquals(256, tileMatrixSet.getTileMatrices().get(0).getTileWidth(),
                "Standard tile width should be 256");
            assertEquals(256, tileMatrixSet.getTileMatrices().get(0).getTileHeight(),
                "Standard tile height should be 256");
        }).verifyComplete();
    }

    @Test
    @Tag("TileMatrices")
    public void test06_02_ListTileMatricesAsync() {
        StepVerifier.create(dataAsyncClient.getTileMatrices()).assertNext(tileMatrixIds -> {
            assertNotNull(tileMatrixIds, "Tile matrix IDs should not be null");
            assertTrue(tileMatrixIds.contains("WebMercatorQuad"), "Should include WebMercatorQuad");
            assertTrue(tileMatrixIds.contains("WorldCRS84Quad"), "Should include WorldCRS84Quad");
        }).verifyComplete();
    }

    @Test
    @Tag("Assets")
    public void test06_03_GetItemAvailableAssetsAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        StepVerifier.create(dataAsyncClient.getItemAvailableAssets(collectionId, itemId)).assertNext(assets -> {
            assertNotNull(assets, "Assets list should not be null");
            assertTrue(assets.size() > 0, "Should have at least one asset");
            for (String asset : assets) {
                assertNotNull(asset, "Asset name should not be null");
                assertFalse(asset.isEmpty(), "Asset name should not be empty");
            }
        }).verifyComplete();
    }

    @Test
    @Tag("Bounds")
    public void test06_04_GetItemBoundsAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        StepVerifier.create(dataAsyncClient.getItemBounds(collectionId, itemId)).assertNext(boundsResult -> {
            assertNotNull(boundsResult, "Bounds result should not be null");
            assertNotNull(boundsResult.getBounds(), "Bounds array should not be null");
            assertEquals(4, boundsResult.getBounds().size(),
                "Bounds should have 4 coordinates [minx, miny, maxx, maxy]");

            List<Double> bounds = boundsResult.getBounds();
            assertTrue(bounds.get(0) < bounds.get(2), "minx should be less than maxx");
            assertTrue(bounds.get(1) < bounds.get(3), "miny should be less than maxy");
        }).verifyComplete();
    }

    @Test
    @Tag("Preview")
    public void test06_05_GetItemPreviewAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient.getItemPreviewWithResponse(collectionId, itemId, requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                assertTrue(imageBytes.length > 100,
                    String.format("Image should be substantial, got only %d bytes", imageBytes.length));

                // Server may return JPEG or PNG
                boolean isJpeg = imageBytes.length >= 3
                    && imageBytes[0] == (byte) 0xFF
                    && imageBytes[1] == (byte) 0xD8
                    && imageBytes[2] == (byte) 0xFF;
                byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
                boolean isPng = imageBytes.length >= 8;
                if (isPng) {
                    for (int i = 0; i < pngMagic.length && isPng; i++) {
                        isPng = imageBytes[i] == pngMagic[i];
                    }
                }
                assertTrue(isJpeg || isPng, "Expected JPEG or PNG format");
            }).verifyComplete();
    }

    // ---- 06b tests ----

    @Test
    @Tag("Info")
    public void test06_06_GetInfoGeoJsonAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);

        StepVerifier.create(dataAsyncClient.getItemInfoGeoJsonWithResponse(collectionId, itemId, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                assertNotNull(response.getValue(), "Response body should not be null");
            })
            .verifyComplete();
    }

    @Test
    @Tag("Statistics")
    public void test06_07_GetItemStatisticsAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);

        StepVerifier.create(dataAsyncClient.getItemStatisticsWithResponse(collectionId, itemId, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                assertNotNull(response.getValue(), "Response body should not be null");
            })
            .verifyComplete();
    }

    @Test
    @Tag("WMTS")
    public void test06_08_GetWmtsCapabilitiesAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("tile_format", "png", false);
        requestOptions.addQueryParam("tile_scale", "1", false);
        requestOptions.addQueryParam("minzoom", "7", false);
        requestOptions.addQueryParam("maxzoom", "14", false);

        StepVerifier
            .create(dataAsyncClient
                .getItemWmtsCapabilitiesWithTmsWithResponse(collectionId, itemId, "WebMercatorQuad", requestOptions)
                .map(response -> new String(response.getValue().toBytes(), StandardCharsets.UTF_8)))
            .assertNext(xmlString -> {
                assertTrue(xmlString.length() > 0, "XML should not be empty");
                assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
                assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
                assertTrue(xmlString.contains("TileMatrix"), "Response should contain TileMatrix information");
            })
            .verifyComplete();
    }

    @Test
    @Tag("Crop")
    public void test06_09_CropFeatureAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        GeoJsonPolygon geometry = new GeoJsonPolygon().setCoordinates(
            Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714))));
        GeoJsonFeature feature = new GeoJsonFeature(geometry, FeatureType.FEATURE).setProperties(new HashMap<>());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        requestOptions.addQueryParam("format", "png", false);

        StepVerifier.create(dataAsyncClient
            .cropFeatureWithResponse(collectionId, itemId, BinaryData.fromObject(feature), requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
                assertTrue(imageBytes.length > 100, "Image should be substantial");
                for (int i = 0; i < pngMagic.length; i++) {
                    assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
                }
            }).verifyComplete();
    }

    @Test
    @Tag("Part")
    public void test06_10_GetItemBboxCropAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double minx = -84.3930, miny = 33.6798, maxx = -84.3670, maxy = 33.7058;

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient
            .getItemBboxCropWithResponse(collectionId, itemId, minx, miny, maxx, maxy, "png", requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
                assertTrue(imageBytes.length > 100, "Image should be substantial");
                for (int i = 0; i < pngMagic.length; i++) {
                    assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
                }
            }).verifyComplete();
    }

    // ---- 06c tests ----

    @Test
    @Tag("Point")
    public void test06_11_GetItemPointAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double longitude = -84.386;
        double latitude = 33.676;

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);

        StepVerifier
            .create(dataAsyncClient.getItemPointWithResponse(collectionId, itemId, longitude, latitude, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                assertNotNull(response.getValue(), "Response body should not be null");
            })
            .verifyComplete();
    }

    // ---- 06d tests ----

    @Test
    @Tag("Preview")
    public void test06_12_GetPreviewWithFormatAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("format", "jpg", false);

        StepVerifier
            .create(dataAsyncClient.getItemPreviewWithFormatWithResponse(collectionId, itemId, "jpg", requestOptions)
                .map(response -> response.getValue().toBytes()))
            .assertNext(imageBytes -> {
                byte[] jpegMagic = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
                assertTrue(imageBytes.length > 100, "Image should be substantial");
                for (int i = 0; i < jpegMagic.length; i++) {
                    assertEquals(jpegMagic[i], imageBytes[i], String.format("JPEG magic byte %d mismatch", i));
                }
            })
            .verifyComplete();
    }

    @Test
    @Tag("TileJson")
    public void test06_13_GetItemTileJsonAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient.getItemTileJsonWithResponse(collectionId, itemId, requestOptions))
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                assertNotNull(response.getValue(), "Response body should not be null");
            })
            .verifyComplete();
    }

    @Test
    @Tag("Tile")
    public void test06_14_GetTileByFormatAsync() {
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);

        StepVerifier.create(dataAsyncClient
            .getTileWithTmsByFormatWithResponse(collectionId, itemId, "WebMercatorQuad", 14, 4349, 6564, "png",
                requestOptions)
            .map(response -> response.getValue().toBytes())).assertNext(imageBytes -> {
                byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
                assertTrue(imageBytes.length > 100, "Image should be substantial");
                for (int i = 0; i < pngMagic.length; i++) {
                    assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
                }
            }).verifyComplete();
    }
}
