// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Mirrors {@code ProjectResourceTests.cs} from the .NET source-of-truth.
 */
public class ProjectResourceTests extends StorageMoverManagementTestBase {

    @Test
    public void getUpdateDelete() {
        String storageMoverName = generateRandomResourceName("stomover-", 24);
        StorageMover storageMover = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        String projectName = generateRandomResourceName("project-", 24);
        Project created = storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, storageMover.name())
            .create();

        Project fetched = storageMoverManager.projects().get(resourceGroupName, storageMover.name(), projectName);
        Assertions.assertEquals(created.name(), fetched.name());
        Assertions.assertEquals(created.description(), fetched.description());
        Assertions.assertEquals(created.id(), fetched.id());

        Project updated = fetched.update().withDescription("This is an updated project").apply();
        Assertions.assertEquals("This is an updated project", updated.description());

        storageMoverManager.projects().delete(resourceGroupName, storageMover.name(), projectName);
        assertNotFound(() -> storageMoverManager.projects().get(resourceGroupName, storageMover.name(), projectName));
    }
}
