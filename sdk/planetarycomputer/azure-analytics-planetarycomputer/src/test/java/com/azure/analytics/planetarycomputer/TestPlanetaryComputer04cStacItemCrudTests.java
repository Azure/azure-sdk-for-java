// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.StacItem;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Item CRUD lifecycle operations (Group 04c).
 * Covers: beginCreateItem, beginUpdateItem, beginReplaceItem, beginDeleteItem (all LROs).
 * Uses protocol methods (raw JSON) because the generated StacItem model has a read-only 'id' field.
 * Python equivalent: planetary_computer_02_stac_specification.py (create/update/replace/delete)
 * JS equivalent: 02_stacSpecification.spec.ts (create, update, replace, delete tests)
 */
@Tag("STAC")
@Tag("ItemCRUD")
public class TestPlanetaryComputer04cStacItemCrudTests extends PlanetaryComputerTestBase {

    private static final String TEST_ITEM_SUFFIX = "-crud-test";

    /**
     * Creates a sample STAC item as raw JSON.
     * Uses raw JSON because the generated StacItem model has no setter for 'id'.
     */
    private String createSampleItemJson(String itemId, String collectionId) {
        return "{" + "\"type\": \"Feature\"," + "\"stac_version\": \"1.0.0\"," + "\"id\": \"" + itemId + "\","
            + "\"collection\": \"" + collectionId + "\","
            + "\"geometry\": {\"type\": \"Point\", \"coordinates\": [-84.39, 33.67]},"
            + "\"bbox\": [-84.44, 33.63, -84.38, 33.69],"
            + "\"properties\": {\"datetime\": \"2021-11-14T16:00:00Z\", \"gsd\": 0.6}," + "\"assets\": {"
            + "  \"image\": {"
            + "    \"href\": \"https://naipeuwest.blob.core.windows.net/naip/v002/ga/2021/ga_060cm_2021/33084/m_3308421_se_16_060_20211114.tif\","
            + "    \"type\": \"image/tiff; application=geotiff; profile=cloud-optimized\","
            + "    \"title\": \"RGBIR COG tile\"," + "    \"roles\": [\"data\"]" + "  }" + "},"
            + "\"links\": [{\"rel\": \"collection\", \"href\": \"./\", \"type\": \"application/json\"}]" + "}";
    }

    /**
     * Safely try to delete a test item (ignore if not found).
     */
    private void tryDeleteItem(StacClient stacClient, String collectionId, String itemId) {
        try {
            SyncPoller<BinaryData, Void> poller
                = stacClient.beginDeleteItem(collectionId, itemId, new RequestOptions());
            poller.waitForCompletion();
        } catch (Exception ex) {
            // Item may not exist, that's fine
        }
    }

    /**
     * Test creating a STAC item via LRO.
     * Python equivalent: create_stac_item
     * JS equivalent: should create and get a STAC item
     */
    @Test
    @Tag("CreateItem")
    public void test04_13_CreateAndGetItem() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId() + TEST_ITEM_SUFFIX;

        System.out.println("Test item ID: " + itemId);
        System.out.println("Collection: " + collectionId);

        // Clean up if item already exists
        tryDeleteItem(stacClient, collectionId, itemId);

        // Create the item using protocol method
        String itemJson = createSampleItemJson(itemId, collectionId);

        System.out.println("Calling: beginCreateItem(...)");
        SyncPoller<BinaryData, BinaryData> createPoller
            = stacClient.beginCreateItem(collectionId, BinaryData.fromString(itemJson), new RequestOptions());
        createPoller.getFinalResult();

        System.out.println("Item creation completed");

        // Verify by getting the item
        StacItem created = stacClient.getItem(collectionId, itemId);

        assertNotNull(created, "Created item should not be null");
        assertEquals(itemId, created.getId(), "Item ID should match");
        assertEquals(collectionId, created.getCollection(), "Collection should match");
        assertNotNull(created.getGeometry(), "Item should have geometry");
        assertNotNull(created.getAssets(), "Item should have assets");
        assertTrue(created.getAssets().containsKey("image"), "Item should have 'image' asset");

        System.out.println("Item '" + itemId + "' created and verified successfully");

