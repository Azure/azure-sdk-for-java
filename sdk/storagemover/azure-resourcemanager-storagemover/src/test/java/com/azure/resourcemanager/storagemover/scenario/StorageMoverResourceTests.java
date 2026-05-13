// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.EndpointType;
import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mirrors {@code StorageMoverResourceTests.cs} from the .NET source-of-truth
 * test suite.
 */
public class StorageMoverResourceTests extends StorageMoverManagementTestBase {

    @Test
    public void getStorageMover() {
        String name = generateRandomResourceName("testsm-get-", 24);

        Map<String, String> tags = new HashMap<>();
        tags.put("k", "v");

        StorageMover created = storageMoverManager.storageMovers()
            .define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .withTags(tags)
            .create();

        StorageMover fetched1 = storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, name);
        StorageMover fetched2 = storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, name);

        Assertions.assertEquals(name, fetched1.name());
        Assertions.assertEquals(fetched1.name(), fetched2.name());
        Assertions.assertEquals(fetched1.id(), fetched2.id());
        Assertions.assertEquals(fetched1.location(), fetched2.location());
        Assertions.assertEquals(fetched1.type(), fetched2.type());
        Assertions.assertEquals(fetched1.tags(), fetched2.tags());
        Assertions.assertEquals(created.id(), fetched1.id());
    }

    /**
     * Skipped: agents cannot be created by the RP; this scenario requires a real
     * registered agent VM — see the cross-language playbook.
     */
    @Test
    @Disabled("Agents cannot be created by the RP; this test requires a registered agent VM.")
    public void getStorageMoverAgent() {
    }

    @Test
    public void getStorageMoverEndpoint() {
        String storageMoverName = generateRandomResourceName("testsm-getep-", 24);
        String endpointName = generateRandomResourceName("blobep-", 24);

        storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        AzureStorageBlobContainerEndpointProperties props
            = new AzureStorageBlobContainerEndpointProperties().withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                .withBlobContainerName("testcontainer");

        storageMoverManager.endpoints()
            .define(endpointName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .withProperties(props)
            .create();

        Endpoint fetched = storageMoverManager.endpoints().get(resourceGroupName, storageMoverName, endpointName);
        Assertions.assertEquals(endpointName, fetched.name());
        Assertions.assertEquals(EndpointType.AZURE_STORAGE_BLOB_CONTAINER, fetched.properties().endpointType());
    }

    @Test
    public void getStorageMoverProject() {
        String storageMoverName = generateRandomResourceName("testsm-getproj-", 24);
        String projectName = generateRandomResourceName("project-", 24);

        storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, storageMoverName)
            .create();

        Project fetched = storageMoverManager.projects().get(resourceGroupName, storageMoverName, projectName);
        Assertions.assertEquals(projectName, fetched.name());
    }

    @Test
    public void updateAddSetRemoveTagDelete() {
        String name = generateRandomResourceName("testsm-tags-", 24);

        // Step 1: create with no tags.
        StorageMover sm = storageMoverManager.storageMovers()
            .define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();
        Assertions.assertEquals(name, sm.name());
        Assertions.assertEquals(DEFAULT_REGION.name(), sm.location());

        // Step 2: PATCH description (mirrors .NET UpdateAsync(patch{Description = ...})).
        sm = sm.refresh().update().withDescription("This is an updated storage mover").apply();
        Assertions.assertEquals("This is an updated storage mover", sm.description());

        // Step 3: AddTag — replace tag set with {tag1: val1}. Java SDK has no
        // dedicated AddTag helper, so use the same fluent update.apply() pattern.
        Map<String, String> tagsAdd = new HashMap<>();
        tagsAdd.put("tag1", "val1");
        sm = sm.update().withTags(tagsAdd).apply();
        Assertions.assertEquals(1, sm.tags().size());
        Assertions.assertEquals("val1", sm.tags().get("tag1"));

        // Step 4: SetTags — replace tag set with {tag2: val2, tag3: val3}.
        Map<String, String> tagsSet = new HashMap<>();
        tagsSet.put("tag2", "val2");
        tagsSet.put("tag3", "val3");
        sm = sm.update().withTags(tagsSet).apply();
        Assertions.assertEquals(2, sm.tags().size());

        // Step 5: RemoveTag — replace tag set with the remaining {tag3: val3}.
        Map<String, String> tagsAfterRemove = Collections.singletonMap("tag3", "val3");
        sm = sm.update().withTags(tagsAfterRemove).apply();
        Assertions.assertEquals(1, sm.tags().size());

        // Step 6: Delete and confirm 404 on subsequent get.
        storageMoverManager.storageMovers().deleteById(sm.id());
        assertNotFound(() -> storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, name));
    }
}
