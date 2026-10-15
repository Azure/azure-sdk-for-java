// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

/**
 * Mirrors {@code ProjectCollectionTests.cs} from the .NET source-of-truth.
 * The .NET method name carries a typo ({@code CrateGetExistTest}); the Java
 * port spells it {@code createGetExist}.
 */
public class ProjectCollectionTests extends StorageMoverManagementTestBase {

    @Test
    public void createGetExist() {
        String storageMoverName = generateRandomResourceName("stomover-", 24);
        StorageMover storageMover = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();
        Assertions.assertEquals(storageMoverName, storageMover.name());

        String projectName = generateRandomResourceName("project-", 24);
        Project created = storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .create();
        Assertions.assertEquals(projectName, created.name());
        Assertions.assertNull(created.description());
        Assertions.assertEquals("microsoft.storagemover/storagemovers/projects", created.type().toLowerCase());

        Project fetched = storageMoverManager.projects().get(resourceGroupName, storageMoverName, projectName);
        Assertions.assertEquals(projectName, fetched.name());
        Assertions.assertNull(fetched.description());
        Assertions.assertEquals("microsoft.storagemover/storagemovers/projects", fetched.type().toLowerCase());

        long count = StreamSupport
            .stream(storageMoverManager.projects().list(resourceGroupName, storageMoverName).spliterator(), false)
            .count();
        Assertions.assertTrue(count >= 1, "expected at least one project but found " + count);

        // Existence positive case — get must succeed.
        Assertions.assertEquals(projectName,
            storageMoverManager.projects().get(resourceGroupName, storageMoverName, projectName).name());
    }
}
