// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.AssetMetadata;
import com.azure.analytics.planetarycomputer.models.FileDetails;
import com.azure.analytics.planetarycomputer.models.StacAssetData;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Collection Lifecycle operations (Group 08b: Tests 04-06).
 */
@Tag("CollectionLifecycle")
public class TestPlanetaryComputer08bCollectionLifecycleTests extends PlanetaryComputerTestBase {

    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("CollectionAsset")
    public void test08_04_CreateCollectionAsset() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String assetId = "test-asset";

        System.out.println("Collection ID: " + collectionId);
        System.out.println("Asset ID: " + assetId);

        // Delete the asset if it already exists
        try {
            System.out.println("Checking if asset '" + assetId + "' already exists and deleting if found...");
            stacClient.deleteCollectionAsset(collectionId, assetId);
            System.out.println("Deleted existing '" + assetId + "'");
        } catch (Exception ex) {
            System.out.println("Asset '" + assetId + "' does not exist, proceeding with creation");
        }

        // Create asset data
        AssetMetadata metadata
            = new AssetMetadata(assetId, "text/plain", Arrays.asList("metadata"), "Test Asset", null);

        byte[] fileContent = "Test asset content".getBytes(StandardCharsets.UTF_8);
        FileDetails file = new FileDetails(BinaryData.fromBytes(fileContent)).setFilename("test-asset.txt")
            .setContentType("text/plain");

        StacAssetData assetData = new StacAssetData(metadata, file);

        System.out.println("Calling: createCollectionAsset('" + collectionId + "', {...})");

        StacCollection response = stacClient.createCollectionAsset(collectionId, assetData);

        assertNotNull(response);
        System.out.println("Asset '" + assetId + "' created successfully");
    }

    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("CollectionAsset")
    public void test08_05_ReplaceCollectionAsset() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String assetId = "test-asset";

        System.out.println("Collection ID: " + collectionId);
        System.out.println("Asset ID: " + assetId);

        // Update asset data
        AssetMetadata metadata
            = new AssetMetadata(assetId, "text/plain", Arrays.asList("metadata"), "Test Asset - Updated", null);

        byte[] fileContent = "Test asset content - updated".getBytes(StandardCharsets.UTF_8);
        FileDetails file = new FileDetails(BinaryData.fromBytes(fileContent)).setFilename("test-asset-updated.txt")
            .setContentType("text/plain");

        StacAssetData assetData = new StacAssetData(metadata, file);

        System.out.println("Calling: replaceCollectionAsset('" + collectionId + "', '" + assetId + "', {...})");

        StacCollection response = stacClient.replaceCollectionAsset(collectionId, assetId, assetData);

        assertNotNull(response);
        System.out.println("Asset '" + assetId + "' replaced successfully");
    }

    @Test
    @Disabled("Missing session recording - needs to be recorded")
    @Tag("CollectionAsset")
    public void test08_06_DeleteCollectionAsset() {
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();
        String assetId = "test-asset";

        System.out.println("Collection ID: " + collectionId);
        System.out.println("Asset ID: " + assetId);
        System.out.println("Calling: deleteCollectionAsset('" + collectionId + "', '" + assetId + "')");

        stacClient.deleteCollectionAsset(collectionId, assetId);

        System.out.println("Asset '" + assetId + "' deleted successfully");
    }
}
