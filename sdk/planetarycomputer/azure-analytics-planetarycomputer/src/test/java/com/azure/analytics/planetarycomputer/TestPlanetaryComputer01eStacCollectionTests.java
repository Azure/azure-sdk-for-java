// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacMosaic;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Collection operations (Group 01e: Tests 15-17a).
 * Ported from TestPlanetaryComputer01eStacCollectionTests.cs
 */
@Tag("STAC")
@Tag("Mosaics")
public class TestPlanetaryComputer01eStacCollectionTests extends PlanetaryComputerTestBase {

    /**
     * Test adding a mosaic to a collection.
     * Python equivalent: test_15_add_mosaic
     * Java method: addMosaic(collectionId, stacMosaic)
     */
    @Test
    @Tag("Mutation")
    public void test01_15_AddMosaic() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing addMosaic for collection: " + collectionId);

        // Check if mosaic already exists and delete it
        try {
            stacClient.deleteMosaic(collectionId, "test-mosaic-1");
            System.out.println("Deleted existing test mosaic");
        } catch (Exception e) {
            // Ignore if it doesn't exist
        }

        // Create mosaic
        StacMosaic mosaic
            = new StacMosaic("test-mosaic-1", "Test Most recent available", new ArrayList<Map<String, BinaryData>>());

        // Act
        StacMosaic createdMosaic = stacClient.addMosaic(collectionId, mosaic);

        // Assert
        assertNotNull(createdMosaic, "Created mosaic should not be null");
        assertEquals("test-mosaic-1", createdMosaic.getId(), "ID should match");

        System.out.println("Successfully added mosaic: " + createdMosaic.getId());
    }

    /**
     * Test getting a specific mosaic.
     * Python equivalent: test_16_get_mosaic
     * Java method: getMosaic(collectionId, mosaicId)
     */
    @Test
    public void test01_16_GetMosaic() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getMosaic for collection: " + collectionId);

        // Act
        StacMosaic mosaic = stacClient.getMosaic(collectionId, "test-mosaic-1");

        // Assert
        assertNotNull(mosaic, "Mosaic should not be null");
        assertEquals("test-mosaic-1", mosaic.getId(), "ID should match");

        System.out.println("Successfully retrieved mosaic: " + mosaic.getId());
    }

    /**
     * Test replacing a mosaic.
     * Python equivalent: test_17_replace_mosaic
     * Java method: replaceMosaic(collectionId, mosaicId, stacMosaic)
     */
    @Test
    @Tag("Mutation")
    public void test01_17_ReplaceMosaic() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing replaceMosaic for collection: " + collectionId);

        // Create updated mosaic (name max 30 chars)
        StacMosaic mosaic
            = new StacMosaic("test-mosaic-1", "Test Mosaic Updated", new ArrayList<Map<String, BinaryData>>());

        // Act
        StacMosaic updatedMosaic = stacClient.replaceMosaic(collectionId, "test-mosaic-1", mosaic);

        // Assert
        assertNotNull(updatedMosaic, "Updated mosaic should not be null");
        assertEquals("test-mosaic-1", updatedMosaic.getId(), "ID should match");
        assertEquals("Test Mosaic Updated", updatedMosaic.getName(), "Name should be updated");

        System.out.println("Successfully replaced mosaic: " + updatedMosaic.getId());
    }

    /**
     * Test deleting a mosaic.
     * Python equivalent: test_17a_delete_mosaic
     * Java method: deleteMosaic(collectionId, mosaicId)
     */
    @Test
    @Tag("Mutation")
    public void test01_17a_DeleteMosaic() {
        // Arrange
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing deleteMosaic for collection: " + collectionId);

        // Create a mosaic to be deleted
        StacMosaic mosaic = new StacMosaic("test-mosaic-delete", "Test Mosaic To Be Deleted",
            new ArrayList<Map<String, BinaryData>>());

        System.out.println("Creating mosaic for deletion");
        stacClient.addMosaic(collectionId, mosaic);

        // Verify it exists
        StacMosaic existingMosaic = stacClient.getMosaic(collectionId, "test-mosaic-delete");
        assertNotNull(existingMosaic, "Mosaic should exist before deletion");

        // Act - Delete it
        stacClient.deleteMosaic(collectionId, "test-mosaic-delete");

        System.out.println("Mosaic deleted successfully");

        // Verify deletion
        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> stacClient.getMosaic(collectionId, "test-mosaic-delete"),
                "Getting deleted mosaic should have failed");

        assertEquals(404, exception.getResponse().getStatusCode(), "Should return 404 for deleted resource");
        System.out.println("Verified mosaic was deleted");
    }
}
