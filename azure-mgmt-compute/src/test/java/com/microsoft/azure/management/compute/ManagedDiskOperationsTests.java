package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.File;

public class ManagedDiskOperationsTests extends ComputeManagementTestBase {
    private static ApplicationTokenCredentials credentials;
    private static RestClient restClient;
    private static Region region = Region.fromName("eastus2euap");   // Special regions for canary deployment 'eastus2euap' and 'centraluseuap'

    @BeforeClass
    public static void setup() throws Exception {
        File credFile = new File("C:\\my.azureauth");
        credentials = ApplicationTokenCredentials.fromFile(credFile);

        AzureEnvironment canary = new AzureEnvironment("https://login.microsoftonline.com/",
                "https://management.core.windows.net/",
                "https://brazilus.management.azure.com/",
                "https://graph.windows.net/");

        restClient = new RestClient.Builder()
                .withBaseUrl(canary, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .build();

        computeManager = ComputeManager
                .authenticate(restClient, credentials.defaultSubscriptionId());
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(credentials.defaultSubscriptionId());
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void CanOperateOnEmptyManagedDisk() {
        final String rgName = ResourceNamer.randomResourceName("rg-md-", 20);
        final String diskName = ResourceNamer.randomResourceName("md-empty-", 20);
        // Cannot really test account update as canary deployment supports only STANDARD_LRS
        //
        final StorageAccountTypes updateTo = StorageAccountTypes.STANDARD_LRS;

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .define(rgName)
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
                .withAccountType(StorageAccountTypes.STANDARD_LRS)
                .withTag("tkey1", "tval1")
                // End option
                .create();

        Assert.assertNotNull(disk.id());
        Assert.assertTrue(disk.name().equalsIgnoreCase(diskName));
        Assert.assertEquals(disk.accountType(), StorageAccountTypes.STANDARD_LRS);
        Assert.assertEquals(disk.creationMethod(), DiskCreateOption.EMPTY);
        Assert.assertFalse(disk.isAttachedToVirtualMachine());
        Assert.assertEquals(disk.sizeInGB(), 100);
        Assert.assertNull(disk.osType());
        Assert.assertNotNull(disk.source());
        Assert.assertEquals(disk.source().type(), DiskSourceType.EMPTY);
        Assert.assertNull(disk.source().sourceId());

        // Resize and change storage account type
        //
        disk = disk.update()
                .withAccountType(updateTo)
                .withSizeInGB(200)
                .apply();

        Assert.assertEquals(disk.accountType(), updateTo);
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
        resourceManager.resourceGroups().deleteByName(rgName);
    }

    @Test
    public void canOperateOnManagedDiskFromDisk() {
        final String rgName = ResourceNamer.randomResourceName("rg-md-", 20);
        final String diskName1 = ResourceNamer.randomResourceName("md-1", 20);
        final String diskName2 = ResourceNamer.randomResourceName("md-2", 20);

        ResourceGroup resourceGroup = resourceManager
                .resourceGroups()
                .define(rgName)
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
                .copiedFromManagedDisk(emptyDisk)
                // Start Option
                .withSizeInGB(200)
                .withAccountType(StorageAccountTypes.STANDARD_LRS)
                // End Option
                .create();

        disk = computeManager.disks().getById(disk.id());

        Assert.assertNotNull(disk.id());
        Assert.assertTrue(disk.name().equalsIgnoreCase(diskName2));
        Assert.assertEquals(disk.accountType(), StorageAccountTypes.STANDARD_LRS);
        Assert.assertEquals(disk.creationMethod(), DiskCreateOption.COPY);
        Assert.assertFalse(disk.isAttachedToVirtualMachine());
        Assert.assertEquals(disk.sizeInGB(), 200);
        Assert.assertNull(disk.osType());
        Assert.assertNotNull(disk.source());
        Assert.assertEquals(disk.source().type(), DiskSourceType.COPIED_FROM_DISK);
        Assert.assertTrue(disk.source().sourceId().equalsIgnoreCase(emptyDisk.id()));

        computeManager.disks().deleteById(emptyDisk.id());
        computeManager.disks().deleteById(disk.id());
    }

//    @Test
//    public void canOperateOnManagedDiskFromPIRImage() {
//        final String rgName = ResourceNamer.randomResourceName("rg-md-", 20);
//        final String osDiskName = ResourceNamer.randomResourceName("md-os-", 20);
//        final String dataDiskNamePrefix = ResourceNamer.randomResourceName("md-data-", 20);
//
//        // TODO: Use this image once we move to production, canary does not have this image.
//        //
////        VirtualMachineImage linuxVmImage = computeManager.virtualMachineImages().getImage(region,
////                "alienvault",
////                "unified-security-management-anywhere",
////                "unified-security-management-anywhere",
////                "3.2.0");
//
//        VirtualMachineImage linuxVmImage = computeManager.virtualMachineImages().getImage(region,
//                "Canonical",
//                "UbuntuServer",
//                "14.04.2-LTS",
//                "14.04.201507060");
//
//        Assert.assertNotNull("Could not locate the PIR image", linuxVmImage.inner());
//
//        ResourceGroup resourceGroup = resourceManager
//                .resourceGroups()
//                .define(rgName)
//                .withRegion(region)
//                .create();
//
//        // Create managed disk containing os disk based on image's os disk image
//        //
//        Disk osDisk = computeManager.disks()
//                .define(osDiskName)
//                .withRegion(region)
//                .withExistingResourceGroup(resourceGroup.name())
//                .withOs()
//                .fromImage(linuxVmImage)
//                // Start Options
//                .withSizeInGB(200)
//                .withAccountType(StorageAccountTypes.STANDARD_LRS)
//                // End options
//                .create();
//
//        Assert.assertNotNull(osDisk.id());
//        Assert.assertTrue(osDisk.name().equalsIgnoreCase(osDiskName));
//        Assert.assertEquals(osDisk.accountType(), StorageAccountTypes.STANDARD_LRS);
//        Assert.assertEquals(osDisk.creationMethod(), DiskCreateOption.FROM_IMAGE);
//        Assert.assertFalse(osDisk.isAttachedToVirtualMachine());
//        Assert.assertEquals(osDisk.sizeInGB(), 200);
//        Assert.assertEquals(osDisk.osType(), OperatingSystemTypes.LINUX);
//        Assert.assertNotNull(osDisk.source());
//        Assert.assertEquals(osDisk.source().type(), DiskSourceType.FROM_OS_DISK_IMAGE);
//        Assert.assertEquals(osDisk.source().sourceId(), linuxVmImage.id());
//
//        // Create managed disks containing data disks based on image's data disk images
//        //
//        for (DataDiskImage diskImage : linuxVmImage.dataDiskImages().values()) {
//            final String dataDiskName = dataDiskNamePrefix + "-" + diskImage.lun();
//            Disk dataDisk = computeManager.disks()
//                    .define(dataDiskName)
//                    .withRegion(region)
//                    .withExistingResourceGroup(rgName)
//                    .withData()
//                    .fromImage(linuxVmImage, diskImage.lun())
//                    // Start Options
//                    .withSizeInGB(200)
//                    .withAccountType(StorageAccountTypes.PREMIUM_LRS)
//                    // End options
//                    .create();
//
//            Assert.assertNotNull(dataDisk.id());
//            Assert.assertTrue(dataDisk.name().equalsIgnoreCase(dataDiskName));
//            Assert.assertEquals(dataDisk.accountType(), StorageAccountTypes.STANDARD_LRS);
//            Assert.assertEquals(dataDisk.creationMethod(), DiskCreateOption.FROM_IMAGE);
//            Assert.assertFalse(dataDisk.isAttachedToVirtualMachine());
//            Assert.assertEquals(dataDisk.sizeInGB(), 200);
//            Assert.assertNull(dataDisk.osType());
//            Assert.assertNotNull(dataDisk.source());
//            Assert.assertEquals(dataDisk.source().type(), DiskSourceType.FROM_DATA_DISK_IMAGE);
//            Assert.assertEquals(dataDisk.source().sourceId(), linuxVmImage.id());
//            Assert.assertEquals((long) dataDisk.source().sourceDataDiskImageLun(), (long) diskImage.lun());
//        }
//
//        computeManager.disks().deleteById(osDisk.id());
//        for (DataDiskImage diskImage : linuxVmImage.dataDiskImages().values()) {
//            final String dataDiskName = dataDiskNamePrefix + "-" + diskImage.lun();
//            computeManager.disks().deleteByGroup(rgName, dataDiskName);
//        }
//    }

    @Test
    public void canOperateOnManagedDiskFromSnapshot() {
        // TODO After adding support for snapshot
    }
}