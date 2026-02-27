// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.StorageContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for StorageContainer operations against EUAP endpoint.
 * 
 * StorageContainers are top-level resources under ResourceGroup.
 */
public class StorageContainerTests extends DiscoveryManagementTest {

    private static final String STORAGE_CONTAINER_RESOURCE_GROUP = "newapiversiontest";
    private static final String STORAGE_CONTAINER_NAME = "test-storage-container";

    @Test
    @Disabled("Backend may not have storage containers in test subscription")
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
    @Disabled("Backend may not have storage containers in test resource group")
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
    @Disabled("Requires existing storage container")
    public void testGetStorageContainer() {
        StorageContainer storageContainer = discoveryManager.storageContainers()
            .getByResourceGroup(STORAGE_CONTAINER_RESOURCE_GROUP, STORAGE_CONTAINER_NAME);
        assertNotNull(storageContainer);
        assertNotNull(storageContainer.name());
        assertNotNull(storageContainer.id());
    }

    @Test
    @Disabled("Create is a mutating operation - requires proper storage setup")
    public void testCreateStorageContainer() {
        // StorageContainer creation requires proper storage configuration
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Update is a mutating operation - requires existing storage container")
    public void testUpdateStorageContainer() {
        // StorageContainer update requires an existing container
        // This test is a placeholder for integration testing
    }

    @Test
    @Disabled("Delete is a mutating operation - requires existing storage container")
    public void testDeleteStorageContainer() {
        // StorageContainer deletion requires an existing container
        // This test is a placeholder for integration testing
    }
}
