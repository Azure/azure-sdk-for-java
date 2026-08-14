// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for STAC Queryable CRUD operations (Group 01f).
 * Covers: createQueryables, replaceQueryable, deleteQueryable.
 * Python equivalent: planetary_computer_00_stac_collection.py (manage_queryables)
 * JS equivalent: 00_stacCollection.spec.ts (should create, replace, and delete a queryable)
 */
@Tag("STAC")
@Tag("Queryables")
public class TestPlanetaryComputer01fStacQueryableTests extends PlanetaryComputerTestBase {

    private static final String TEST_QUERYABLE_NAME = "test:cloud_cover_java";

    /**
     * Safely try to delete a test queryable (ignore if not found).
     */
    private void tryDeleteQueryable(StacClient stacClient, String collectionId, String queryableName) {
        try {
            stacClient.deleteQueryable(collectionId, queryableName);
        } catch (Exception ex) {
            // Queryable may not exist
        }
    }

    /**
     * Test creating, replacing, and deleting a queryable.
     * Python equivalent: manage_queryables
     * JS equivalent: should create, replace, and delete a queryable
     */
    @Test
    @Tag("QueryableCRUD")
    public void test01_18_CreateReplaceDeleteQueryable() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Collection: " + collectionId);
        System.out.println("Queryable name: " + TEST_QUERYABLE_NAME);

        // Clean up if queryable exists
        tryDeleteQueryable(stacClient, collectionId, TEST_QUERYABLE_NAME);

        // Step 1: Create queryable - use protocol method for precise JSON control
        String createBody = "[{\"name\":\"" + TEST_QUERYABLE_NAME
            + "\",\"definition\":{\"type\":\"number\"},\"data_type\":\"number\",\"create_index\":false}]";

        System.out.println("Calling: createQueryables(...)");
        com.azure.core.http.rest.Response<BinaryData> createResponse = stacClient.createQueryablesWithResponse(
            collectionId, BinaryData.fromString(createBody), new com.azure.core.http.rest.RequestOptions());

        assertNotNull(createResponse, "Create response should not be null");
        assertTrue(createResponse.getStatusCode() >= 200 && createResponse.getStatusCode() < 300);
        System.out.println("Queryable(s) created");

        // Step 2: Replace queryable with description - use protocol method
        String replaceBody = "{\"name\":\"" + TEST_QUERYABLE_NAME
            + "\",\"definition\":{\"type\":\"number\",\"description\":\"Cloud cover percentage\"},\"data_type\":\"number\",\"create_index\":false}";

        System.out.println("Calling: replaceQueryable(...)");
        com.azure.core.http.rest.Response<BinaryData> replaceResponse
            = stacClient.replaceQueryableWithResponse(collectionId, TEST_QUERYABLE_NAME,
                BinaryData.fromString(replaceBody), new com.azure.core.http.rest.RequestOptions());

        assertNotNull(replaceResponse, "Replace response should not be null");
        assertTrue(replaceResponse.getStatusCode() >= 200 && replaceResponse.getStatusCode() < 300);
        System.out.println("Queryable replaced successfully");

        // Step 3: Delete queryable
        System.out.println("Calling: deleteQueryable(...)");
        stacClient.deleteQueryable(collectionId, TEST_QUERYABLE_NAME);

        // Verify deletion - queryable should no longer appear in collection queryables
        Response<BinaryData> queryablesResponse
            = stacClient.getCollectionQueryablesWithResponse(collectionId, new RequestOptions());
        String responseJson = queryablesResponse.getValue().toString();
        assertFalse(responseJson.contains(TEST_QUERYABLE_NAME),
            "Deleted queryable should not appear in collection queryables");

        System.out.println("Queryable '" + TEST_QUERYABLE_NAME + "' deleted and verified successfully");
    }
}
