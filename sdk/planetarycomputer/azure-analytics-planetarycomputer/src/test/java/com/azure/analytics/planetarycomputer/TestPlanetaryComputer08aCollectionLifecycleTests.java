// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.*;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

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

    // Fixed collection ID to match recording
    private static final String TEST_COLLECTION_ID = "test-collection-1769514824114";

    @Test
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

        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("id", TEST_COLLECTION_ID);

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

        // Use protocol method to work around StacCollection.toJson() codegen bug
        // that omits the 'id' field during serialization
        Response<BinaryData> getResponse
            = stacClient.getCollectionWithResponse(TEST_COLLECTION_ID, new RequestOptions());
        String collectionJson = getResponse.getValue().toString();

        // Modify the description in the raw JSON
        collectionJson = collectionJson.replace("An example collection", "Test collection - UPDATED");

        // Replace collection using protocol method (raw JSON preserves all fields including 'id')
        System.out.println("Calling: createOrReplaceCollection(...)");
        Response<BinaryData> updateResponse = stacClient.createOrReplaceCollectionWithResponse(TEST_COLLECTION_ID,
            BinaryData.fromString(collectionJson), new RequestOptions());

        assertTrue(updateResponse.getStatusCode() >= 200 && updateResponse.getStatusCode() < 300);
        String updatedJson = updateResponse.getValue().toString();
        assertTrue(updatedJson.contains("Test collection - UPDATED"), "Description should be updated");

        System.out.println("Collection '" + TEST_COLLECTION_ID + "' updated successfully");
    }

    @Test
    @Tag("DeleteCollection")
    public void test08_03_DeleteCollection() {
        StacClient stacClient = getStacClient();

        System.out.println("Test collection ID: " + TEST_COLLECTION_ID);
        System.out.println("Calling: beginDeleteCollection(...)");

        // Use protocol method and just start the LRO without waiting for completion
        // (matching .NET test pattern which uses WaitUntil.Started)
        SyncPoller<BinaryData, Void> deletePoller
            = stacClient.beginDeleteCollection(TEST_COLLECTION_ID, new RequestOptions());
        deletePoller.poll();

        System.out.println("Collection '" + TEST_COLLECTION_ID + "' delete initiated successfully");
    }
}
