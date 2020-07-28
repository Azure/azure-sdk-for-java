// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.CreationSourceType;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOption;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ManagedDiskOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_WEST_CENTRAL;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canOperateOnEmptyManagedDisk() {
        final String diskName = generateRandomResourceName("md-empty-", 20);
        final DiskSkuTypes updateTo = DiskSkuTypes.STANDARD_LRS;

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create an empty managed disk
        //
        Disk disk =
            computeManager
                .disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .withSizeInGB(100)
                // Start option
                .withSku(DiskSkuTypes.STANDARD_LRS)
                .withTag("tkey1", "tval1")
                // End option
                .create();

        Assertions.assertNotNull(disk.id());
        Assertions.assertTrue(disk.name().equalsIgnoreCase(diskName));
        Assertions.assertEquals(disk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assertions.assertEquals(disk.creationMethod(), DiskCreateOption.EMPTY);
        Assertions.assertFalse(disk.isAttachedToVirtualMachine());
        Assertions.assertEquals(disk.sizeInGB(), 100);
        Assertions.assertNull(disk.osType());
        Assertions.assertNotNull(disk.source());
        Assertions.assertEquals(disk.source().type(), CreationSourceType.EMPTY);
        Assertions.assertNull(disk.source().sourceId());

        // Resize and change storage account type
        //
        disk = disk.update().withSku(updateTo).withSizeInGB(200).apply();

        Assertions.assertEquals(disk.sku(), updateTo);
        Assertions.assertEquals(disk.sizeInGB(), 200);

        disk = computeManager.disks().getByResourceGroup(disk.resourceGroupName(), disk.name());
        Assertions.assertNotNull(disk);

        PagedIterable<Disk> myDisks = computeManager.disks().listByResourceGroup(disk.resourceGroupName());
        Assertions.assertNotNull(myDisks);
        Assertions.assertTrue(TestUtilities.getSize(myDisks) > 0);

        String sasUrl = disk.grantAccess(100);
        Assertions.assertTrue(sasUrl != null && sasUrl != "");

        // Requires access to be revoked before deleting the disk
        //
        disk.revokeAccess();
        computeManager.disks().deleteById(disk.id());
    }

    @Test
    public void canOperateOnManagedDiskFromDisk() {
        final String diskName1 = generateRandomResourceName("md-1", 20);
        final String diskName2 = generateRandomResourceName("md-2", 20);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create an empty  managed disk
        //
        Disk emptyDisk =
            computeManager
                .disks()
                .define(diskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .withSizeInGB(100)
                .create();

        // Create a managed disk from existing managed disk
        //
        Disk disk =
            computeManager
                .disks()
                .define(diskName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .fromDisk(emptyDisk)
                // Start Option
                .withSizeInGB(200)
                .withSku(DiskSkuTypes.STANDARD_LRS)
                // End Option
                .create();

        disk = computeManager.disks().getById(disk.id());

        Assertions.assertNotNull(disk.id());
        Assertions.assertTrue(disk.name().equalsIgnoreCase(diskName2));
        Assertions.assertEquals(disk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assertions.assertEquals(disk.creationMethod(), DiskCreateOption.COPY);
        Assertions.assertFalse(disk.isAttachedToVirtualMachine());
        Assertions.assertEquals(disk.sizeInGB(), 200);
        Assertions.assertNull(disk.osType());
        Assertions.assertNotNull(disk.source());
        Assertions.assertEquals(disk.source().type(), CreationSourceType.COPIED_FROM_DISK);
        Assertions.assertTrue(disk.source().sourceId().equalsIgnoreCase(emptyDisk.id()));

        computeManager.disks().deleteById(emptyDisk.id());
        computeManager.disks().deleteById(disk.id());
    }

    @Test
    public void canOperateOnManagedDiskFromUpload() {
        final String diskName = generateRandomResourceName("md-2", 20);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        // Create a managed disk from upload
        //
        Disk disk =
            computeManager
                .disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .withUploadSizeInMB(1000)
                .withSku(DiskSkuTypes.STANDARD_LRS)
                // End Option
                .create();

        disk = computeManager.disks().getById(disk.id());

        Assertions.assertNotNull(disk.id());
        Assertions.assertTrue(disk.name().equalsIgnoreCase(diskName));
        Assertions.assertEquals(disk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assertions.assertEquals(disk.creationMethod(), DiskCreateOption.UPLOAD);
        Assertions.assertFalse(disk.isAttachedToVirtualMachine());
        Assertions.assertEquals(disk.sizeInGB(), 0);
        Assertions.assertNull(disk.osType());
        Assertions.assertNotNull(disk.source());
        Assertions.assertEquals(disk.source().type(), CreationSourceType.UNKNOWN);

        computeManager.disks().deleteById(disk.id());
    }

    @Test
    public void canOperateOnManagedDiskFromSnapshot() {
        final String emptyDiskName = generateRandomResourceName("md-empty-", 20);
        final String snapshotBasedDiskName = generateRandomResourceName("md-snp-", 20);
        final String snapshotName = generateRandomResourceName("snp-", 20);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Disk emptyDisk =
            computeManager
                .disks()
                .define(emptyDiskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(100)
                .create();

        Snapshot snapshot =
            computeManager
                .snapshots()
                .define(snapshotName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withDataFromDisk(emptyDisk)
                .withSizeInGB(200)
                .withSku(SnapshotSkuType.STANDARD_LRS)
                .create();

        Assertions.assertNotNull(snapshot.id());
        Assertions.assertTrue(snapshot.name().equalsIgnoreCase(snapshotName));
        Assertions.assertEquals(snapshot.sku(), DiskSkuTypes.STANDARD_LRS);
        Assertions.assertEquals(snapshot.creationMethod(), DiskCreateOption.COPY);
        Assertions.assertEquals(snapshot.sizeInGB(), 200);
        Assertions.assertNull(snapshot.osType());
        Assertions.assertNotNull(snapshot.source());
        Assertions.assertEquals(snapshot.source().type(), CreationSourceType.COPIED_FROM_DISK);
        Assertions.assertTrue(snapshot.source().sourceId().equalsIgnoreCase(emptyDisk.id()));

        Disk fromSnapshotDisk =
            computeManager
                .disks()
                .define(snapshotBasedDiskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .fromSnapshot(snapshot)
                .withSizeInGB(300)
                .create();

        Assertions.assertNotNull(fromSnapshotDisk.id());
        Assertions.assertTrue(fromSnapshotDisk.name().equalsIgnoreCase(snapshotBasedDiskName));
        Assertions.assertEquals(fromSnapshotDisk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assertions.assertEquals(fromSnapshotDisk.creationMethod(), DiskCreateOption.COPY);
        Assertions.assertEquals(fromSnapshotDisk.sizeInGB(), 300);
        Assertions.assertNull(fromSnapshotDisk.osType());
        Assertions.assertNotNull(fromSnapshotDisk.source());
        Assertions.assertEquals(fromSnapshotDisk.source().type(), CreationSourceType.COPIED_FROM_SNAPSHOT);
        Assertions.assertTrue(fromSnapshotDisk.source().sourceId().equalsIgnoreCase(snapshot.id()));
    }
}
