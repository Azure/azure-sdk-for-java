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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Collection Lifecycle operations (Group 08a).
 * Combined into a single test following the Python/JS SDK pattern:
 * - Python: create_collection → update_collection (in planetary_computer_00_stac_collection.py)
 * - JS: "should create, replace, and delete a collection" (07_collectionLifecycle.spec.ts)
 */
@Tag("CollectionLifecycle")
public class TestPlanetaryComputer08aCollectionLifecycleTests extends PlanetaryComputerTestBase {

    private static final String TEST_COLLECTION_ID = "test-collection-lifecycle-java";

    /**
     * Test full collection lifecycle: create → replace → delete.
     * Follows the JS SDK pattern of a single test covering the full lifecycle.
     * Python equivalent: create_collection + update_collection
     * JS equivalent: "should create, replace, and delete a collection"
     */
    @Test
    @Tag("CollectionLifecycle")
    public void test08_01_CreateReplaceDeleteCollection() {
        StacClient stacClient = getStacClient();

        System.out.println("Test collection ID: " + TEST_COLLECTION_ID);

        // Step 1: Clean up if exists from a previous failed run
        // Matching JS: try { deleteCollection } catch { ignore }
        try {
            SyncPoller<BinaryData, Void> cleanupPoller
                = stacClient.beginDeleteCollection(TEST_COLLECTION_ID, new RequestOptions());
            cleanupPoller.waitForCompletion();
            System.out.println("Cleaned up existing collection");

            // Wait for collection to be fully removed (matching JS: setTimeout 30s)
            waitForCollectionDeletion(stacClient, TEST_COLLECTION_ID, 60);
        } catch (Exception ex) {
            System.out.println("No existing collection to clean up");
        }

        // Step 2: Create collection using protocol method with raw JSON
        // Matching Python: begin_create_collection(body=collection_data)
        // Matching JS: createCollectionWithRetry({...})
        String collectionJson = "{" + "\"id\": \"" + TEST_COLLECTION_ID + "\"," + "\"type\": \"Collection\","
            + "\"stac_version\": \"1.0.0\"," + "\"title\": \"Java SDK Test Collection\","
            + "\"description\": \"Test collection for Java SDK lifecycle tests\"," + "\"license\": \"proprietary\","
            + "\"extent\": {" + "  \"spatial\": {\"bbox\": [[-180, -90, 180, 90]]},"
            + "  \"temporal\": {\"interval\": [[\"2020-01-01T00:00:00Z\", \"2024-12-31T23:59:59Z\"]]}" + "},"
            + "\"links\": []" + "}";

        System.out.println("Step 2: Creating collection...");
        SyncPoller<BinaryData, BinaryData> createPoller
            = stacClient.beginCreateCollection(BinaryData.fromString(collectionJson), new RequestOptions());
        createPoller.getFinalResult();
        System.out.println("Collection created");

        // Step 3: Verify creation
        // Matching JS: getCollection → expect(id), expect(title)
        StacCollection collection = stacClient.getCollection(TEST_COLLECTION_ID);
        assertEquals(TEST_COLLECTION_ID, collection.getId(), "Collection ID should match");
        assertEquals("Java SDK Test Collection", collection.getTitle(), "Title should match");
        System.out.println("Step 3: Collection verified - ID: " + collection.getId());

        // Step 4: Replace (update) the collection
        // Matching Python: replace_collection(collection_id, body=updated)
        // Matching JS: replaceCollection(id, {...description: "UPDATED"})
        Response<BinaryData> getResponse
            = stacClient.getCollectionWithResponse(TEST_COLLECTION_ID, new RequestOptions());
        String currentJson = getResponse.getValue().toString();
        String updatedJson = currentJson.replace("Test collection for Java SDK lifecycle tests",
            "UPDATED - Test collection for Java SDK lifecycle tests");

        System.out.println("Step 4: Replacing collection...");
        Response<BinaryData> replaceResponse = stacClient.replaceCollectionWithResponse(TEST_COLLECTION_ID,
            BinaryData.fromString(updatedJson), new RequestOptions());

        assertTrue(replaceResponse.getStatusCode() >= 200 && replaceResponse.getStatusCode() < 300);
        String replaceBody = replaceResponse.getValue().toString();
        assertTrue(replaceBody.contains("UPDATED"), "Description should be updated");
        System.out.println("Step 4: Collection replaced successfully");

        // Step 5: Delete collection
        // Matching Python: begin_delete_collection(collection_id, polling=True).result()
        // Matching JS: deleteCollection().pollUntilDone()
        System.out.println("Step 5: Deleting collection...");
        SyncPoller<BinaryData, Void> deletePoller
            = stacClient.beginDeleteCollection(TEST_COLLECTION_ID, new RequestOptions());
        deletePoller.waitForCompletion();
        System.out.println("Step 5: Collection deleted successfully");

        System.out.println("Full collection lifecycle completed: create → replace → delete");
    }

    /**
     * Waits until a collection is fully deleted by polling getCollection until it returns 404.
     * Matching JS pattern: setTimeout with retry loop.
     */
    private void waitForCollectionDeletion(StacClient stacClient, String collectionId, int maxWaitSeconds) {
        long deadline = System.currentTimeMillis() + (maxWaitSeconds * 1000L);
        int attempt = 0;

        while (System.currentTimeMillis() < deadline) {
            attempt++;
            try {
                stacClient.getCollection(collectionId);
                System.out.println("  Attempt " + attempt + ": collection still exists, waiting...");
                Thread.sleep(5000);
            } catch (com.azure.core.exception.HttpResponseException ex) {
                if (ex.getResponse().getStatusCode() == 404) {
                    System.out.println("  Attempt " + attempt + ": collection fully deleted (404)");
                    return;
                }
                System.out
                    .println("  Attempt " + attempt + ": HTTP " + ex.getResponse().getStatusCode() + ", waiting...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("  Collection not fully deleted after " + maxWaitSeconds + "s — proceeding anyway");
    }
}
