// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.NfsMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

/**
 * Mirrors {@code JobRunTests.cs} from the .NET source-of-truth.
 *
 * <p>Without a registered agent no JobRun ever materializes against a job
 * definition, so the .NET test's "get an existing job run" assertion is
 * impossible to satisfy hermetically. Per the cross-language playbook (Python's
 * final state) this collapses to two assertions on a freshly created job
 * definition: the job-run list is empty AND a get of an arbitrary jobName
 * returns 404.
 */
public class JobRunTests extends StorageMoverManagementTestBase {

    @Test
    public void getExist() {
        String storageMoverName = generateRandomResourceName("stomover-jr-", 24);
        String projectName = generateRandomResourceName("project-jr-", 24);
        String sourceEndpointName = generateRandomResourceName("nfsep-", 24);
        String targetEndpointName = generateRandomResourceName("blobep-", 24);
        String jobDefinitionName = generateRandomResourceName("jobdef-jr-", 24);
        // Constant from the .NET base class — any well-formed name works since
        // we only use it for the negative get-by-name path.
        String missingJobName = "6e8c0dfe-821a-427d-8d11-a9ed7f1c9c13";

        StorageMover sm = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();
        storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .create();
        storageMoverManager.endpoints()
            .define(sourceEndpointName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1").withExport("/"))
            .create();
        storageMoverManager.endpoints()
            .define(targetEndpointName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .withProperties(
                new AzureStorageBlobContainerEndpointProperties().withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer"))
            .create();
        JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
            .define(jobDefinitionName)
            .withExistingProject(resourceGroupName, sm.name(), projectName)
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName(sourceEndpointName)
            .withTargetName(targetEndpointName)
            .create();
        Assertions.assertNotNull(jobDefinition);

        long count = StreamSupport.stream(storageMoverManager.jobRuns()
            .list(resourceGroupName, sm.name(), projectName, jobDefinitionName)
            .spliterator(), false).count();
        Assertions.assertEquals(0L, count, "no JobRun should exist without a registered agent");

        assertNotFound(() -> storageMoverManager.jobRuns()
            .get(resourceGroupName, sm.name(), projectName, jobDefinitionName, missingJobName));
    }
}
