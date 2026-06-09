// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Mirrors {@code StorageMoverCollectionTests.cs} from the .NET source-of-truth
 * test suite.
 */
public class StorageMoverCollectionTests extends StorageMoverManagementTestBase {

    @Test
    public void createUpdateGetExists() {
        String storageMoverName1 = generateRandomResourceName("testsm-", 24);
        String storageMoverName2 = generateRandomResourceName("testsm-", 24);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");

        StorageMover sm1 = storageMoverManager.storageMovers()
            .define(storageMoverName1)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .withTags(tags)
            .withDescription("This is a new storage mover")
            .create();
        Assertions.assertEquals(storageMoverName1, sm1.name());
        Assertions.assertEquals("value1", sm1.tags().get("tag1"));
        Assertions.assertEquals("This is a new storage mover", sm1.description());

        StorageMover sm2 = storageMoverManager.storageMovers()
            .define(storageMoverName2)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .withTags(tags)
            .withDescription("This is a new storage mover")
            .create();
        Assertions.assertEquals(storageMoverName2, sm2.name());

        StorageMover fetched
            = storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, storageMoverName1);
        Assertions.assertEquals(storageMoverName1, fetched.name());
        Assertions.assertEquals("value1", fetched.tags().get("tag1"));
        Assertions.assertEquals("This is a new storage mover", fetched.description());

        long count = StreamSupport
            .stream(storageMoverManager.storageMovers().listByResourceGroup(resourceGroupName).spliterator(), false)
            .count();
        Assertions.assertEquals(2L, count);

        StorageMover updated = fetched.update().withDescription("This is an updated storage mover").apply();
        Assertions.assertEquals(storageMoverName1, updated.name());
        Assertions.assertEquals("value1", updated.tags().get("tag1"));
        Assertions.assertEquals("This is an updated storage mover", updated.description());

        // Existence check — Java SDK has no Exists helper, so we verify presence
        // via getByResourceGroup and absence via assertNotFound (404 wrapped in
        // ManagementException).
        Assertions.assertEquals(storageMoverName1,
            storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, storageMoverName1).name());
        assertNotFound(
            () -> storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, storageMoverName1 + "111"));
    }

    @SuppressWarnings("unused")
    private static <T> long count(Iterable<T> iterable) {
        // Helper intentionally left in case future tests want to count without
        // depending on Stream — avoids importing StreamSupport everywhere.
        return StreamSupport.stream(iterable.spliterator(), false).count();
    }

    @SuppressWarnings("unused")
    private static Map<String, String> emptyTags() {
        return Collections.emptyMap();
    }
}
