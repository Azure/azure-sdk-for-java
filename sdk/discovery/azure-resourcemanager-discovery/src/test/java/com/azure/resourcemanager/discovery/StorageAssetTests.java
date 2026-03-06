// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.StorageAsset;
import com.azure.resourcemanager.discovery.models.StorageAssetProperties;
import com.azure.resourcemanager.discovery.fluent.models.StorageAssetInner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for StorageAsset operations against EUAP endpoint.
 *
 * StorageAssets are child resources of StorageContainers.
 * Java-specific names: test-sa-java01 under test-stc-java01.
 */
public class StorageAssetTests extends DiscoveryManagementTest {

    private static final String STORAGE_CONTAINER_RESOURCE_GROUP = "olawal";
    private static final String STORAGE_CONTAINER_NAME = "test-stc-java01";
    private static final String STORAGE_ASSET_NAME = "test-sa-java01";

    @Test
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
    public void testGetStorageAsset() {
        StorageAsset storageAsset = discoveryManager.storageAssets()
            .get(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME, STORAGE_ASSET_NAME);
        assertNotNull(storageAsset);
        assertNotNull(storageAsset.name());
        assertNotNull(storageAsset.id());
    }

    @Test
    public void testCreateStorageAsset() {
        StorageAssetProperties properties
            = new StorageAssetProperties().withDescription("Test storage asset for Java SDK validation")
                .withPath("data/test-assets");

        StorageAsset asset = discoveryManager.storageAssets()
            .define(STORAGE_ASSET_NAME)
            .withRegion("uksouth")
            .withExistingStorageContainer(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME)
            .withProperties(properties)
            .create();

        assertNotNull(asset);
        assertNotNull(asset.id());
        assertNotNull(asset.name());
    }

    @Test
    public void testUpdateStorageAsset() {
        // Use service client directly with a fresh inner model to avoid sending
        // read-only fields (location, path) in the PATCH body
        Map<String, String> tags = new HashMap<>();
        tags.put("SkipAutoDeleteTill", "2026-12-31");

        StorageAssetInner patchBody = new StorageAssetInner().withTags(tags);

        StorageAssetInner updated = discoveryManager.serviceClient()
            .getStorageAssets()
            .update(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME, STORAGE_ASSET_NAME, patchBody);

        assertNotNull(updated);
        assertNotNull(updated.id());
    }

    @Test
    public void testDeleteStorageAsset() {
        discoveryManager.storageAssets()
            .delete(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME, STORAGE_ASSET_NAME);
    }
}
