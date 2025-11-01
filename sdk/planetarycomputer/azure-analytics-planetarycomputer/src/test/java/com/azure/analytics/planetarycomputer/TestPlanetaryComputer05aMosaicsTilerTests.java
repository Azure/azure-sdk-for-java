// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.FilterLanguage;
import com.azure.analytics.planetarycomputer.models.GetMosaicTileJsonOptions;
import com.azure.analytics.planetarycomputer.models.GetMosaicTileOptions;
import com.azure.analytics.planetarycomputer.models.GetMosaicWmtsCapabilitiesOptions;
import com.azure.analytics.planetarycomputer.models.RegisterMosaicsSearchOptions;
import com.azure.analytics.planetarycomputer.models.StacSearchSortingDirection;
import com.azure.analytics.planetarycomputer.models.StacSortExtension;
import com.azure.analytics.planetarycomputer.models.TileJsonMetadata;
import com.azure.analytics.planetarycomputer.models.TilerImageFormat;
import com.azure.analytics.planetarycomputer.models.TilerMosaicSearchRegistrationResponse;
import com.azure.analytics.planetarycomputer.models.TilerStacSearchRegistration;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Mosaics Tiler operations (Group 05a: Tests 01-05).
 * Ported from TestPlanetaryComputer05aMosaicsTilerTests.cs
 */
@Tag("Mosaics")
public class TestPlanetaryComputer05aMosaicsTilerTests extends PlanetaryComputerTestBase {

