// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMs;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.time.Duration;
import java.util.Iterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineScaleSetManagedDiskOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private Region region = Region.US_EAST;

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
    public void canCreateUpdateVirtualMachineScaleSetFromPIRWithManagedDisk() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            this
                .networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1");
        VirtualMachineScaleSet vmScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_DS1_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                .withNewDataDisk(100, 2, CachingTypes.READ_ONLY)
                .withOSDiskStorageAccountType(StorageAccountTypes.PREMIUM_LRS) // Override the default STANDARD_LRS
                .create();

        Assertions.assertTrue(vmScaleSet.managedOSDiskStorageAccountType().equals(StorageAccountTypes.PREMIUM_LRS));
        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedIterable<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();
        Assertions.assertEquals(TestUtilities.getSize(virtualMachines), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assertions.assertTrue(vm.isOSBasedOnPlatformImage());
            Assertions.assertFalse(vm.isOSBasedOnCustomImage());
            Assertions.assertFalse(vm.isOSBasedOnStoredImage());
            Assertions.assertTrue(vm.isManagedDiskEnabled());
            Assertions.assertNotNull(vm.unmanagedDataDisks());
            Assertions.assertEquals(vm.unmanagedDataDisks().size(), 0);
            Assertions.assertNotNull(vm.dataDisks());
            Assertions.assertEquals(vm.dataDisks().size(), 3);
        }
        vmScaleSet.update().withoutDataDisk(0).withNewDataDisk(50).apply();

        virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        virtualMachines = virtualMachineScaleSetVMs.list();

        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        Assertions.assertEquals(TestUtilities.getSize(virtualMachines), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assertions.assertNotNull(vm.dataDisks());
            Assertions.assertEquals(vm.dataDisks().size(), 3);
        }

        // test attach/detach data disk to single instance
        final String diskName = generateRandomResourceName("disk", 10);
        Disk disk0 =
            this
                .computeManager
                .disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(32)
                .create();

        Iterator<VirtualMachineScaleSetVM> vmIterator = virtualMachines.iterator();
        VirtualMachineScaleSetVM vm0 = vmIterator.next();
        VirtualMachineScaleSetVM vm1 = vmIterator.next();
        final int existDiskLun = 2;
        final int newDiskLun = 10;
        // cannot detach non-exist disk
        Exception expectedException = null;
        try {
            vm0.update().withoutDataDisk(newDiskLun);
        } catch (IllegalStateException e) {
            expectedException = e;
        }
        Assertions.assertNotNull(expectedException);
        // cannot detach disk from VMSS model
        expectedException = null;
        try {
            vm0.update().withoutDataDisk(existDiskLun);
        } catch (IllegalStateException e) {
            expectedException = e;
        }
        Assertions.assertNotNull(expectedException);
        // cannot attach disk with same lun
        expectedException = null;
        try {
            vm0.update().withExistingDataDisk(disk0, existDiskLun, CachingTypes.NONE);
        } catch (IllegalStateException e) {
            expectedException = e;
        }
        Assertions.assertNotNull(expectedException);
        // cannot attach disk with same lun
        expectedException = null;
        try {
            vm0
                .update()
                .withExistingDataDisk(disk0, newDiskLun, CachingTypes.NONE)
                .withExistingDataDisk(disk0, newDiskLun, CachingTypes.NONE);
        } catch (IllegalStateException e) {
            expectedException = e;
        }
        Assertions.assertNotNull(expectedException);

        // attach disk
        final int vmssModelDiskCount = vm0.dataDisks().size();
        vm0.update().withExistingDataDisk(disk0, newDiskLun, CachingTypes.READ_WRITE).apply();
        Assertions.assertEquals(vmssModelDiskCount + 1, vm0.dataDisks().size());

        // cannot attach disk that already attached
        disk0.refresh();
        expectedException = null;
        try {
            vm1.update().withExistingDataDisk(disk0, newDiskLun, CachingTypes.NONE).apply();
        } catch (IllegalStateException e) {
            expectedException = e;
        }
        Assertions.assertNotNull(expectedException);

        // detach disk
        vm0.update().withoutDataDisk(newDiskLun).apply();
        Assertions.assertEquals(vmssModelDiskCount, vm0.dataDisks().size());
    }

    @Test
    public void canCreateVirtualMachineScaleSetFromCustomImageWithManagedDisk() throws Exception {
        final String userName = "tirekicker";
        final String password = password();
        final String publicIpDnsLabel = generateRandomResourceName("pip", 10);
        final String customImageName = generateRandomResourceName("img", 10);
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().define(rgName).withRegion(region).create();

        VirtualMachine vm =
            this
                .computeManager
                .virtualMachines()
                .define(generateRandomResourceName("vm", 10))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                .withNewVhd(100)
                .withLun(1)
                .attach()
                .defineUnmanagedDataDisk("disk-2")
                .withNewVhd(50)
                .withLun(2)
                .attach()
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        Assertions.assertNotNull(vm);

        // Waiting for pip to be reachable
        //
        ResourceManagerUtils.sleep(Duration.ofSeconds(40));

        deprovisionAgentInLinuxVM(vm);
        vm.deallocate();
        vm.generalize();

        VirtualMachineCustomImage virtualMachineCustomImage =
            this
                .computeManager
                .virtualMachineCustomImages()
                .define(customImageName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .fromVirtualMachine(vm)
                .create();

        Assertions.assertNotNull(virtualMachineCustomImage);

        Network network =
            this
                .networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1");
        VirtualMachineScaleSet vmScaleSet =
            this
                .computeManager
                .virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D5_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                .withRootUsername(userName)
                .withSsh(sshPublicKey())
                .create();

        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedIterable<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();
        Assertions.assertEquals(TestUtilities.getSize(virtualMachines), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm1 : virtualMachines) {
            Assertions.assertTrue(vm1.isOSBasedOnCustomImage());
            Assertions.assertFalse(vm1.isOSBasedOnPlatformImage());
            Assertions.assertFalse(vm1.isOSBasedOnStoredImage());
            Assertions.assertTrue(vm1.isManagedDiskEnabled());
            Assertions.assertNotNull(vm1.unmanagedDataDisks());
            Assertions.assertEquals(vm1.unmanagedDataDisks().size(), 0);
            Assertions.assertNotNull(vm1.dataDisks());
            Assertions.assertEquals(vm1.dataDisks().size(), 2); // Disks from data disk image from custom image
            Assertions.assertTrue(vm1.dataDisks().containsKey(1));
            VirtualMachineDataDisk disk = vm1.dataDisks().get(1);
            Assertions.assertEquals(disk.size(), 100);
            Assertions.assertTrue(vm1.dataDisks().containsKey(2));
            disk = vm1.dataDisks().get(2);
            Assertions.assertEquals(disk.size(), 50);
        }

        vmScaleSet.deallocate();

        // Updating and adding disk as part of VMSS Update seems consistency failing, CRP is aware of
        // this, hence until it is fixed comment-out the test
        //
        //        {
        //            "startTime": "2017-01-25T06:10:55.2243509+00:00",
        //                "endTime": "2017-01-25T06:11:07.8649525+00:00",
        //                "status": "Failed",
        //                "error": {
        //            "code": "InternalExecutionError",
        //                    "message": "An internal execution error occurred."
        //        },
        //            "name": "6786df83-ed3f-4d7a-bf58-d295b96fef46"
        //        }
        //
        //        vmScaleSet.update()
        //                .withDataDiskUpdated(1, 200) // update not supported
        //                .withNewDataDisk(100)
        //                .apply();
        //
        //        vmScaleSet.start();
        //
        //        virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        //        virtualMachines = virtualMachineScaleSetVMs.list();
        //        for (VirtualMachineScaleSetVM vm1 : virtualMachines) {
        //            Assertions.assertTrue(vm1.isOSBasedOnCustomImage());
        //            Assertions.assertFalse(vm1.isOSBasedOnPlatformImage());
        //            Assertions.assertFalse(vm1.isOSBasedOnStoredImage());
        //            Assertions.assertTrue(vm1.isManagedDiskEnabled());
        //            Assertions.assertNotNull(vm1.unmanagedDataDisks());
        //            Assertions.assertEquals(vm1.unmanagedDataDisks().size(), 0);
        //            Assertions.assertNotNull(vm1.dataDisks());
        //            Assertions.assertEquals(vm1.dataDisks().size(), 3);
        //            Assertions.assertTrue(vm1.dataDisks().containsKey(1));
        //            VirtualMachineDataDisk disk = vm1.dataDisks().get(1);
        //            Assertions.assertEquals(disk.size(), 200);
        //            Assertions.assertTrue(vm1.dataDisks().containsKey(2));
        //            disk = vm1.dataDisks().get(2);
        //            Assertions.assertEquals(disk.size(), 50);
        //            Assertions.assertTrue(vm1.dataDisks().containsKey(0));
        //            disk = vm1.dataDisks().get(0);
        //            Assertions.assertEquals(disk.size(), 100);
        //        }
    }
}
