// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.azure.analytics.planetarycomputer.models.GeoJsonFeature;
import com.azure.analytics.planetarycomputer.models.FeatureType;
import com.azure.analytics.planetarycomputer.models.GeoJsonPolygon;
import com.azure.analytics.planetarycomputer.models.TilerMosaicSearchRegistrationResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Mosaics Tiler operations (Groups 05a+05b+05c).
 * Mirrors sync tests in TestPlanetaryComputer05aMosaicsTilerTests,
 * TestPlanetaryComputer05bMosaicsTilerTests, and TestPlanetaryComputer05cMosaicsTilerTests.
 * Covers: registerMosaicsSearch, getSearchInfo, getSearchTileJson, getSearchTile,
 * getSearchWmtsCapabilities, getSearchPointWithAssets, getSearchAssetsForTile,
 * getSearchBboxCrop, cropSearchFeature.
 */
@Tag("Mosaics")
@Tag("Async")
public class TestPlanetaryComputer05AsyncMosaicsTilerTests extends PlanetaryComputerTestBase {

    /**
     * Registers a mosaics search asynchronously using protocol method with raw JSON filter.
     *
     * @param collectionId the collection ID to filter on
     * @return Mono emitting the search ID
     */
    private Mono<String> registerSearchAsync(String collectionId) {
        String requestBody = "{\"filter\":" + createCqlFilterJson(collectionId) + ",\"filter-lang\":\"cql2-json\""
            + ",\"sortby\":[{\"field\":\"datetime\",\"direction\":\"desc\"}]}";

        return dataAsyncClient
            .registerMosaicsSearchWithResponse(BinaryData.fromString(requestBody), new RequestOptions())
            .map(response -> response.getValue().toObject(TilerMosaicSearchRegistrationResponse.class).getSearchId());
    }

    /**
     * Creates a CQL2-JSON filter as raw JSON string.
     */
    private String createCqlFilterJson(String collectionId) {
        return "{\"op\":\"and\",\"args\":[" + "{\"op\":\"=\",\"args\":[{\"property\":\"collection\"},\"" + collectionId
            + "\"]}," + "{\"op\":\">=\",\"args\":[{\"property\":\"datetime\"},\"2021-01-01T00:00:00Z\"]},"
            + "{\"op\":\"<=\",\"args\":[{\"property\":\"datetime\"},\"2022-12-31T23:59:59Z\"]}" + "]}";
    }

    // ---- 05a tests ----

