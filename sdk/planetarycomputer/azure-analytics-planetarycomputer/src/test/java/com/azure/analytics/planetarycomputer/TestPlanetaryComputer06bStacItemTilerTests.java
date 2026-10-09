// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06b: Tests 06-10).
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06bStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("Info")
    public void test06_06_GetInfoGeoJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        // Pass assets parameter like Python SDK: get_item_info_geo_json(assets=["image"])
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        Response<BinaryData> response = dataClient.getItemInfoGeoJsonWithResponse(collectionId, itemId, requestOptions);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        assertNotNull(response.getValue(), "Response body should not be null");
        System.out.println("Info GeoJSON retrieved successfully");
    }

    @Test
    @Tag("Statistics")
    public void test06_07_ListStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        // Pass assets parameter like Python SDK: get_item_statistics(assets=["image"])
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        Response<BinaryData> response = dataClient.getItemStatisticsWithResponse(collectionId, itemId, requestOptions);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        assertNotNull(response.getValue(), "Response body should not be null");
        System.out.println("Statistics retrieved successfully");
    }

    @Test
    @Tag("WMTS")
    public void test06_08_GetWmtsCapabilities() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        // Use protocol method with query params for WMTS XML response
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("tile_format", "png", false);
        requestOptions.addQueryParam("tile_scale", "1", false);
        requestOptions.addQueryParam("minzoom", "7", false);
        requestOptions.addQueryParam("maxzoom", "14", false);
        byte[] xmlBytes = dataClient
            .getItemWmtsCapabilitiesWithTmsWithResponse(collectionId, itemId, "WebMercatorQuad", requestOptions)
            .getValue()
            .toBytes();

        String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);

        System.out.println("XML size: " + xmlBytes.length + " bytes");
        System.out.println("XML first 200 chars: " + xmlString.substring(0, Math.min(200, xmlString.length())));

        assertTrue(xmlBytes.length > 0, "XML bytes should not be empty");
        assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
        assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
        assertTrue(xmlString.contains("TileMatrix"), "Response should contain TileMatrix information");

        System.out.println("WMTS capabilities XML validated successfully");
    }

    @Test
    @Tag("Statistics")
    public void test06_09_GetAssetStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        // Pass assets parameter like Python SDK: get_item_asset_statistics(assets=["image"])
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        Response<BinaryData> response
            = dataClient.getItemAssetStatisticsWithResponse(collectionId, itemId, requestOptions);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        assertNotNull(response.getValue(), "Response body should not be null");
        System.out.println("Asset statistics retrieved successfully");
    }

    @Test
    @Tag("Crop")
    public void test06_10_CropGeoJson() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        List<List<List<Double>>> coordinates
            = Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714)));
        GeoJsonPolygon geometry = new GeoJsonPolygon().setCoordinates(coordinates);
        GeoJsonFeature feature
            = new GeoJsonFeature(geometry, FeatureType.FEATURE).setProperties(new java.util.HashMap<>());

        System.out.println("Input - collection_id: " + collectionId);
        System.out.println("Input - item_id: " + itemId);

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        requestOptions.addQueryParam("format", "png", false);
        BinaryData imageData
            = dataClient.cropFeatureWithResponse(collectionId, itemId, BinaryData.fromObject(feature), requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        System.out.println("Image size: " + imageBytes.length + " bytes");

        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100, "Image should be substantial");
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }

        System.out.println("PNG magic bytes verified successfully");
    }
}
