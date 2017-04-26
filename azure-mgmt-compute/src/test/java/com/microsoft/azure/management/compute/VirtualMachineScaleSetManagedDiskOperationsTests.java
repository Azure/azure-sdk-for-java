/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class VirtualMachineScaleSetManagedDiskOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static Region region = Region.US_EAST;

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
    public void canCreateUpdateVirtualMachineScaleSetFromPIRWithManagedDisk() throws Exception {
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region)
                .create();

        Network network = this.networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1");
        VirtualMachineScaleSet vmScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D5_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withRootPassword("123OData!@#123")
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                .withNewDataDisk(100, 2, CachingTypes.READ_ONLY)
                .create();

        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedList<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();
        Assert.assertEquals(virtualMachines.size(), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assert.assertTrue(vm.isOSBasedOnPlatformImage());
            Assert.assertFalse(vm.isOSBasedOnCustomImage());
            Assert.assertFalse(vm.isOSBasedOnStoredImage());
            Assert.assertTrue(vm.isManagedDiskEnabled());
            Assert.assertNotNull(vm.unmanagedDataDisks());
            Assert.assertEquals(vm.unmanagedDataDisks().size(), 0);
            Assert.assertNotNull(vm.dataDisks());
            Assert.assertEquals(vm.dataDisks().size(), 3);
        }
        vmScaleSet.update()
                .withoutDataDisk(0)
                .withNewDataDisk(50)
                .apply();

        virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        virtualMachines = virtualMachineScaleSetVMs.list();
        Assert.assertEquals(virtualMachines.size(), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm : virtualMachines) {
            Assert.assertNotNull(vm.dataDisks());
            Assert.assertEquals(vm.dataDisks().size(), 3);
        }
    }

    @Test
    public void canCreateVirtualMachineScaleSetFromCustomImageWithManagedDisk() throws Exception {
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        final String publicIpDnsLabel = generateRandomResourceName("pip", 10);
        final String customImageName = generateRandomResourceName("img", 10);
        final String vmssName = generateRandomResourceName("vmss", 10);

        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region)
                .create();

        VirtualMachine vm = this.computeManager.virtualMachines().define(generateRandomResourceName("vm", 10))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withRootPassword(password)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                    .withNewVhd(100)
                    .withLun(1)
                    .attach()
                .defineUnmanagedDataDisk("disk-2")
                    .withNewVhd(50)
                    .withLun(2)
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        Assert.assertNotNull(vm);

        deprovisionAgentInLinuxVM(vm.getPrimaryPublicIPAddress().fqdn(), 22, userName, password);
        vm.deallocate();
        vm.generalize();

        VirtualMachineCustomImage virtualMachineCustomImage = this.computeManager.virtualMachineCustomImages()
                .define(customImageName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .fromVirtualMachine(vm)
                .create();

        Assert.assertNotNull(virtualMachineCustomImage);

        Network network = this.networkManager
                .networks()
                .define(generateRandomResourceName("vmssvnet", 15))
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1");
        VirtualMachineScaleSet vmScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(vmssName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D5_V2)
                .withExistingPrimaryNetworkSubnet(network, "subnet1")
                .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withoutPrimaryInternalLoadBalancer()
                .withLinuxCustomImage(virtualMachineCustomImage.id())
                .withRootUsername(userName)
                .withRootPassword(password)
                .create();

        VirtualMachineScaleSetVMs virtualMachineScaleSetVMs = vmScaleSet.virtualMachines();
        PagedList<VirtualMachineScaleSetVM> virtualMachines = virtualMachineScaleSetVMs.list();
        Assert.assertEquals(virtualMachines.size(), vmScaleSet.capacity());
        for (VirtualMachineScaleSetVM vm1 : virtualMachines) {
            Assert.assertTrue(vm1.isOSBasedOnCustomImage());
            Assert.assertFalse(vm1.isOSBasedOnPlatformImage());
            Assert.assertFalse(vm1.isOSBasedOnStoredImage());
            Assert.assertTrue(vm1.isManagedDiskEnabled());
            Assert.assertNotNull(vm1.unmanagedDataDisks());
            Assert.assertEquals(vm1.unmanagedDataDisks().size(), 0);
            Assert.assertNotNull(vm1.dataDisks());
            Assert.assertEquals(vm1.dataDisks().size(), 2); // Disks from data disk image from custom image
            Assert.assertTrue(vm1.dataDisks().containsKey(1));
            VirtualMachineDataDisk disk = vm1.dataDisks().get(1);
            Assert.assertEquals(disk.size(), 100);
            Assert.assertTrue(vm1.dataDisks().containsKey(2));
            disk = vm1.dataDisks().get(2);
            Assert.assertEquals(disk.size(), 50);
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
//            Assert.assertTrue(vm1.isOSBasedOnCustomImage());
//            Assert.assertFalse(vm1.isOSBasedOnPlatformImage());
//            Assert.assertFalse(vm1.isOSBasedOnStoredImage());
//            Assert.assertTrue(vm1.isManagedDiskEnabled());
//            Assert.assertNotNull(vm1.unmanagedDataDisks());
//            Assert.assertEquals(vm1.unmanagedDataDisks().size(), 0);
//            Assert.assertNotNull(vm1.dataDisks());
//            Assert.assertEquals(vm1.dataDisks().size(), 3);
//            Assert.assertTrue(vm1.dataDisks().containsKey(1));
//            VirtualMachineDataDisk disk = vm1.dataDisks().get(1);
//            Assert.assertEquals(disk.size(), 200);
//            Assert.assertTrue(vm1.dataDisks().containsKey(2));
//            disk = vm1.dataDisks().get(2);
//            Assert.assertEquals(disk.size(), 50);
//            Assert.assertTrue(vm1.dataDisks().containsKey(0));
//            disk = vm1.dataDisks().get(0);
//            Assert.assertEquals(disk.size(), 100);
//        }
    }
}
