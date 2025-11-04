// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.FilterLanguage;
import com.azure.analytics.planetarycomputer.models.ImageParameters;
import com.azure.analytics.planetarycomputer.models.ImageResponse;
import com.azure.analytics.planetarycomputer.models.Geometry;
import com.azure.analytics.planetarycomputer.models.Polygon;
import com.azure.analytics.planetarycomputer.models.RegisterMosaicsSearchOptions;
import com.azure.analytics.planetarycomputer.models.StacItemPointAsset;
import com.azure.analytics.planetarycomputer.models.TilerMosaicSearchRegistrationResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Mosaics Tiler operations (Group 05b: Tests 06-09).
 * Ported from TestPlanetaryComputer05bMosaicsTilerTests.cs
 */
@Tag("Mosaics")
public class TestPlanetaryComputer05bMosaicsTilerTests extends PlanetaryComputerTestBase {

    /**
     * Tests getting mosaic assets for a specific point.
     * Python equivalent: test_06_get_mosaics_assets_for_point
     * Java method: getMosaicsAssetsForPoint(searchId, longitude, latitude, ...)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("Assets")
    public void test05_06_GetMosaicsAssetsForPoint() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        double longitude = -84.43202751899601;
        double latitude = 33.639647639722273;

        System.out.println(String.format("Input - point: longitude=%f, latitude=%f", longitude, latitude));

        // Register search first
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        RegisterMosaicsSearchOptions registerOptions
            = new RegisterMosaicsSearchOptions().setFilter(filter).setFilterLanguage(FilterLanguage.CQL2_TEXT);
        TilerMosaicSearchRegistrationResponse registerResult = dataClient.registerMosaicsSearch(registerOptions);

        String searchId = registerResult.getSearchId();
        System.out.println("Using search ID: " + searchId);

        // Act - Get assets for point
        List<StacItemPointAsset> assets
            = dataClient.getMosaicsAssetsForPoint(searchId, longitude, latitude, 100, 100, 30, true, true, "EPSG:4326");

        // Assert
        assertNotNull(assets, "Assets list should not be null");

        System.out.println("Number of assets: " + assets.size());

        // If we have assets, validate structure
        if (assets.size() > 0) {
            StacItemPointAsset firstAsset = assets.get(0);
            assertNotNull(firstAsset, "First asset should not be null");
            assertNotNull(firstAsset.getId(), "Asset ID should not be null");
            assertFalse(firstAsset.getId().isEmpty(), "Asset ID should not be empty");

            System.out.println("First asset ID: " + firstAsset.getId());
        } else {
            System.out.println("No assets returned for this point");
        }
    }

    /**
     * Tests getting mosaic assets for a specific tile.
     * Python equivalent: test_07_get_mosaics_assets_for_tile
     * Java method: getMosaicsAssetsForTile(searchId, tileMatrixSetId, z, x, y, collectionId)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("Assets")
    public void test05_07_GetMosaicsAssetsForTile() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Input - tile coordinates: z=13, x=2174, y=3282");

        // Register search first
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        RegisterMosaicsSearchOptions registerOptions
            = new RegisterMosaicsSearchOptions().setFilter(filter).setFilterLanguage(FilterLanguage.CQL2_TEXT);
        TilerMosaicSearchRegistrationResponse registerResult = dataClient.registerMosaicsSearch(registerOptions);

        String searchId = registerResult.getSearchId();
        System.out.println("Using search ID: " + searchId);

        // Act - Get assets for tile
        List<BinaryData> assets = dataClient.getMosaicsAssetsForTile(searchId, "WebMercatorQuad", collectionId, 13.0,
            2174.0, 3282.0, null, null, null, null, null);

        // Assert
        assertNotNull(assets, "Assets list should not be null");

        System.out.println("Number of assets: " + assets.size());
        System.out.println("Assets for tile retrieved successfully");
    }

    /**
     * Tests creating a static image from a mosaic search.
     * Python equivalent: test_08_create_static_image
     * Java method: createStaticImage(collectionId, body)
     */
    @Test
    @Tag("StaticImage")
    public void test05_08_CreateStaticImage() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // Define geometry for the static image - coordinates as [[[lon, lat], ...]]
        List<List<List<Double>>> coordinates = Arrays.asList(Arrays.asList(Arrays.asList(-84.4537811, 33.6567307),
            Arrays.asList(-84.398056, 33.6567307), Arrays.asList(-84.398056, 33.6194572),
            Arrays.asList(-84.4537811, 33.6194572), Arrays.asList(-84.4537811, 33.6567307)));
        Polygon geometry = new Polygon().setCoordinates(coordinates);

