// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.StorageAsset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for StorageAsset operations against EUAP endpoint.
 * 
 * StorageAssets are child resources of StorageContainers.
 */
public class StorageAssetTests extends DiscoveryManagementTest {

    private static final String STORAGE_CONTAINER_RESOURCE_GROUP = "newapiversiontest";
    private static final String STORAGE_CONTAINER_NAME = "test-storage-container";
    private static final String STORAGE_ASSET_NAME = "test-storage-asset";

    @Test
    @Disabled("Requires existing storage container with assets")
    public void testListStorageAssetsByStorageContainer() {
        PagedIterable<StorageAsset> storageAssets = discoveryManager.storageAssets()
            .listByStorageContainer(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME);
        assertNotNull(storageAssets);

        List<StorageAsset> assetList = new ArrayList<>();
        for (StorageAsset asset : storageAssets) {
            assertNotNull(asset.name());
            assertNotNull(asset.id());
            assetList.add(asset);
        }

        assertNotNull(assetList);
    }

    @Test
    @Disabled("Requires existing storage container with asset")
    public void testGetStorageAsset() {
        StorageAsset storageAsset = discoveryManager.storageAssets()
            .get(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME, STORAGE_ASSET_NAME);
        assertNotNull(storageAsset);
        assertNotNull(storageAsset.name());
        assertNotNull(storageAsset.id());
    }

    @Test
    @Disabled("Create is a mutating operation - requires storage container setup")
    public void testCreateStorageAsset() {
        // StorageAsset creation requires a valid storage container and configuration
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Update is a mutating operation - requires existing storage asset")
    public void testUpdateStorageAsset() {
        // StorageAsset update requires an existing asset
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete is a mutating operation - requires existing storage asset")
    public void testDeleteStorageAsset() {
        // StorageAsset deletion requires an existing asset
        // This test is a placeholder for integration testing
    }
}