    @Test
    @Tag("RegisterSearch")
    public void test05_01_RegisterMosaicsSearchAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(registerSearchAsync(collectionId)).assertNext(searchId -> {
            assertNotNull(searchId, "Search ID should not be null");
            assertFalse(searchId.isEmpty(), "Search ID should not be empty");
        }).verifyComplete();
    }

    @Test
    @Tag("SearchInfo")
    public void test05_02_GetMosaicsSearchInfoAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier
            .create(registerSearchAsync(collectionId).flatMap(searchId -> dataAsyncClient.getSearchInfo(searchId)))
            .assertNext(searchInfo -> {
                assertNotNull(searchInfo, "Search info should not be null");
            })
            .verifyComplete();
    }

    @Test
    @Tag("TileJson")
    public void test05_03_GetMosaicsTileJsonAsync() {
        String collectionId = testEnvironment.getCollectionId();

        Mono<BinaryData> pipeline = registerSearchAsync(collectionId).flatMap(searchId -> {
            RequestOptions tileJsonOptions = new RequestOptions();
            tileJsonOptions.addQueryParam("assets", "image", false);
            tileJsonOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
            return dataAsyncClient.getSearchTileJsonWithResponse(searchId, tileJsonOptions).map(response -> {
                assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                return response.getValue();
            });
        });

        StepVerifier.create(pipeline).assertNext(tileJson -> {
            assertNotNull(tileJson, "TileJSON response should not be null");
        }).verifyComplete();
    }

    @Test
    @Tag("Tile")
    public void test05_04_GetMosaicsTileAsync() {
        String collectionId = testEnvironment.getCollectionId();

        Mono<byte[]> pipeline = registerSearchAsync(collectionId).flatMap(searchId -> {
            RequestOptions tileOptions = new RequestOptions();
            tileOptions.addQueryParam("assets", "image", false);
            tileOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
            return dataAsyncClient
                .getSearchTileWithTmsByFormatWithResponse(searchId, "WebMercatorQuad", 13.0, 2174.0, 3282.0, "png",
                    tileOptions)
                .map(response -> response.getValue().toBytes());
        });

        StepVerifier.create(pipeline).assertNext(imageBytes -> {
            byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
            assertTrue(imageBytes.length > 100, "Image should be substantial");
            for (int i = 0; i < pngMagic.length; i++) {
                assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
            }
        }).verifyComplete();
    }

    @Test
    @Tag("WMTS")
    public void test05_05_GetMosaicsWmtsCapabilitiesAsync() {
        String collectionId = testEnvironment.getCollectionId();

        Mono<String> pipeline = registerSearchAsync(collectionId).flatMap(searchId -> {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("tile_format", "png", false);
            requestOptions.addQueryParam("tile_scale", "1", false);
            requestOptions.addQueryParam("minzoom", "7", false);
            requestOptions.addQueryParam("maxzoom", "13", false);
            requestOptions.addQueryParam("assets", "image", false);
            requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
            return dataAsyncClient
                .getSearchWmtsCapabilitiesWithTmsWithResponse(searchId, "WebMercatorQuad", requestOptions)
                .map(response -> new String(response.getValue().toBytes(), StandardCharsets.UTF_8));
        });

        StepVerifier.create(pipeline).assertNext(xmlString -> {
            assertTrue(xmlString.length() > 0, "XML should not be empty");
            assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
            assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
        }).verifyComplete();
    }

    // ---- 05b tests ----

    @Test
    @Tag("Assets")
    public void test05_06_GetMosaicsAssetsForPointAsync() {
        String collectionId = testEnvironment.getCollectionId();
        double longitude = -84.43202751899601;
        double latitude = 33.639647639722273;

        StepVerifier
            .create(registerSearchAsync(collectionId)
                .flatMap(searchId -> dataAsyncClient.getSearchPointWithAssets(searchId, longitude, latitude)))
            .assertNext(assets -> {
                assertNotNull(assets, "Assets list should not be null");
                if (!assets.isEmpty()) {
                    assertNotNull(assets.get(0), "First asset should not be null");
                    assertNotNull(assets.get(0).getId(), "Asset ID should not be null");
                    assertFalse(assets.get(0).getId().isEmpty(), "Asset ID should not be empty");
                }
            })
            .verifyComplete();
    }

    @Test
    @Tag("Assets")
    public void test05_07_GetMosaicsAssetsForTileAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier
            .create(registerSearchAsync(collectionId).flatMap(searchId -> dataAsyncClient
                .getSearchAssetsForTileWithTms(searchId, "WebMercatorQuad", collectionId, 13.0, 2174.0, 3282.0)))
            .assertNext(assets -> {
                assertNotNull(assets, "Assets list should not be null");
            })
            .verifyComplete();
    }

    // ---- 05c tests ----

    @Test
    @Tag("BboxCrop")
    public void test05_08_GetSearchBboxCropAsync() {
        String collectionId = testEnvironment.getCollectionId();
        double minx = -84.39;
        double miny = 33.68;
        double maxx = -84.385;
        double maxy = 33.685;

        Mono<byte[]> pipeline = registerSearchAsync(collectionId).flatMap(searchId -> {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("assets", "image", false);
            requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
            return dataAsyncClient
                .getSearchBboxCropWithResponse(searchId, minx, miny, maxx, maxy, "png", requestOptions)
                .map(response -> response.getValue().toBytes());
        });

        StepVerifier.create(pipeline).assertNext(imageBytes -> {
            assertTrue(imageBytes.length > 0, "Bbox crop image should not be empty");
        }).verifyComplete();
    }

    @Test
    @Tag("FeatureCrop")
    public void test05_09_CropSearchFeatureAsync() {
        String collectionId = testEnvironment.getCollectionId();

        GeoJsonPolygon geometry = new GeoJsonPolygon()
            .setCoordinates(Arrays.asList(Arrays.asList(Arrays.asList(-84.39, 33.68), Arrays.asList(-84.385, 33.68),
                Arrays.asList(-84.385, 33.685), Arrays.asList(-84.39, 33.685), Arrays.asList(-84.39, 33.68))));
        GeoJsonFeature feature = new GeoJsonFeature(geometry, FeatureType.FEATURE).setProperties(new HashMap<>());

        Mono<byte[]> pipeline = registerSearchAsync(collectionId).flatMap(searchId -> {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.addQueryParam("assets", "image", false);
            requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
            return dataAsyncClient
                .cropSearchFeatureWithResponse(searchId, BinaryData.fromObject(feature), requestOptions)
                .map(response -> response.getValue().toBytes());
        });

        StepVerifier.create(pipeline).assertNext(imageBytes -> {
            assertTrue(imageBytes.length > 0, "Cropped image should not be empty");
        }).verifyComplete();
    }
}