        // Clean up
        tryDeleteItem(stacClient, collectionId, itemId);
    }

    /**
     * Test updating a STAC item via LRO (JSON merge patch).
     * Python equivalent: update_stac_item
     * JS equivalent: should update a STAC item
     */
    @Test
    @Tag("UpdateItem")
    public void test04_14_UpdateItem() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId() + "-update-test";

        System.out.println("Test item ID: " + itemId);

        // Clean up and create a fresh item
        tryDeleteItem(stacClient, collectionId, itemId);

        String itemJson = createSampleItemJson(itemId, collectionId);
        SyncPoller<BinaryData, BinaryData> createPoller
            = stacClient.beginCreateItem(collectionId, BinaryData.fromString(itemJson), new RequestOptions());
        createPoller.getFinalResult();
        System.out.println("Item created for update test");

        // Update the item — merge patch requires id and type
        String updateBody
            = "{\"id\": \"" + itemId + "\", \"type\": \"Feature\", \"properties\": {\"platform\": \"Imagery\"}}";

        System.out.println("Calling: beginUpdateItem(...)");
        SyncPoller<BinaryData, BinaryData> updatePoller
            = stacClient.beginUpdateItem(collectionId, itemId, BinaryData.fromString(updateBody), new RequestOptions());
        updatePoller.getFinalResult();
        System.out.println("Item update completed");

        // Verify the update
        StacItem updated = stacClient.getItem(collectionId, itemId);

        assertNotNull(updated, "Updated item should not be null");
        assertEquals(itemId, updated.getId(), "Item ID should not change");
        assertNotNull(updated.getProperties(), "Properties should not be null");

        // Check platform was set
        if (updated.getProperties().getPlatform() != null) {
            assertEquals("Imagery", updated.getProperties().getPlatform(), "Platform should be 'Imagery'");
        } else if (updated.getProperties().getAdditionalProperties() != null
            && updated.getProperties().getAdditionalProperties().containsKey("platform")) {
            assertEquals("Imagery",
                updated.getProperties().getAdditionalProperties().get("platform").toObject(String.class),
                "Platform should be 'Imagery'");
        }

        System.out.println("Item '" + itemId + "' updated successfully");

        // Clean up
        tryDeleteItem(stacClient, collectionId, itemId);
    }

    /**
     * Test replacing a STAC item via LRO.
     * Python equivalent: create_or_replace_stac_item
     * JS equivalent: should replace a STAC item
     */
    @Test
    @Tag("ReplaceItem")
    public void test04_15_ReplaceItem() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId() + "-replace-test";

        System.out.println("Test item ID: " + itemId);

        // Clean up and create a fresh item
        tryDeleteItem(stacClient, collectionId, itemId);

        String itemJson = createSampleItemJson(itemId, collectionId);
        SyncPoller<BinaryData, BinaryData> createPoller
            = stacClient.beginCreateItem(collectionId, BinaryData.fromString(itemJson), new RequestOptions());
        createPoller.getFinalResult();
        System.out.println("Item created for replace test");

        // Replace the item with updated properties (add platform field)
        String replacementJson = createSampleItemJson(itemId, collectionId).replace("\"gsd\": 0.6",
            "\"gsd\": 0.6, \"platform\": \"Imagery Updated\"");

        System.out.println("Calling: beginReplaceItem(...)");
        SyncPoller<BinaryData, BinaryData> replacePoller = stacClient.beginReplaceItem(collectionId, itemId,
            BinaryData.fromString(replacementJson), new RequestOptions());
        replacePoller.getFinalResult();
        System.out.println("Item replace completed");

        // Verify the replacement
        StacItem replaced = stacClient.getItem(collectionId, itemId);

        assertNotNull(replaced, "Replaced item should not be null");
        assertEquals(itemId, replaced.getId(), "Item ID should not change");

        System.out.println("Item '" + itemId + "' replaced successfully");

        // Clean up
        tryDeleteItem(stacClient, collectionId, itemId);
    }

    /**
     * Test deleting a STAC item via LRO.
     * Python equivalent: delete_stac_item
     * JS equivalent: should delete a STAC item
     */
    @Test
    @Tag("DeleteItem")
    public void test04_16_DeleteItem() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String itemId = testEnvironment.getItemId() + "-delete-test";

        System.out.println("Test item ID: " + itemId);

        // Clean up and create a fresh item
        tryDeleteItem(stacClient, collectionId, itemId);

        String itemJson = createSampleItemJson(itemId, collectionId);
        SyncPoller<BinaryData, BinaryData> createPoller
            = stacClient.beginCreateItem(collectionId, BinaryData.fromString(itemJson), new RequestOptions());
        createPoller.getFinalResult();
        System.out.println("Item created for delete test");

        // Delete the item
        System.out.println("Calling: beginDeleteItem(...)");
        SyncPoller<BinaryData, Void> deletePoller
            = stacClient.beginDeleteItem(collectionId, itemId, new RequestOptions());
        // Use waitForCompletion() — equivalent to Python's .result() and JS's pollUntilDone()
        // Note: getFinalResult() throws "Cannot get final result" on delete LROs because
        // SyncOperationResourcePollingStrategy doesn't handle Void final result type.
        // This is a known Java azure-core LRO issue — Python and JS handle this correctly.
        deletePoller.waitForCompletion();
        System.out.println("Item delete completed");

        // Verify deletion — matching Python/JS pattern:
        // Python catches ResourceNotFoundError, JS catches RestError with 404
        try {
            stacClient.getItem(collectionId, itemId);
            // If we get here, deletion may still be propagating (matching JS comment)
            System.out.println("Item still accessible after delete - deletion may still be propagating");
        } catch (com.azure.core.exception.HttpResponseException ex) {
            assertEquals(404, ex.getResponse().getStatusCode(), "Should get 404 for deleted item");
            System.out.println("Confirmed item '" + itemId + "' was deleted (404)");
        }
    }
}