    /**
     * Tests registering a mosaics search with STAC search parameters.
     * Python equivalent: test_01_register_mosaics_search
     * Java method: registerMosaicsSearch(filter, filterLanguage, sortBy)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("RegisterSearch")
    public void test05_01_RegisterMosaicsSearch() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Input - collection_id: " + collectionId);

        // Create search parameters - filter to 2021-2022 date range with CQL2-Text
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        StacSortExtension[] sortBy
            = new StacSortExtension[] { new StacSortExtension("datetime", StacSearchSortingDirection.DESC) };

        System.out.println("Filter: " + filter);
        System.out.println("Filter Language: cql2-text");

        // Act
        RegisterMosaicsSearchOptions options = new RegisterMosaicsSearchOptions().setFilter(filter)
            .setFilterLanguage(FilterLanguage.CQL2_TEXT)
            .setSortBy(Arrays.asList(sortBy));
        TilerMosaicSearchRegistrationResponse result = dataClient.registerMosaicsSearch(options);

        // Assert
        assertNotNull(result, "Result should not be null");
        validateNotNullOrEmpty(result.getSearchId(), "Search ID");

        System.out.println("Search ID: " + result.getSearchId());

        // Search ID should be a non-empty string (typically a hash)
        assertTrue(result.getSearchId().length() > 0, "Search ID should not be empty");

        // In live mode, verify search ID format (typically alphanumeric hash)
        if ("LIVE".equals(System.getenv("AZURE_TEST_MODE"))) {
            assertTrue(result.getSearchId().matches("^[a-zA-Z0-9]+$"),
                "Search ID should be alphanumeric (hash format)");
        }
    }

    /**
     * Tests getting mosaics search info after registration.
     * Python equivalent: test_02_get_mosaics_search_info
     * Java method: getMosaicsSearchInfo(searchId)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("SearchInfo")
    public void test05_02_GetMosaicsSearchInfo() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // First, register a search to get a search ID with CQL2-Text
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        RegisterMosaicsSearchOptions options
            = new RegisterMosaicsSearchOptions().setFilter(filter).setFilterLanguage(FilterLanguage.CQL2_TEXT);
        TilerMosaicSearchRegistrationResponse registerResult = dataClient.registerMosaicsSearch(options);

        String searchId = registerResult.getSearchId();
        System.out.println("Registered Search ID: " + searchId);

        // Act - Get search info for the registered search
        TilerStacSearchRegistration searchInfo = dataClient.getMosaicsSearchInfo(searchId);

        // Assert
        assertNotNull(searchInfo, "Search info should not be null");

        System.out.println("Search registration retrieved successfully");
        System.out.println("Search info retrieved for search ID: " + searchId);
    }

    /**
     * Tests getting mosaics tile JSON metadata.
     * Python equivalent: test_03_get_mosaics_tile_json
     * Java method: getMosaicsTileJson(searchId, tileMatrixSetId, assets, ...)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("TileJson")
    public void test05_03_GetMosaicsTileJson() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // Register search first
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        RegisterMosaicsSearchOptions registerOptions
            = new RegisterMosaicsSearchOptions().setFilter(filter).setFilterLanguage(FilterLanguage.CQL2_TEXT);
        TilerMosaicSearchRegistrationResponse registerResult = dataClient.registerMosaicsSearch(registerOptions);

        String searchId = registerResult.getSearchId();
        System.out.println("Using search ID: " + searchId);

        // Act - Get tile JSON metadata
        GetMosaicTileJsonOptions tileJsonOptions = new GetMosaicTileJsonOptions().setAssets(Arrays.asList("image"))
            .setExpression("image|1,2,3")
            .setMinZoom(1)
            .setMaxZoom(9)
            .setTileFormat(TilerImageFormat.PNG)
            .setCollection(collectionId);
        TileJsonMetadata tileJson = dataClient.getMosaicsTileJson(searchId, "WebMercatorQuad", tileJsonOptions);

        // Assert
        assertNotNull(tileJson, "TileJSON should not be null");
        assertNotNull(tileJson.getTileJson(), "TileJSON version should not be null");
        assertNotNull(tileJson.getTiles(), "Tiles array should not be null");
        assertTrue(tileJson.getTiles().size() > 0, "Should have at least one tile URL pattern");

        System.out.println("TileJSON version: " + tileJson.getTileJson());
        System.out.println("Number of tile URL patterns: " + tileJson.getTiles().size());
        if (tileJson.getTiles().size() > 0) {
            System.out.println("First tile URL pattern: " + tileJson.getTiles().get(0));
        }
    }

    /**
     * Tests getting a specific mosaic tile as PNG image.
     * Python equivalent: test_04_get_mosaics_tile
     * Java method: getMosaicsTile(searchId, tileMatrixSetId, z, x, y, ...)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("Tile")
    public void test05_04_GetMosaicsTile() {
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

        // Act - Get tile image
        GetMosaicTileOptions tileOptions = new GetMosaicTileOptions().setAssets(Arrays.asList("image"))
            .setExpression("image|1,2,3")
            .setCollection(collectionId);
        BinaryData imageData = dataClient.getMosaicsTile(searchId, "WebMercatorQuad", 13.0, 2174.0, 3282.0, 1.0, "png",
            tileOptions, "image/png");

        // Assert
        byte[] imageBytes = imageData.toBytes();

        System.out.println("Image size: " + imageBytes.length + " bytes");
        System.out.println(
            "First 16 bytes (hex): " + bytesToHex(Arrays.copyOfRange(imageBytes, 0, Math.min(16, imageBytes.length))));

        // Verify PNG magic bytes
        byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        assertTrue(imageBytes.length > 0, "Image bytes should not be empty");
        assertTrue(imageBytes.length > 100,
            String.format("Image should be substantial, got only %d bytes", imageBytes.length));

        for (int i = 0; i < pngMagic.length; i++) {
            assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
        }

        System.out.println("PNG magic bytes verified successfully");
    }

    /**
     * Tests getting WMTS capabilities XML for mosaics.
     * Python equivalent: test_05_get_mosaics_wmts_capabilities
     * Java method: getMosaicsWmtsCapabilities(searchId, tileMatrixSetId, ...)
     */
    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("WMTS")
    public void test05_05_GetMosaicsWmtsCapabilities() {
        // Arrange
        DataClient dataClient = getDataClient();
        String collectionId = testEnvironment.getCollectionId();

        // Register search first
        String filter = String.format(
            "collection = '%s' AND datetime >= TIMESTAMP('2021-01-01T00:00:00Z') AND datetime <= TIMESTAMP('2022-12-31T23:59:59Z')",
            collectionId);

        RegisterMosaicsSearchOptions registerOptions
            = new RegisterMosaicsSearchOptions().setFilter(filter).setFilterLanguage(FilterLanguage.CQL2_TEXT);
        TilerMosaicSearchRegistrationResponse registerResult = dataClient.registerMosaicsSearch(registerOptions);

        String searchId = registerResult.getSearchId();
        System.out.println("Using search ID: " + searchId);

        // Act - Get WMTS capabilities
        GetMosaicWmtsCapabilitiesOptions wmtsOptions
            = new GetMosaicWmtsCapabilitiesOptions().setTileFormat(TilerImageFormat.PNG)
                .setTileScale(1)
                .setMinZoom(7)
                .setMaxZoom(13)
                .setAssets(Arrays.asList("image"))
                .setExpression("image|1,2,3");
        byte[] xmlBytes = dataClient.getMosaicsWmtsCapabilities(searchId, "WebMercatorQuad", wmtsOptions);

        // Assert
        String xmlString = new String(xmlBytes, StandardCharsets.UTF_8);

        System.out.println("XML size: " + xmlBytes.length + " bytes");
        System.out.println("XML first 200 chars: " + xmlString.substring(0, Math.min(200, xmlString.length())));

        // Validate XML structure
        assertTrue(xmlBytes.length > 0, "XML bytes should not be empty");
        assertTrue(xmlString.contains("Capabilities"), "Response should contain Capabilities element");
        assertTrue(xmlString.toLowerCase().contains("wmts"), "Response should reference WMTS");
        assertTrue(xmlString.contains("TileMatrix"), "Response should contain TileMatrix information");

        System.out.println("WMTS capabilities XML validated successfully");
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
