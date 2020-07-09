// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.HyperVGenerationTypes;
import com.azure.resourcemanager.compute.models.ImageDataDisk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemStateTypes;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineCustomImageOperationsTest extends ComputeManagementTest {
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
    public void canCreateImageFromNativeVhd() throws IOException {
        final String vhdBasedImageName = generateRandomResourceName("img", 20);

        VirtualMachine linuxVM =
            prepareGeneralizedVmWith2EmptyDataDisks(
                rgName, generateRandomResourceName("muldvm", 15), region, computeManager);
        //
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings creatableDisk =
            computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                .withOSDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineUnmanagedDataDisk disk : linuxVM.unmanagedDataDisks().values()) {
            creatableDisk
                .defineDataDiskImage()
                .withLun(disk.lun())
                .fromVhd(disk.vhdUri())
                .withDiskCaching(disk.cachingType())
                .withDiskSizeInGB(disk.size() + 10) // Resize each data disk image by +10GB
                .attach();
        }
        VirtualMachineCustomImage customImage = creatableDisk.create();
        Assertions.assertNotNull(customImage.id());
        Assertions.assertEquals(customImage.name(), vhdBasedImageName);
        Assertions.assertFalse(customImage.isCreatedFromVirtualMachine());
        Assertions.assertNull(customImage.sourceVirtualMachineId());
        Assertions.assertNotNull(customImage.osDiskImage());
        Assertions.assertNotNull(customImage.osDiskImage().blobUri());
        Assertions.assertEquals(customImage.osDiskImage().caching(), CachingTypes.READ_WRITE);
        Assertions.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assertions.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assertions.assertNotNull(customImage.dataDiskImages());
        Assertions.assertEquals(customImage.dataDiskImages().size(), linuxVM.unmanagedDataDisks().size());
        Assertions.assertTrue(customImage.hyperVGeneration().equals(HyperVGenerationTypes.V1));
        for (ImageDataDisk diskImage : customImage.dataDiskImages().values()) {
            VirtualMachineUnmanagedDataDisk matchedDisk = null;
            for (VirtualMachineUnmanagedDataDisk vmDisk : linuxVM.unmanagedDataDisks().values()) {
                if (vmDisk.lun() == diskImage.lun()) {
                    matchedDisk = vmDisk;
                    break;
                }
            }
            Assertions.assertNotNull(matchedDisk);
            Assertions.assertEquals(matchedDisk.cachingType(), diskImage.caching());
            Assertions.assertEquals(matchedDisk.vhdUri(), diskImage.blobUri());
            Assertions.assertEquals((long) matchedDisk.size() + 10, (long) diskImage.diskSizeGB());
        }
        VirtualMachineCustomImage image =
            computeManager.virtualMachineCustomImages().getByResourceGroup(rgName, vhdBasedImageName);
        Assertions.assertNotNull(image);
        PagedIterable<VirtualMachineCustomImage> images =
            computeManager.virtualMachineCustomImages().listByResourceGroup(rgName);
        Assertions.assertTrue(TestUtilities.getSize(images) > 0);

        // Create virtual machine from custom image
        //
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(generateRandomResourceName("cusvm", 15))
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withGeneralizedLinuxCustomImage(image.id())
                .withRootUsername("javauser")
                .withRootPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assertions.assertNotNull(dataDisks);
        Assertions.assertEquals(dataDisks.size(), image.dataDiskImages().size());

        // Create a hyperv Gen2 image
        VirtualMachineCustomImage.DefinitionStages.WithCreateAndDataDiskImageOSDiskSettings creatableDiskGen2 =
            computeManager
                .virtualMachineCustomImages()
                .define(vhdBasedImageName + "Gen2")
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withHyperVGeneration(HyperVGenerationTypes.V2)
                .withLinuxFromVhd(linuxVM.osUnmanagedDiskVhdUri(), OperatingSystemStateTypes.GENERALIZED)
                .withOSDiskCaching(linuxVM.osDiskCachingType());
        for (VirtualMachineUnmanagedDataDisk disk : linuxVM.unmanagedDataDisks().values()) {
            creatableDisk
                .defineDataDiskImage()
                .withLun(disk.lun())
                .fromVhd(disk.vhdUri())
                .withDiskCaching(disk.cachingType())
                .withDiskSizeInGB(disk.size() + 10) // Resize each data disk image by +10GB
                .attach();
        }
        VirtualMachineCustomImage customImageGen2 = creatableDiskGen2.create();
        Assertions.assertNotNull(customImageGen2.id());
        Assertions.assertEquals(customImageGen2.name(), vhdBasedImageName + "Gen2");
        Assertions.assertFalse(customImageGen2.isCreatedFromVirtualMachine());
        Assertions.assertNull(customImageGen2.sourceVirtualMachineId());
        Assertions.assertNotNull(customImageGen2.osDiskImage());
        Assertions.assertNotNull(customImageGen2.osDiskImage().blobUri());
        Assertions.assertEquals(customImageGen2.osDiskImage().caching(), CachingTypes.READ_WRITE);
        Assertions.assertEquals(customImageGen2.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assertions.assertEquals(customImageGen2.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assertions.assertNotNull(customImageGen2.dataDiskImages());
        Assertions.assertEquals(customImageGen2.dataDiskImages().size(), 0);
        Assertions.assertTrue(customImageGen2.hyperVGeneration().equals(HyperVGenerationTypes.V2));
    }

    @Test
    public void canCreateImageByCapturingVM() {
        final String vmName = generateRandomResourceName("vm67-", 20);
        final String imageName = generateRandomResourceName("img", 15);

        VirtualMachine vm = prepareGeneralizedVmWith2EmptyDataDisks(rgName, vmName, region, computeManager);
        //
        VirtualMachineCustomImage customImage =
            computeManager
                .virtualMachineCustomImages()
                .define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withHyperVGeneration(HyperVGenerationTypes.V1)
                .fromVirtualMachine(vm.id())
                .create();

        Assertions.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assertions.assertNotNull(customImage.osDiskImage());
        Assertions.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assertions.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assertions.assertNotNull(customImage.dataDiskImages());
        Assertions.assertEquals(customImage.dataDiskImages().size(), 2);
        Assertions.assertNotNull(customImage.sourceVirtualMachineId());
        Assertions.assertTrue(customImage.sourceVirtualMachineId().equalsIgnoreCase(vm.id()));
        Assertions.assertTrue(customImage.hyperVGeneration().equals(HyperVGenerationTypes.V1));

        for (VirtualMachineUnmanagedDataDisk vmDisk : vm.unmanagedDataDisks().values()) {
            Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assertions.assertEquals(diskImage.caching(), vmDisk.cachingType());
            Assertions.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size());
            Assertions.assertNotNull(diskImage.blobUri());
            diskImage.blobUri().equalsIgnoreCase(vmDisk.vhdUri());
        }

        customImage = computeManager.virtualMachineCustomImages().getByResourceGroup(rgName, imageName);
        Assertions.assertNotNull(customImage);
        Assertions.assertNotNull(customImage.inner());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
    }

    @Test
    public void canCreateImageFromManagedDisk() {
        final String vmName = generateRandomResourceName("vm7-", 20);
        final String storageAccountName = generateRandomResourceName("stg", 17);
        final String uname = "juser";
        final String password = password();

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
                .defineUnmanagedDataDisk("disk1")
                .withNewVhd(100)
                .withCaching(CachingTypes.READ_ONLY)
                .attach()
                .withNewUnmanagedDataDisk(100)
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(storageAccountName)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        Assertions.assertFalse(nativeVm.isManagedDiskEnabled());
        String osVhdUri = nativeVm.osUnmanagedDiskVhdUri();
        Assertions.assertNotNull(osVhdUri);
        Map<Integer, VirtualMachineUnmanagedDataDisk> dataDisks = nativeVm.unmanagedDataDisks();
        Assertions.assertEquals(dataDisks.size(), 2);

        computeManager.virtualMachines().deleteById(nativeVm.id());

        final String osDiskName = generateRandomResourceName("dsk", 15);
        // Create managed disk with Os from vm's Os disk
        //
        Disk managedOsDisk =
            computeManager
                .disks()
                .define(osDiskName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromVhd(osVhdUri)
                .withStorageAccountName(storageAccountName)
                .create();

        // Create managed disk with Data from vm's lun0 data disk
        //
        StorageAccount storageAccount =
            storageManager.storageAccounts().getByResourceGroup(rgName, storageAccountName);

        final String dataDiskName1 = generateRandomResourceName("dsk", 15);
        VirtualMachineUnmanagedDataDisk vmNativeDataDisk1 = dataDisks.get(0);
        Disk managedDataDisk1 =
            computeManager
                .disks()
                .define(dataDiskName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .fromVhd(vmNativeDataDisk1.vhdUri())
                .withStorageAccount(storageAccount)
                .create();

        // Create managed disk with Data from vm's lun1 data disk
        //
        final String dataDiskName2 = generateRandomResourceName("dsk", 15);
        VirtualMachineUnmanagedDataDisk vmNativeDataDisk2 = dataDisks.get(1);
        Disk managedDataDisk2 =
            computeManager
                .disks()
                .define(dataDiskName2)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withData()
                .fromVhd(vmNativeDataDisk2.vhdUri())
                .withStorageAccountId(storageAccount.id())
                .create();

        // Create an image from the above managed disks
        // Note that this is not a direct user scenario, but including this as per CRP team request
        //
        final String imageName = generateRandomResourceName("img", 15);
        VirtualMachineCustomImage customImage =
            computeManager
                .virtualMachineCustomImages()
                .define(imageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinuxFromDisk(managedOsDisk, OperatingSystemStateTypes.GENERALIZED)
                .defineDataDiskImage()
                .withLun(vmNativeDataDisk1.lun())
                .fromManagedDisk(managedDataDisk1)
                .withDiskCaching(vmNativeDataDisk1.cachingType())
                .withDiskSizeInGB(vmNativeDataDisk1.size() + 10)
                .attach()
                .defineDataDiskImage()
                .withLun(vmNativeDataDisk2.lun())
                .fromManagedDisk(managedDataDisk2)
                .withDiskSizeInGB(vmNativeDataDisk2.size() + 10)
                .attach()
                .create();

        Assertions.assertNotNull(customImage);
        Assertions.assertTrue(customImage.name().equalsIgnoreCase(imageName));
        Assertions.assertNotNull(customImage.osDiskImage());
        Assertions.assertEquals(customImage.osDiskImage().osState(), OperatingSystemStateTypes.GENERALIZED);
        Assertions.assertEquals(customImage.osDiskImage().osType(), OperatingSystemTypes.LINUX);
        Assertions.assertNotNull(customImage.dataDiskImages());
        Assertions.assertEquals(customImage.dataDiskImages().size(), 2);
        Assertions.assertTrue(customImage.hyperVGeneration().equals(HyperVGenerationTypes.V1));
        Assertions.assertNull(customImage.sourceVirtualMachineId());

        Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk1.lun()));
        Assertions
            .assertEquals(
                customImage.dataDiskImages().get(vmNativeDataDisk1.lun()).caching(), vmNativeDataDisk1.cachingType());
        Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmNativeDataDisk2.lun()));
        Assertions.assertEquals(customImage.dataDiskImages().get(vmNativeDataDisk2.lun()).caching(), CachingTypes.NONE);

        for (VirtualMachineUnmanagedDataDisk vmDisk : dataDisks.values()) {
            Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assertions.assertEquals((long) diskImage.diskSizeGB(), vmDisk.size() + 10);
            Assertions.assertNull(diskImage.blobUri());
            Assertions.assertNotNull(diskImage.managedDisk());
            Assertions
                .assertTrue(
                    diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk1.id())
                        || diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk2.id()));
        }
        computeManager.disks().deleteById(managedOsDisk.id());
        computeManager.disks().deleteById(managedDataDisk1.id());
        computeManager.disks().deleteById(managedDataDisk2.id());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
    }

    private VirtualMachine prepareGeneralizedVmWith2EmptyDataDisks(
        String rgName, String vmName, Region region, ComputeManager computeManager) {
        final String uname = "javauser";
        final String password = password();
        final KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(linuxImage)
                .withRootUsername(uname)
                .withRootPassword(password)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                .withNewVhd(30)
                .withCaching(CachingTypes.READ_WRITE)
                .attach()
                .defineUnmanagedDataDisk("disk-2")
                .withNewVhd(60)
                .withCaching(CachingTypes.READ_ONLY)
                .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                .withNewStorageAccount(generateRandomResourceName("stg", 17))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();
        //
        deprovisionAgentInLinuxVM(virtualMachine.getPrimaryPublicIPAddress().fqdn(), 22, uname, password);
        virtualMachine.deallocate();
        virtualMachine.generalize();
        return virtualMachine;
    }
}
