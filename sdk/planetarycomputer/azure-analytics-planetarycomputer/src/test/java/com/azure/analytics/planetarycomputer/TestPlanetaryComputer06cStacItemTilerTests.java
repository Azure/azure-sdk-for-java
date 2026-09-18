// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item Tiler operations (Group 06c: Tests 11-15).
 */
@Tag("ItemTiler")
public class TestPlanetaryComputer06cStacItemTilerTests extends PlanetaryComputerTestBase {

    @Test
    @Tag("Crop")
    public void test06_11_CropGeoJsonWithDimensions() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        GeoJsonPolygon polygon = new GeoJsonPolygon().setCoordinates(
            Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714))));
        GeoJsonFeature feature
            = new GeoJsonFeature(polygon, FeatureType.FEATURE).setProperties(new java.util.HashMap<>());

        // Use protocol method to pass assets
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData
            = dataClient
                .cropFeatureWidthByHeightWithResponse(collectionId, itemId, 512, 512, "png",
                    BinaryData.fromObject(feature), requestOptions)
                .getValue();

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Tag("Statistics")
    public void test06_12_GetGeoJsonStatistics() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        GeoJsonPolygon polygon = new GeoJsonPolygon().setCoordinates(
            Arrays.asList(Arrays.asList(Arrays.asList(-84.3906, 33.6714), Arrays.asList(-84.3814, 33.6714),
                Arrays.asList(-84.3814, 33.6806), Arrays.asList(-84.3906, 33.6806), Arrays.asList(-84.3906, 33.6714))));
        GeoJsonFeature feature
            = new GeoJsonFeature(polygon, FeatureType.FEATURE).setProperties(new java.util.HashMap<>());

        // Use protocol method to avoid deserialization issues with null statistics fields
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        Response<BinaryData> response = dataClient.getItemFeatureStatisticsWithResponse(collectionId, itemId,
            BinaryData.fromObject(feature), requestOptions);

        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        BinaryData body = response.getValue();
        assertNotNull(body);

        String json = body.toString();
        assertTrue(json.contains("\"type\":\"Feature\""), "Response should be a GeoJSON Feature");
        assertTrue(json.contains("\"statistics\""), "Response should contain statistics");
        System.out.println("GeoJSON statistics retrieved successfully via protocol method");
    }

    @Test
    @Tag("Part")
    public void test06_13_GetPart() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double minx = -84.3930, miny = 33.6798, maxx = -84.3670, maxy = 33.7058;

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData = dataClient
            .getItemBboxCropWithResponse(collectionId, itemId, minx, miny, maxx, maxy, "png", requestOptions)
            .getValue();

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Tag("Part")
    public void test06_14_GetPartWithDimensions() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        double minx = -84.3930, miny = 33.6798, maxx = -84.3670, maxy = 33.7058;

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        requestOptions.addQueryParam("asset_bidx", "image|1,2,3", false);
        BinaryData imageData = dataClient
            .getItemBboxCropWithDimensionsWithResponse(collectionId, itemId, minx, miny, maxx, maxy, 256, 256, "png",
                requestOptions)
            .getValue();

        byte[] imageBytes = imageData.toBytes();
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 100);
        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i]);
        }
    }

    @Test
    @Tag("Point")
    public void test06_15_GetPoint() {
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId();

        // Coordinates within the NAIP item bounds (Georgia area)
        double longitude = -84.386;
        double latitude = 33.676;

        // Use protocol method to pass required assets parameter
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addQueryParam("assets", "image", false);
        Response<BinaryData> response
            = dataClient.getItemPointWithResponse(collectionId, itemId, longitude, latitude, requestOptions);

        assertNotNull(response);
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
        assertNotNull(response.getValue());
        System.out.println("Point data retrieved successfully");
    }
}
