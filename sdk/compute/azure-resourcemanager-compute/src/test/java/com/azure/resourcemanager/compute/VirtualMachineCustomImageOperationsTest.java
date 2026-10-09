// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.*;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

public class VirtualMachineCustomImageOperationsTest extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_WEST2;

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
    public void canCreateImageByCapturingVM() {
        final String vmName = generateRandomResourceName("vm67-", 20);
        final String imageName = generateRandomResourceName("img", 15);

        VirtualMachine vm = prepareGeneralizedVmWith2EmptyDataDisks(rgName, vmName, region, computeManager);
        //
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages()
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

        for (VirtualMachineDataDisk vmDisk : vm.dataDisks().values()) {
            Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Assertions.assertEquals(diskImage.caching(), vmDisk.cachingType());
            Assertions.assertEquals((long) diskImage.diskSizeGB(),
                computeManager.disks().getById(vmDisk.id()).sizeInGB());
            Assertions.assertNull(diskImage.blobUri());
            Assertions.assertNotNull(diskImage.managedDisk());
        }

        customImage = computeManager.virtualMachineCustomImages().getByResourceGroup(rgName, imageName);
        Assertions.assertNotNull(customImage);
        Assertions.assertNotNull(customImage.innerModel());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
    }

    @Test
    public void canCreateImageFromManagedDisk() {
        final String vmName = generateRandomResourceName("vm7-", 20);
        final String uname = "juser";

        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername(uname)
            .withSsh(sshPublicKey())
            .withNewDataDisk(100, 0, CachingTypes.READ_ONLY)
            .withNewDataDisk(100, 1, CachingTypes.NONE)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DETACH)
            .withSize(generalPurposeVMSize())
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskDeleteOptions(DeleteOptions.DETACH)
            .create();

        Assertions.assertTrue(vm.isManagedDiskEnabled());
        Map<Integer, VirtualMachineDataDisk> dataDisks = vm.dataDisks();
        Assertions.assertEquals(dataDisks.size(), 2);

        VirtualMachineDataDisk vmDataDisk1 = dataDisks.get(0);
        VirtualMachineDataDisk vmDataDisk2 = dataDisks.get(1);
        final String osDiskId = vm.osDiskId();
        final String dataDiskId1 = vmDataDisk1.id();
        final String dataDiskId2 = vmDataDisk2.id();

        // Generalize and delete the VM, keeping its managed OS and data disks (delete options explicitly set to DETACH)
        //
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));
        deprovisionAgentInLinuxVM(vm);
        vm.deallocate();
        vm.generalize();
        computeManager.virtualMachines().deleteById(vm.id());

        Disk managedOsDisk = computeManager.disks().getById(osDiskId);
        Disk managedDataDisk1 = computeManager.disks().getById(dataDiskId1);
        Disk managedDataDisk2 = computeManager.disks().getById(dataDiskId2);

        // Create an image from the above managed disks
        // Note that this is not a direct user scenario, but including this as per CRP team request
        //
        final String imageName = generateRandomResourceName("img", 15);
        VirtualMachineCustomImage customImage = computeManager.virtualMachineCustomImages()
            .define(imageName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withLinuxFromDisk(managedOsDisk, OperatingSystemStateTypes.GENERALIZED)
            .defineDataDiskImage()
            .withLun(vmDataDisk1.lun())
            .fromManagedDisk(managedDataDisk1)
            .withDiskCaching(vmDataDisk1.cachingType())
            .withDiskSizeInGB(managedDataDisk1.sizeInGB() + 10)
            .attach()
            .defineDataDiskImage()
            .withLun(vmDataDisk2.lun())
            .fromManagedDisk(managedDataDisk2)
            .withDiskSizeInGB(managedDataDisk2.sizeInGB() + 10)
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

        Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDataDisk1.lun()));
        Assertions.assertEquals(customImage.dataDiskImages().get(vmDataDisk1.lun()).caching(),
            vmDataDisk1.cachingType());
        Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDataDisk2.lun()));
        Assertions.assertEquals(customImage.dataDiskImages().get(vmDataDisk2.lun()).caching(), CachingTypes.NONE);

        for (VirtualMachineDataDisk vmDisk : dataDisks.values()) {
            Assertions.assertTrue(customImage.dataDiskImages().containsKey(vmDisk.lun()));
            ImageDataDisk diskImage = customImage.dataDiskImages().get(vmDisk.lun());
            Disk sourceDisk = vmDisk.lun() == vmDataDisk1.lun() ? managedDataDisk1 : managedDataDisk2;
            Assertions.assertEquals((long) diskImage.diskSizeGB(), sourceDisk.sizeInGB() + 10);
            Assertions.assertNull(diskImage.blobUri());
            Assertions.assertNotNull(diskImage.managedDisk());
            Assertions.assertTrue(diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk1.id())
                || diskImage.managedDisk().id().equalsIgnoreCase(managedDataDisk2.id()));
        }
        computeManager.disks().deleteById(managedOsDisk.id());
        computeManager.disks().deleteById(managedDataDisk1.id());
        computeManager.disks().deleteById(managedDataDisk2.id());
        computeManager.virtualMachineCustomImages().deleteById(customImage.id());
    }

    private VirtualMachine prepareGeneralizedVmWith2EmptyDataDisks(String rgName, String vmName, Region region,
        ComputeManager computeManager) {
        final String uname = "javauser";
        final KnownLinuxVirtualMachineImage linuxImage = KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS;
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
            .withPopularLinuxImage(linuxImage)
            .withRootUsername(uname)
            .withSsh(sshPublicKey())
            .withNewDataDisk(30, 0, CachingTypes.READ_WRITE)
            .withNewDataDisk(60, 1, CachingTypes.READ_ONLY)
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withSize(generalPurposeVMSize())
            .create();
        //
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));
        deprovisionAgentInLinuxVM(virtualMachine);
        virtualMachine.deallocate();
        virtualMachine.generalize();
        return virtualMachine;
    }
}
