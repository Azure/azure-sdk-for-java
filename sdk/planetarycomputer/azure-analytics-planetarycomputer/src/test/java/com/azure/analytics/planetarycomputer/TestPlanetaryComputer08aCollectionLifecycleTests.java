// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Collection Lifecycle operations (Group 08a: Tests 01-03).
 */
@Tag("CollectionLifecycle")
public class TestPlanetaryComputer08aCollectionLifecycleTests extends PlanetaryComputerTestBase {

    private static final String TEST_COLLECTION_ID = "test-collection-" + System.currentTimeMillis();

    @Test
    @Disabled("Recording incomplete - missing final GET request after LRO completion - needs to be re-recorded")
    @Tag("CreateCollection")
    public void test08_01_BeginCreateCollection() {
        StacClient stacClient = getStacClient();

        System.out.println("Test collection ID: " + TEST_COLLECTION_ID);

        // Check if collection exists and delete it first
        try {
            stacClient.getCollection(TEST_COLLECTION_ID);
            System.out.println("Collection '" + TEST_COLLECTION_ID + "' already exists, deleting first...");
            SyncPoller<Operation, Void> deletePoller = stacClient.beginDeleteCollection(TEST_COLLECTION_ID);
            deletePoller.getFinalResult();
            System.out.println("Deleted existing collection '" + TEST_COLLECTION_ID + "'");
        } catch (Exception ex) {
            System.out.println("Collection '" + TEST_COLLECTION_ID + "' does not exist, proceeding with creation");
        }

        // Create collection
        StacExtensionSpatialExtent spatialExtent
            = new StacExtensionSpatialExtent().setBoundingBox(Arrays.asList(Arrays.asList(-180.0, -90.0, 180.0, 90.0)));

        List<OffsetDateTime> temporalInterval
            = Arrays.asList(OffsetDateTime.parse("2018-01-01T00:00:00Z"), OffsetDateTime.parse("2018-12-31T23:59:59Z"));
        StacCollectionTemporalExtent temporalExtent = new StacCollectionTemporalExtent(Arrays.asList(temporalInterval));

        StacExtensionExtent extent = new StacExtensionExtent(spatialExtent, temporalExtent);

        Map<String, BinaryData> additionalProperties = new HashMap<>();
        additionalProperties.put("id", BinaryData.fromBytes(TEST_COLLECTION_ID.getBytes(StandardCharsets.UTF_8)));

        StacCollection collection = new StacCollection("An example collection", Arrays.asList(), "CC-BY-4.0", extent);
        collection.setStacVersion("1.0.0");
        collection.setTitle("Example Collection");
        collection.setType("Collection");
        collection.setShortDescription("An example collection");
        collection.setAdditionalProperties(additionalProperties);

        System.out.println("Calling: beginCreateCollection(...)");

        SyncPoller<Operation, Void> createPoller = stacClient.beginCreateCollection(collection);
        createPoller.getFinalResult();

        System.out.println("Collection creation operation completed");

        // Verify creation by retrieving the collection
        StacCollection verifyCollection = stacClient.getCollection(TEST_COLLECTION_ID);

        assertEquals(TEST_COLLECTION_ID, verifyCollection.getId(), "Collection ID should match");
        assertEquals("Example Collection", verifyCollection.getTitle(), "Title should match");
        assertEquals("Collection", verifyCollection.getType(), "Type should be Collection");

        System.out.println("Collection '" + TEST_COLLECTION_ID + "' created successfully");
    }

    @Test
    @Tag("UpdateCollection")
    public void test08_02_CreateOrReplaceCollection() {
        StacClient stacClient = getStacClient();

        System.out.println("Test collection ID: " + TEST_COLLECTION_ID);

        // Get existing collection
        StacCollection originalCollection = stacClient.getCollection(TEST_COLLECTION_ID);

        // Update the title
        originalCollection.setTitle("Updated Example Collection");

        // Replace collection
        System.out.println("Calling: createOrReplaceCollection(...)");
        StacCollection updatedCollection = stacClient.createOrReplaceCollection(TEST_COLLECTION_ID, originalCollection);

        assertNotNull(updatedCollection);
        assertEquals("Updated Example Collection", updatedCollection.getTitle(), "Title should be updated");

        System.out.println("Collection '" + TEST_COLLECTION_ID + "' updated successfully");
    }

    @Test
    @Tag("DeleteCollection")
    public void test08_03_DeleteCollection() {
        StacClient stacClient = getStacClient();

        System.out.println("Test collection ID: " + TEST_COLLECTION_ID);
        System.out.println("Calling: beginDeleteCollection(...)");

        SyncPoller<Operation, Void> deletePoller = stacClient.beginDeleteCollection(TEST_COLLECTION_ID);
        deletePoller.getFinalResult();

        System.out.println("Collection '" + TEST_COLLECTION_ID + "' deleted successfully");
    }
}
