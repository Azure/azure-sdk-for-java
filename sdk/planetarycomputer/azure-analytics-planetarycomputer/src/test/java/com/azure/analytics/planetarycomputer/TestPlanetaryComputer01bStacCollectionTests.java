// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacMosaic;
import com.azure.analytics.planetarycomputer.models.TileSettings;
import com.azure.analytics.planetarycomputer.models.UserCollectionSettings;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Collection operations (Group 01b: Tests 06-10).
 * Ported from TestPlanetaryComputer01bStacCollectionTests.cs
 */
@Tag("STAC")
public class TestPlanetaryComputer01bStacCollectionTests extends PlanetaryComputerTestBase {

    /**
     * Test getting tile settings for a collection.
     * Python equivalent: test_06_get_tile_settings
     * Java method: getTileSettings(collectionId)
     */
    @Test
    @Tag("TileSettings")
    public void test01_06_GetTileSettings() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getTileSettings for collection: " + collectionId);

        // Act
        TileSettings tileSettings = stacClient.getTileSettings(collectionId);

        // Assert
        assertNotNull(tileSettings, "TileSettings should not be null");

        // Log available properties
        System.out.println("Max items per tile: " + tileSettings.getMaxItemsPerTile());
        System.out.println("Min zoom: " + tileSettings.getMinZoom());

        if (tileSettings.getDefaultLocation() != null) {
            System.out.println("Default location: " + tileSettings.getDefaultLocation());
        }

        System.out.println("Successfully retrieved tile settings");
    }

    /**
     * Test listing mosaics for a collection.
     * Python equivalent: test_07_list_mosaics
     * Java method: listMosaics(collectionId)
     */
    @Test
    @Tag("Mosaics")
    public void test01_07_ListMosaics() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing listMosaics for collection: " + collectionId);

        // Act
        List<StacMosaic> mosaics = stacClient.listMosaics(collectionId);

        // Assert
        assertNotNull(mosaics, "Mosaics should not be null");

        int mosaicCount = mosaics.size();
        System.out.println("Number of mosaics: " + mosaicCount);

        if (mosaicCount > 0) {
            StacMosaic firstMosaic = mosaics.get(0);
            assertNotNull(firstMosaic.getId(), "Mosaic should have ID");
            System.out.println("First mosaic ID: " + firstMosaic.getId());

            if (firstMosaic.getName() != null) {
                System.out.println("First mosaic name: " + firstMosaic.getName());
            }
        }

        System.out.println("Successfully listed " + mosaicCount + " mosaics");
    }

    /**
     * Test getting queryables for a collection.
     * Python equivalent: test_08_get_collection_queryables
     * Java method: getCollectionQueryablesWithResponse(collectionId, requestOptions)
     * Returns raw JSON for queryables
     */
    @Test
    @Tag("Queryables")
    public void test01_08_GetCollectionQueryables() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionQueryables for collection: " + collectionId);

        // Act - Using protocol method for raw JSON response
        Response<BinaryData> response
            = stacClient.getCollectionQueryablesWithResponse(collectionId, new RequestOptions());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode(), "Expected successful response");

        // Parse JSON response
        BinaryData responseBody = response.getValue();
        assertNotNull(responseBody, "Response body should not be null");

        @SuppressWarnings("unchecked")
        Map<String, Object> jsonMap = responseBody.toObject(Map.class);
        assertTrue(jsonMap.containsKey("properties"), "Response should have 'properties' key");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) jsonMap.get("properties");

        int propertyCount = properties.size();
        System.out.println("Number of queryables: " + propertyCount);

        if (propertyCount > 0) {
            String firstProperty = properties.keySet().iterator().next();
            System.out.println("First queryable: " + firstProperty);
        }

        System.out.println("Successfully retrieved " + propertyCount + " queryables");
    }

    /**
     * Test listing all queryables (global).
     * Python equivalent: test_09_list_queryables
     * Java method: getQueryablesWithResponse(requestOptions)
     */
    @Test
    @Tag("Queryables")
    public void test01_09_ListQueryables() {
        // Arrange
        StacClient stacClient = getStacClient();

        System.out.println("Testing getQueryables (global queryables)");

        // Act - Using protocol method for raw JSON response
        Response<BinaryData> response = stacClient.listQueryablesWithResponse(new RequestOptions());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode(), "Expected successful response");

        // Parse JSON response
        BinaryData responseBody = response.getValue();
        assertNotNull(responseBody, "Response body should not be null");

        @SuppressWarnings("unchecked")
        Map<String, Object> jsonMap = responseBody.toObject(Map.class);
        assertTrue(jsonMap.containsKey("properties"), "Response should have 'properties' key");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) jsonMap.get("properties");

        int propertyCount = properties.size();
        System.out.println("Number of global queryables: " + propertyCount);

        System.out.println("Successfully retrieved " + propertyCount + " global queryables");
    }

    /**
     * Test getting collection configuration.
     * Python equivalent: test_10_get_collection_configuration
     * Java method: getCollectionConfiguration(collectionId)
     */
    @Test
    @Tag("Configuration")
    public void test01_10_GetCollectionConfiguration() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getCollectionConfiguration for collection: " + collectionId);

        // Act
        UserCollectionSettings config = stacClient.getCollectionConfiguration(collectionId);

        // Assert
        assertNotNull(config, "Configuration should not be null");

        System.out.println("Successfully retrieved collection configuration");
    }
}
