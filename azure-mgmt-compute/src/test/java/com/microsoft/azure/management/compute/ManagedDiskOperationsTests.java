package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class ManagedDiskOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static Region region = Region.US_WEST_CENTRAL;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }
    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void CanOperateOnEmptyManagedDisk() {
        final String diskName = generateRandomResourceName("md-empty-", 20);
        final DiskSkuTypes updateTo = DiskSkuTypes.STANDARD_LRS;

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .define(RG_NAME)
                .withRegion(region)
                .create();

        // Create an empty managed disk
        //
        Disk disk = computeManager.disks()
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

        Assert.assertNotNull(disk.id());
        Assert.assertTrue(disk.name().equalsIgnoreCase(diskName));
        Assert.assertEquals(disk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assert.assertEquals(disk.creationMethod(), DiskCreateOption.EMPTY);
        Assert.assertFalse(disk.isAttachedToVirtualMachine());
        Assert.assertEquals(disk.sizeInGB(), 100);
        Assert.assertNull(disk.osType());
        Assert.assertNotNull(disk.source());
        Assert.assertEquals(disk.source().type(), CreationSourceType.EMPTY);
        Assert.assertNull(disk.source().sourceId());

        // Resize and change storage account type
        //
        disk = disk.update()
                .withSku(updateTo)
                .withSizeInGB(200)
                .apply();

        Assert.assertEquals(disk.sku(), updateTo);
        Assert.assertEquals(disk.sizeInGB(), 200);

        disk = computeManager.disks().getByGroup(disk.resourceGroupName(), disk.name());
        Assert.assertNotNull(disk);

        PagedList<Disk> myDisks = computeManager.disks().listByGroup(disk.resourceGroupName());
        Assert.assertNotNull(myDisks);
        Assert.assertTrue(myDisks.size() > 0);

        String sasUrl = disk.grantAccess(100);
        Assert.assertTrue(sasUrl != null && sasUrl != "");

        // Requires access to be revoked before deleting the disk
        //
        disk.revokeAccess();
        computeManager.disks().deleteById(disk.id());
    }

    @Test
    public void canOperateOnManagedDiskFromDisk() {
        final String diskName1 = generateRandomResourceName("md-1", 20);
        final String diskName2 = generateRandomResourceName("md-2", 20);

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .define(RG_NAME)
                .withRegion(region)
                .create();

        // Create an empty  managed disk
        //
        Disk emptyDisk = computeManager.disks()
                .define(diskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup.name())
                .withData()
                .withSizeInGB(100)
                .create();

        // Create a managed disk from existing managed disk
        //
        Disk disk = computeManager.disks()
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

        Assert.assertNotNull(disk.id());
        Assert.assertTrue(disk.name().equalsIgnoreCase(diskName2));
        Assert.assertEquals(disk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assert.assertEquals(disk.creationMethod(), DiskCreateOption.COPY);
        Assert.assertFalse(disk.isAttachedToVirtualMachine());
        Assert.assertEquals(disk.sizeInGB(), 200);
        Assert.assertNull(disk.osType());
        Assert.assertNotNull(disk.source());
        Assert.assertEquals(disk.source().type(), CreationSourceType.COPIED_FROM_DISK);
        Assert.assertTrue(disk.source().sourceId().equalsIgnoreCase(emptyDisk.id()));

        computeManager.disks().deleteById(emptyDisk.id());
        computeManager.disks().deleteById(disk.id());
    }

    @Test
    public void canOperateOnManagedDiskFromSnapshot() {
        final String emptyDiskName = generateRandomResourceName("md-empty-", 20);
        final String snapshotBasedDiskName = generateRandomResourceName("md-snp-", 20);
        final String snapshotName = generateRandomResourceName("snp-", 20);

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .define(RG_NAME)
                .withRegion(region)
                .create();

        Disk emptyDisk = computeManager.disks()
                .define(emptyDiskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(100)
                .create();

        Snapshot snapshot = computeManager.snapshots()
                .define(snapshotName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withDataFromDisk(emptyDisk)
                .withSizeInGB(200)
                .withSku(DiskSkuTypes.STANDARD_LRS)
                .create();

        Assert.assertNotNull(snapshot.id());
        Assert.assertTrue(snapshot.name().equalsIgnoreCase(snapshotName));
        Assert.assertEquals(snapshot.sku(), DiskSkuTypes.STANDARD_LRS);
        Assert.assertEquals(snapshot.creationMethod(), DiskCreateOption.COPY);
        Assert.assertEquals(snapshot.sizeInGB(), 200);
        Assert.assertNull(snapshot.osType());
        Assert.assertNotNull(snapshot.source());
        Assert.assertEquals(snapshot.source().type(), CreationSourceType.COPIED_FROM_DISK);
        Assert.assertTrue(snapshot.source().sourceId().equalsIgnoreCase(emptyDisk.id()));

        Disk fromSnapshotDisk = computeManager.disks()
                .define(snapshotBasedDiskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .fromSnapshot(snapshot)
                .withSizeInGB(300)
                .create();

        Assert.assertNotNull(fromSnapshotDisk.id());
        Assert.assertTrue(fromSnapshotDisk.name().equalsIgnoreCase(snapshotBasedDiskName));
        Assert.assertEquals(fromSnapshotDisk.sku(), DiskSkuTypes.STANDARD_LRS);
        Assert.assertEquals(fromSnapshotDisk.creationMethod(), DiskCreateOption.COPY);
        Assert.assertEquals(fromSnapshotDisk.sizeInGB(), 300);
        Assert.assertNull(fromSnapshotDisk.osType());
        Assert.assertNotNull(fromSnapshotDisk.source());
        Assert.assertEquals(fromSnapshotDisk.source().type(), CreationSourceType.COPIED_FROM_SNAPSHOT);
        Assert.assertTrue(fromSnapshotDisk.source().sourceId().equalsIgnoreCase(snapshot.id()));
    }
}