        System.out.println("Geometry defined with coordinates");

        // Create CQL2-JSON filter (as map)
        Map<String, BinaryData> cqlFilter = new HashMap<>();
        cqlFilter.put("op", BinaryData.fromString("\"and\""));
        cqlFilter.put("args",
            BinaryData.fromString(String.format("[{\"op\": \"=\", \"args\": [{\"property\": \"collection\"}, \"%s\"]},"
                + "{\"op\": \"anyinteracts\", \"args\": [{\"property\": \"datetime\"},"
                + "{\"interval\": [\"2023-01-01T00:00:00Z\", \"2023-12-31T00:00:00Z\"]}]}]", collectionId)));

        // Create image request
        ImageParameters imageRequest = new ImageParameters(cqlFilter,
            String.format("assets=image&asset_bidx=image|1,2,3&collection=%s", collectionId), 1080, 1080);

        imageRequest.setZoom(13.0);
        imageRequest.setGeometry(geometry);
        imageRequest.setImageSize("1080x1080");
        imageRequest.setShowBranding(false);

        System.out.println(String.format("Image request: columns=%d, rows=%d, zoom=%d", imageRequest.getColumns(),
            imageRequest.getRows(), imageRequest.getZoom()));

        // Act - Create static image
        ImageResponse imageResponse = dataClient.createStaticImage(collectionId, imageRequest);

        // Assert
        assertNotNull(imageResponse, "Image response should not be null");
        assertNotNull(imageResponse.getUrl(), "Image URL should not be null");

        System.out.println("Static image created successfully");
        System.out.println("Image URL: " + imageResponse.getUrl());
    }

    /**
     * Tests retrieving a static image by ID.
     * Python equivalent: test_09_get_static_image
     * Java method: getStaticImage(collectionId, id)
     */
    @Test
    @Tag("StaticImage")
    public void test05_09_GetStaticImage() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // First create a static image to get an ID
        List<List<List<Double>>> coordinates = Arrays.asList(Arrays.asList(Arrays.asList(-84.4537811, 33.6567307),
            Arrays.asList(-84.398056, 33.6567307), Arrays.asList(-84.398056, 33.6194572),
            Arrays.asList(-84.4537811, 33.6194572), Arrays.asList(-84.4537811, 33.6567307)));
        Polygon geometry = new Polygon().setCoordinates(coordinates);

        Map<String, BinaryData> cqlFilter = new HashMap<>();
        cqlFilter.put("op", BinaryData.fromString("\"and\""));
        cqlFilter.put("args",
            BinaryData.fromString(String.format("[{\"op\": \"=\", \"args\": [{\"property\": \"collection\"}, \"%s\"]},"
                + "{\"op\": \"anyinteracts\", \"args\": [{\"property\": \"datetime\"},"
                + "{\"interval\": [\"2023-01-01T00:00:00Z\", \"2023-12-31T00:00:00Z\"]}]}]", collectionId)));

        ImageParameters imageRequest = new ImageParameters(cqlFilter,
            String.format("assets=image&asset_bidx=image|1,2,3&collection=%s", collectionId), 1080, 1080);
        imageRequest.setZoom(13.0);
        imageRequest.setGeometry(geometry);
        imageRequest.setImageSize("1080x1080");
        imageRequest.setShowBranding(false);

        ImageResponse createResponse = dataClient.createStaticImage(collectionId, imageRequest);

        String url = createResponse.getUrl();

        // Extract image ID from URL - split by '?' to remove query params, then get last path segment
        String[] urlParts = url.split("\\?")[0].split("/");
        String imageId = urlParts[urlParts.length - 1];

        System.out.println("Created image with ID: " + imageId);
        System.out.println("Image URL: " + url);

        assertNotNull(imageId, "Image ID should not be null");
        assertFalse(imageId.isEmpty(), "Image ID should not be empty");

        // Act - Get the static image
        BinaryData imageData = dataClient.getStaticImage(collectionId, imageId);

        // Assert
        byte[] imageBytes = imageData.toBytes();

        System.out.println("Image size: " + imageBytes.length + " bytes");
        System.out.println(
            "First 16 bytes (hex): " + bytesToHex(Arrays.copyOfRange(imageBytes, 0, Math.min(16, imageBytes.length))));

        // Verify PNG magic bytes
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 0, "Image bytes should not be empty");

        for (int i = 0; i < Math.min(pngMagic.length, imageBytes.length); i++) {
            assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
        }

        System.out.println("PNG magic bytes verified successfully");
    }

    /**
     * Helper method to convert bytes to hex string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
