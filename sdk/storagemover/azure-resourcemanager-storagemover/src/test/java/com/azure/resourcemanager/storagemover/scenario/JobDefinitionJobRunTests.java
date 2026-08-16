// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.NfsMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

/**
 * Mirrors {@code JobDefinitionJobRunTests.cs} from the .NET source-of-truth.
 *
 * <p>Self-provisions storage mover, project, source endpoint (NFS) and target
 * endpoint (Blob), creates a job definition, and verifies CRUD behaviour. The
 * final {@code startJob}/{@code stopJob} calls are expected to fail with a
 * {@link ManagementException} because no agent is registered against the
 * storage mover — see the cross-language playbook.
 */
public class JobDefinitionJobRunTests extends StorageMoverManagementTestBase {

    @Test
    public void jobDefinitionJobRun() {
        String storageMoverName = generateRandomResourceName("stomover-", 24);
        String projectName = generateRandomResourceName("project-", 24);
        String sourceEndpointName = generateRandomResourceName("nfsep-", 24);
        String targetEndpointName = generateRandomResourceName("blobep-", 24);
        String jobDefinitionName = generateRandomResourceName("jobdef-", 24);

        StorageMover sm = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        Project project = storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .create();

        Endpoint nfsEndpoint = storageMoverManager.endpoints()
            .define(sourceEndpointName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1").withExport("/"))
            .create();

        Endpoint blobEndpoint
            = storageMoverManager.endpoints()
                .define(targetEndpointName)
                .withExistingStorageMover(resourceGroupName, sm.name())
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer"))
                .create();

        JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
            .define(jobDefinitionName)
            .withExistingProject(resourceGroupName, sm.name(), project.name())
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName(nfsEndpoint.name())
            .withTargetName(blobEndpoint.name())
            .create();
        Assertions.assertEquals(jobDefinitionName, jobDefinition.name());
        Assertions.assertEquals(blobEndpoint.name(), jobDefinition.targetName());
        Assertions.assertEquals(nfsEndpoint.name(), jobDefinition.sourceName());
        Assertions.assertEquals(CopyMode.ADDITIVE, jobDefinition.copyMode());

        JobDefinition fetched
            = storageMoverManager.jobDefinitions().get(resourceGroupName, sm.name(), project.name(), jobDefinitionName);
        Assertions.assertEquals(jobDefinitionName, fetched.name());

        long count = StreamSupport.stream(
            storageMoverManager.jobDefinitions().list(resourceGroupName, sm.name(), project.name()).spliterator(),
            false).count();
        Assertions.assertTrue(count >= 1, "expected at least one job definition but found " + count);

        // Equivalence between collection-get and resource-self-refresh.
        JobDefinition refreshed = jobDefinition.refresh();
        Assertions.assertEquals(jobDefinition.name(), refreshed.name());
        Assertions.assertEquals(jobDefinition.targetName(), refreshed.targetName());
        Assertions.assertEquals(jobDefinition.agentName(), refreshed.agentName());
        Assertions.assertEquals(jobDefinition.sourceName(), refreshed.sourceName());
        Assertions.assertEquals(jobDefinition.id(), refreshed.id());

        // StartJob / StopJob require a registered agent — without one the RP
        // returns a 4xx wrapped in ManagementException.
        Assertions.assertThrows(ManagementException.class, jobDefinition::startJob);
        Assertions.assertThrows(ManagementException.class, jobDefinition::stopJob);
    }
}
