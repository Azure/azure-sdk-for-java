// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageBlobContainersTests extends StorageManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateBlobContainer() {
        String saName = generateRandomResourceName("javacmsa", 15);
        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("a", "b");
        metadataTest.put("c", "d");

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer =
            blobContainers
                .defineContainer("blob-test")
                .withExistingStorageAccount(rgName, saName)
                .withPublicAccess(PublicAccess.CONTAINER)
                .withMetadata("a", "b")
                .withMetadata("c", "d")
                .create();

        Assertions.assertEquals("blob-test", blobContainer.name());
        Assertions.assertEquals(PublicAccess.CONTAINER, blobContainer.publicAccess());
        Assertions.assertEquals(metadataTest, blobContainer.metadata());
    }

    @Test
    public void canUpdateBlobContainer() {
        String saName = generateRandomResourceName("javacmsa", 15);

        Map<String, String> metadataInitial = new HashMap<String, String>();
        metadataInitial.put("a", "b");

        Map<String, String> metadataTest = new HashMap<String, String>();
        metadataTest.put("c", "d");
        metadataTest.put("e", "f");

        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer =
            blobContainers
                .defineContainer("blob-test")
                .withExistingStorageAccount(rgName, saName)
                .withPublicAccess(PublicAccess.CONTAINER)
                .withMetadata(metadataInitial)
                .create();

        blobContainer
            .update()
            .withPublicAccess(PublicAccess.BLOB)
            .withMetadata("c", "d")
            .withMetadata("e", "f")
            .apply();

        Assertions.assertEquals("blob-test", blobContainer.name());
        Assertions.assertEquals(PublicAccess.BLOB, blobContainer.publicAccess());
        Assertions.assertEquals(metadataTest, blobContainer.metadata());
    }

    @Test
    @Disabled("Need to set env CLI_USERNAME for CLI authentication, e.g. johndoe@microsoft.com")
    public void canShareHttpPipelineInDataPlane() {
        String userName = Configuration.getGlobalConfiguration().get("CLI_USERNAME");
        Assertions.assertNotNull(userName);

        String saName = generateRandomResourceName("javacmsa", 15);
        String containerName = "blob-test";
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(saName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgName)
                .create();

        BlobContainers blobContainers = this.storageManager.blobContainers();
        BlobContainer blobContainer =
            blobContainers
                .defineContainer(containerName)
                .withExistingStorageAccount(rgName, saName)
                .withPublicAccess(PublicAccess.CONTAINER)
                .create();

        // assign data-plane blob role
        RoleAssignment roleAssignment = msiManager.authorizationManager().roleAssignments()
            .define(UUID.randomUUID().toString())
            .forUser(userName)
            .withBuiltInRole(BuiltInRole.STORAGE_BLOB_DATA_CONTRIBUTOR)
            .withScope(blobContainer.id())
            .create();

        // let the role assignment propagate
        msiManager.authorizationManager().roleAssignments().getByIdAsync(roleAssignment.id())
            .retryWhen(Retry
                // 10 + 20 = 30 seconds
                .backoff(2, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
                .filter(throwable -> {
                    boolean resourceNotFoundException = false;
                    if (throwable instanceof ManagementException) {
                        ManagementException exception = (ManagementException) throwable;
                        if (exception.getResponse().getStatusCode() == 404) {
                            resourceNotFoundException = true;
                        }
                    }
                    return resourceNotFoundException;
                })
                // do not convert to RetryExhaustedException
                .onRetryExhaustedThrow((spec, signal) -> signal.failure()));

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .pipeline(storageManager.httpPipeline())
            .endpoint(storageAccount.endPoints().primary().blob())
            .buildClient();

        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        blobContainerClient.listBlobs().stream().count(); // ensure listBlobs has made API call
    }
}
