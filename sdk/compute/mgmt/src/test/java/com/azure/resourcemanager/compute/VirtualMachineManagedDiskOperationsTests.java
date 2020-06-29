// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.ImageDataDisk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.Map;

import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineManagedDiskOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_EAST;
    private KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;

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
    public void canCreateVirtualMachineFromPIRImageWithManagedOsDisk() {
        final String vmName1 = "myvm1";
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);
        final String uname = "juser";
        final String password = password();

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();
        // Ensure default to managed disk
        //
        Assertions.assertTrue(virtualMachine.isManagedDiskEnabled());
        // Validate caching, size and the default storage account type set for the managed disk
        // backing os disk
        //
        Assertions.assertNotNull(virtualMachine.osDiskStorageAccountType());
        Assertions.assertEquals(virtualMachine.osDiskCachingType(), CachingTypes.READ_WRITE);
        Assertions.assertEquals(virtualMachine.size(), VirtualMachineSizeTypes.STANDARD_D5_V2);
        // Validate the implicit managed disk created by CRP to back the os disk
        //
        Assertions.assertNotNull(virtualMachine.osDiskId());
        Disk osDisk = computeManager.disks().getById(virtualMachine.osDiskId());
        Assertions.assertTrue(osDisk.isAttachedToVirtualMachine());
        Assertions.assertEquals(osDisk.osType(), OperatingSystemTypes.LINUX);
        // Check the auto created public ip
        //
        String publicIpId = virtualMachine.getPrimaryPublicIPAddressId();
        Assertions.assertNotNull(publicIpId);
        // Validates the options which are valid only for native disks
        //
        Assertions.assertNull(virtualMachine.osUnmanagedDiskVhdUri());
        Assertions.assertNotNull(virtualMachine.unmanagedDataDisks());
        Assertions.assertTrue(virtualMachine.unmanagedDataDisks().size() == 0);
    }

    @Test
    public void canCreateUpdateVirtualMachineWithEmptyManagedDataDisks() {
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);
        final String uname = "juser";
        final String password = password();
        // Create with implicit + explicit empty disks, check default and override
        //
        final String vmName1 = "myvm1";
        final String explicitlyCreatedEmptyDiskName1 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName2 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName3 = generateRandomResourceName(vmName1 + "_mdisk_", 25);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Creatable<Disk> creatableEmptyDisk1 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk2 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk3 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                // Start: Add 5 empty managed disks
                .withNewDataDisk(100) // CreateOption: EMPTY
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY) // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE) // CreateOption: ATTACH
                // End : Add 5 empty managed disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assertions.assertTrue(virtualMachine.isManagedDiskEnabled());
        // There should not be any un-managed data disks
        //
        Assertions.assertNotNull(virtualMachine.unmanagedDataDisks());
        Assertions.assertEquals(virtualMachine.unmanagedDataDisks().size(), 0);
        // Validate the managed data disks
        //
        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions.assertTrue(dataDisks.size() == 5);
        Assertions.assertTrue(dataDisks.containsKey(1));
        VirtualMachineDataDisk dataDiskLun1 = dataDisks.get(1);
        Assertions.assertNotNull(dataDiskLun1.id());
        Assertions.assertEquals(dataDiskLun1.cachingType(), CachingTypes.READ_ONLY);
        Assertions.assertEquals(dataDiskLun1.size(), 100);

        Assertions.assertTrue(dataDisks.containsKey(2));
        VirtualMachineDataDisk dataDiskLun2 = dataDisks.get(2);
        Assertions.assertNotNull(dataDiskLun2.id());
        Assertions.assertEquals(dataDiskLun2.cachingType(), CachingTypes.NONE);
        Assertions.assertEquals(dataDiskLun2.size(), 150);

        Assertions.assertTrue(dataDisks.containsKey(3));
        VirtualMachineDataDisk dataDiskLun3 = dataDisks.get(3);
        Assertions.assertNotNull(dataDiskLun3.id());
        Assertions.assertEquals(dataDiskLun3.cachingType(), CachingTypes.NONE);
        Assertions.assertEquals(dataDiskLun3.size(), 150);
        // Validate the defaults assigned
        //
        for (VirtualMachineDataDisk dataDisk : dataDisks.values()) {
            if (dataDisk.lun() != 1 && dataDisk.lun() != 2 && dataDisk.lun() != 3) {
                Assertions.assertEquals(dataDisk.cachingType(), CachingTypes.READ_WRITE);
                Assertions.assertEquals(dataDisk.storageAccountType(), StorageAccountTypes.STANDARD_LRS);
            }
        }

        // Updating and adding disk as part of VM Update seems consistency failing, CRP is aware of
        // this, hence until it is fixed comment-out the test
        //
        //        {
        //            "startTime": "2017-01-26T05:48:59.9290573+00:00",
        //                "endTime": "2017-01-26T05:49:02.2884052+00:00",
        //                "status": "Failed",
        //                "error": {
        //            "code": "InternalExecutionError",
        //                    "message": "An internal execution error occurred."
        //        },
        //            "name": "bc8072a7-38bb-445b-ae59-f16cf125342c"
        //        }
        //
        //        virtualMachine.deallocate();
        //
        //        virtualMachine.update()
        //                .withDataDiskUpdated(1, 200)
        //                .withDataDiskUpdated(2, 200, CachingTypes.READ_WRITE)
        //                .withNewDataDisk(60)
        //                .apply();
        //
        //        Assertions.assertTrue(virtualMachine.isManagedDiskEnabled());
        //        // There should not be any un-managed data disks
        //        //
        //        Assertions.assertNotNull(virtualMachine.unmanagedDataDisks());
        //        Assertions.assertEquals(virtualMachine.unmanagedDataDisks().size(), 0);
        //
        //        // Validate the managed data disks
        //        //
        //         dataDisks = virtualMachine.dataDisks();
        //        Assertions.assertNotNull(dataDisks);
        //        Assertions.assertTrue(dataDisks.size() == 6);
        //        Assertions.assertTrue(dataDisks.containsKey(1));
        //        dataDiskLun1 = dataDisks.get(1);
        //        Assertions.assertNotNull(dataDiskLun1.id());
        //        Assertions.assertEquals(dataDiskLun1.cachingType(), CachingTypes.READ_ONLY);
        //        Assertions.assertEquals(dataDiskLun1.size(), 200);  // 100 -> 200
        //
        //        Assertions.assertTrue(dataDisks.containsKey(2));
        //        dataDiskLun2 = dataDisks.get(2);
        //        Assertions.assertNotNull(dataDiskLun2.id());
        //        Assertions.assertEquals(dataDiskLun2.cachingType(), CachingTypes.READ_WRITE); // NONE -> READ_WRITE
        //        Assertions.assertEquals(dataDiskLun2.size(), 200);  // 150 -> 200
        //
        //        Assertions.assertTrue(dataDisks.containsKey(3));
        //        dataDiskLun3 = dataDisks.get(3);
        //        Assertions.assertNotNull(dataDiskLun3.id());
        //        Assertions.assertEquals(dataDiskLun3.cachingType(), CachingTypes.NONE);
        //        Assertions.assertEquals(dataDiskLun3.size(), 150);
        //
        //        // Ensure defaults of other disks are not affected
        //        for (VirtualMachineDataDisk dataDisk : dataDisks.values()) {
        //            if (dataDisk.lun() != 1 && dataDisk.lun() != 3) {
        //                Assertions.assertEquals(dataDisk.cachingType(), CachingTypes.READ_WRITE);
        //                Assertions.assertEquals(dataDisk.storageAccountType(), StorageAccountTypes.STANDARD_LRS);
        //            }
        //        }
    }

    @Test
    public void canCreateVirtualMachineFromCustomImageWithManagedDisks() {
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);
        final String uname = "juser";
        final String password = password();
        // Create with implicit + explicit empty disks, check default and override
        //
        final String vmName1 = "myvm1";
        final String explicitlyCreatedEmptyDiskName1 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName2 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName3 = generateRandomResourceName(vmName1 + "_mdisk_", 25);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Creatable<Disk> creatableEmptyDisk1 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk2 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk3 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        VirtualMachine virtualMachine1 =
            computeManager
                .virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                // Start: Add bunch of empty managed disks
                .withNewDataDisk(100) // CreateOption: EMPTY
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY) // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE) // CreateOption: ATTACH
                // End : Add bunch of empty managed disks
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();
        System.out.println("Waiting for some time before de-provision");
        sleep(60 * 1000); // Wait for some time to ensure vm is publicly accessible
        deprovisionAgentInLinuxVM(virtualMachine1.getPrimaryPublicIPAddress().fqdn(), 22, uname, password);

        virtualMachine1.deallocate();
        virtualMachine1.generalize();

        final String customImageName = generateRandomResourceName("img-", 10);
        VirtualMachineCustomImage customImage =
            computeManager
                .virtualMachineCustomImages()
                .define(customImageName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .fromVirtualMachine(virtualMachine1)
                .create();
        Assertions.assertNotNull(customImage);
        Assertions.assertNotNull(customImage.sourceVirtualMachineId());
        Assertions
            .assertTrue(customImage.sourceVirtualMachineId().equalsIgnoreCase(virtualMachine1.id().toLowerCase()));
        Assertions.assertNotNull(customImage.osDiskImage());
        Assertions.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assertions.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assertions.assertNotNull(customImage.dataDiskImages());
        Assertions.assertEquals(customImage.dataDiskImages().size(), 5);
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assertions.assertNull(imageDataDisk.blobUri());
            Assertions.assertNotNull(imageDataDisk.managedDisk().id());
        }

        // Create virtual machine from the custom image
        // This one relies on CRP's capability to create implicit data disks from the virtual machine
        // image data disk images.
        //
        final String vmName2 = "myvm2";
        VirtualMachine virtualMachine2 =
            computeManager
                .virtualMachines()
                .define(vmName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withGeneralizedLinuxCustomImage(customImage.id())
                .withRootUsername(uname)
                .withRootPassword(password)
                // No explicit data disks, let CRP create it from the image's data disk images
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine2.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions.assertEquals(dataDisks.size(), customImage.dataDiskImages().size());
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assertions.assertTrue(dataDisks.containsKey(imageDataDisk.lun()));
            VirtualMachineDataDisk dataDisk = dataDisks.get(imageDataDisk.lun());
            Assertions.assertEquals(dataDisk.cachingType(), imageDataDisk.caching());
            // Fails due to CRP bug: Managed disk size is not returned on gets.
            // Assertions.assertEquals(dataDisk.size(), (long) imageDataDisk.diskSizeGB());
        }

        // Create virtual machine from the custom image
        // This one override the size and caching type of data disks from data disk images and
        // adds one additional disk
        //

        final String vmName3 = "myvm3";
        VirtualMachine.DefinitionStages.WithManagedCreate creatableVirtualMachine3 =
            computeManager
                .virtualMachines()
                .define(vmName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withGeneralizedLinuxCustomImage(customImage.id())
                .withRootUsername(uname)
                .withRootPassword(password);
        for (ImageDataDisk dataDiskImage : customImage.dataDiskImages().values()) {
            // Explicitly override the properties of the data disks created from disk image
            //
            // CreateOption: FROM_IMAGE
            VirtualMachineDataDisk dataDisk = dataDisks.get(dataDiskImage.lun());
            creatableVirtualMachine3
                .withNewDataDiskFromImage(
                    dataDiskImage.lun(),
                    dataDisk.size() + 10, // increase size by 10 GB
                    CachingTypes.READ_ONLY);
        }
        VirtualMachine virtualMachine3 =
            creatableVirtualMachine3
                .withNewDataDisk(200) // CreateOption: EMPTY
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        dataDisks = virtualMachine3.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions
            .assertEquals(dataDisks.size(), customImage.dataDiskImages().size() + 1 /* count one extra empty disk */);
        for (ImageDataDisk imageDataDisk : customImage.dataDiskImages().values()) {
            Assertions.assertTrue(dataDisks.containsKey(imageDataDisk.lun()));
            VirtualMachineDataDisk dataDisk = dataDisks.get(imageDataDisk.lun());
            Assertions.assertEquals(dataDisk.cachingType(), CachingTypes.READ_ONLY);
            // Fails due to CRP bug: Managed disk size is not returned on gets.
            // Assertions.assertEquals(dataDisk.size(), (long) imageDataDisk.diskSizeGB() + 10);
        }
    }

    @Test
    public void canUpdateVirtualMachineByAddingAndRemovingManagedDisks() {
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);
        final String uname = "juser";
        final String password = password();
        // Create with implicit + explicit empty disks, check default and override
        //
        final String vmName1 = "myvm1";
        final String explicitlyCreatedEmptyDiskName1 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName2 = generateRandomResourceName(vmName1 + "_mdisk_", 25);
        final String explicitlyCreatedEmptyDiskName3 = generateRandomResourceName(vmName1 + "_mdisk_", 25);

        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Creatable<Disk> creatableEmptyDisk1 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk2 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName2)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        Creatable<Disk> creatableEmptyDisk3 =
            computeManager
                .disks()
                .define(explicitlyCreatedEmptyDiskName3)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(150);

        VirtualMachine virtualMachine1 =
            computeManager
                .virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                // Start: Add bunch of empty managed disks
                .withNewDataDisk(100) // CreateOption: EMPTY
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE) // CreateOption: EMPTY
                .withNewDataDisk(creatableEmptyDisk1) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk2, 2, CachingTypes.NONE) // CreateOption: ATTACH
                .withNewDataDisk(creatableEmptyDisk3, 3, CachingTypes.NONE) // CreateOption: ATTACH
                // End : Add bunch of empty managed disks
                .withDataDiskDefaultCachingType(CachingTypes.READ_ONLY)
                .withDataDiskDefaultStorageAccountType(StorageAccountTypes.STANDARD_LRS)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        virtualMachine1
            .update()
            .withoutDataDisk(1)
            .withNewDataDisk(100, 6, CachingTypes.READ_WRITE) // CreateOption: EMPTY
            .apply();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine1.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions.assertEquals(dataDisks.size(), 5); // Removed one added another
        Assertions.assertTrue(dataDisks.containsKey(6));
        Assertions.assertFalse(dataDisks.containsKey(1));
    }

    @Test
    public void canCreateVirtualMachineByAttachingManagedOsDisk() {
        final String uname = "juser";
        final String password = password();
        final String vmName = "myvm6";
        final String storageAccountName = generateRandomResourceName("stg", 17);

        // Creates a native virtual machine
        //
        VirtualMachine nativeVm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.2-LTS")
                .withRootUsername(uname)
                .withRootPassword(password)
                .withUnmanagedDisks() /* UN-MANAGED OS and DATA DISKS */
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(storageAccountName)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assertions.assertFalse(nativeVm.isManagedDiskEnabled());
        String osVhdUri = nativeVm.osUnmanagedDiskVhdUri();
        Assertions.assertNotNull(osVhdUri);

        computeManager.virtualMachines().deleteById(nativeVm.id());

        final String diskName = generateRandomResourceName("dsk-", 15);
        Disk osDisk =
            computeManager
                .disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromVhd(osVhdUri)
                .withStorageAccountName(storageAccountName)
                .create();

        // Creates a managed virtual machine
        //
        VirtualMachine managedVm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withSpecializedOSDisk(osDisk, OperatingSystemTypes.LINUX)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assertions.assertTrue(managedVm.isManagedDiskEnabled());
        Assertions.assertTrue(managedVm.osDiskId().equalsIgnoreCase(osDisk.id().toLowerCase()));
    }

    @Test
    public void canCreateVirtualMachineWithManagedDiskInManagedAvailabilitySet() {
        final String availSetName = generateRandomResourceName("av-", 15);
        final String uname = "juser";
        final String password = password();
        final String vmName = "myvm6";

        VirtualMachine managedVm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_ONLY)
                .withNewDataDisk(100, 2, CachingTypes.READ_WRITE, StorageAccountTypes.STANDARD_LRS)
                .withNewAvailabilitySet(availSetName) // Default to managed availability set
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assertions.assertNotNull(managedVm.availabilitySetId());
        AvailabilitySet availabilitySet = computeManager.availabilitySets().getById(managedVm.availabilitySetId());
        Assertions.assertTrue(availabilitySet.virtualMachineIds().size() > 0);
        Assertions.assertEquals(availabilitySet.sku(), AvailabilitySetSkuTypes.ALIGNED);
    }
}
