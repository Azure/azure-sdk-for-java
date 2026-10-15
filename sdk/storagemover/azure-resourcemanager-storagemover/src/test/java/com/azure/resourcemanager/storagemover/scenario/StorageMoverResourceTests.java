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

        // Step 1: create with no explicit tags. (Note: some subscriptions apply
        // default tags via Azure Policy, so the resource may already have one
        // or more system-managed tags at this point. Tests should not assume a
        // specific tag-set size; they only assert that the tags WE manage are
        // present or absent as expected.)
        StorageMover sm = storageMoverManager.storageMovers()
            .define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();
        Assertions.assertEquals(name, sm.name());
        Assertions.assertEquals(DEFAULT_REGION.name(), sm.location());

        // Step 2: PATCH description.
        sm = sm.refresh().update().withDescription("This is an updated storage mover").apply();
        Assertions.assertEquals("This is an updated storage mover", sm.description());

        // Step 3: PATCH-set tag1. The Storage Mover RP's PATCH on the tags
        // field REPLACES user-supplied tags (matching .NET's SetTagsAsync
        // semantic), so any previously-set user tags are dropped. System or
        // policy-applied tags, if any, are preserved by the RP across writes.
        sm = sm.update().withTags(Collections.singletonMap("tag1", "val1")).apply();
        Assertions.assertEquals("val1", sm.tags().get("tag1"));

        // Step 4: PATCH-set tag2 — REPLACES tag1 with tag2 (user tags only;
        // policy tags persist).
        sm = sm.update().withTags(Collections.singletonMap("tag2", "val2")).apply();
        Assertions.assertEquals("val2", sm.tags().get("tag2"));
        Assertions.assertFalse(sm.tags().containsKey("tag1"), "PATCH should replace user tag1 with tag2");

        // Step 5: PUT-replace via idempotent define. End state: only tag3 of
        // user-supplied tags is present.
        sm = storageMoverManager.storageMovers()
            .define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .withTags(Collections.singletonMap("tag3", "val3"))
            .create();
        Assertions.assertEquals("val3", sm.tags().get("tag3"));
        Assertions.assertFalse(sm.tags().containsKey("tag1"), "PUT should not retain previously-PATCHed tag1");
        Assertions.assertFalse(sm.tags().containsKey("tag2"), "PUT should not retain previously-PATCHed tag2");

        // Step 6: Delete and confirm 404 on subsequent get.
        storageMoverManager.storageMovers().deleteById(sm.id());
        assertNotFound(() -> storageMoverManager.storageMovers().getByResourceGroup(resourceGroupName, name));
    }
}
