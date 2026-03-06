// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.AzureStorageBlobStore;
import com.azure.resourcemanager.discovery.models.StorageContainer;
import com.azure.resourcemanager.discovery.models.StorageContainerProperties;
import com.azure.resourcemanager.discovery.fluent.models.StorageContainerInner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for StorageContainer operations against EUAP endpoint.
 *
 * Tests match the comprehensive coverage in Python SDK.
 * Java-specific resource name: test-stc-java01 (different from Python's test-sc-8bef0d1a).
 */
public class StorageContainerTests extends DiscoveryManagementTest {

    private static final String STORAGE_CONTAINER_RESOURCE_GROUP = "olawal";
    private static final String STORAGE_CONTAINER_NAME = "test-stc-java01";
    private static final String SUBSCRIPTION_ID = "31b0b6a5-2647-47eb-8a38-7d12047ee8ec";

    @Test
    public void testListStorageContainersBySubscription() {
        PagedIterable<StorageContainer> storageContainers = discoveryManager.storageContainers().list();
        assertNotNull(storageContainers);

        List<StorageContainer> containerList = new ArrayList<>();
        for (StorageContainer container : storageContainers) {
            assertNotNull(container.name());
            assertNotNull(container.id());
            containerList.add(container);
        }

        assertNotNull(containerList);
    }

    @Test
    public void testListStorageContainersByResourceGroup() {
        PagedIterable<StorageContainer> storageContainers
            = discoveryManager.storageContainers().listByResourceGroup(STORAGE_CONTAINER_RESOURCE_GROUP);
        assertNotNull(storageContainers);

        List<StorageContainer> containerList = new ArrayList<>();
        for (StorageContainer container : storageContainers) {
            assertNotNull(container.name());
            assertNotNull(container.id());
            containerList.add(container);
        }

        assertNotNull(containerList);
    }

    @Test
    public void testGetStorageContainer() {
        StorageContainer storageContainer = discoveryManager.storageContainers()
            .getByResourceGroup(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME);
        assertNotNull(storageContainer);
        assertNotNull(storageContainer.name());
        assertNotNull(storageContainer.id());
    }

    @Test
    public void testCreateStorageContainer() {
        StorageContainerProperties properties = new StorageContainerProperties()
            .withStorageStore(new AzureStorageBlobStore().withStorageAccountId("/subscriptions/" + SUBSCRIPTION_ID
                + "/resourceGroups/olawal/providers/Microsoft.Storage/storageAccounts/mytststr"));

        StorageContainer container = discoveryManager.storageContainers()
            .define(STORAGE_CONTAINER_NAME)
            .withRegion("uksouth")
            .withExistingResourceGroup(STORAGE_CONTAINER_RESOURCE_GROUP)
            .withProperties(properties)
            .create();

        assertNotNull(container);
        assertNotNull(container.id());
        assertNotNull(container.name());
    }

    @Test
    public void testUpdateStorageContainer() {
        // Use service client directly with a fresh inner model to avoid sending
        // read-only fields (location) in the PATCH body
        Map<String, String> tags = new HashMap<>();
        tags.put("SkipAutoDeleteTill", "2026-12-31");

        StorageContainerInner patchBody = new StorageContainerInner().withTags(tags);

        StorageContainerInner updated = discoveryManager.serviceClient()
            .getStorageContainers()
            .update(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME, patchBody);

        assertNotNull(updated);
        assertNotNull(updated.id());
    }

    @Test
    public void testDeleteStorageContainer() {
        discoveryManager.storageContainers()
            .deleteByResourceGroup(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME);
    }
}
