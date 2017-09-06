/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPSkuType;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class VirtualMachineAvailabilityZoneOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_EAST2;
    private static final String VMNAME = "javavm";

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
    public void canCreateZonedVirtualMachineWithImplicitZoneForRelatedResources() throws Exception {
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(pipDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone("1")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                // Create VM
                .create();

        // Checks the zone assigned to the virtual machine
        //
        Assert.assertNotNull(virtualMachine.availabilityZones());
        Assert.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assert.assertTrue(virtualMachine.availabilityZones().contains("1"));
        // Checks the zone assigned to the implicitly created public IP address.
        // Implicitly created PIP will be BASIC
        //
        PublicIPAddress publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assert.assertNotNull(publicIPAddress.availabilityZones());
        Assert.assertFalse(publicIPAddress.availabilityZones().isEmpty());
        Assert.assertTrue(publicIPAddress.availabilityZones().contains("1"));
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        String osDiskId = virtualMachine.osDiskId();    // Only VM based on managed disk can have zone assigned
        Assert.assertNotNull(osDiskId);
        Assert.assertFalse(osDiskId.isEmpty());
        Disk osDisk = computeManager.disks().getById(osDiskId);
        Assert.assertNotNull(osDisk);
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        Assert.assertNotNull(osDisk.availabilityZones());
        Assert.assertFalse(osDisk.availabilityZones().isEmpty());
        Assert.assertTrue(osDisk.availabilityZones().contains("1"));
    }

    @Test
    public void canCreateZonedVirtualMachineWithExplicitZoneForRelatedResources() throws Exception {
        // Create zoned public IP for the virtual machine
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withAvailabilityZone("1")  // since the SKU is BASIC and VM is zoned, PIP must be zoned
                .withSku(PublicIPSkuType.BASIC)    // Basic sku is never zone resilient, so if you want it zoned, specify explicitly as above.
                // Create PIP
                .create();
        // Create a zoned data disk for the virtual machine
        //
        final String diskName = generateRandomResourceName("dsk", 10);
        Disk dataDisk = computeManager.disks()
                .define(diskName)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withData()
                .withSizeInGB(100)
                // Optionals
                .withAvailabilityZone("1")
                // Create Disk
                .create();
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIPAddress)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone("1")
                .withExistingDataDisk(dataDisk)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                // Create VM
                .create();
        // Checks the zone assigned to the virtual machine
        //
        Assert.assertNotNull(virtualMachine.availabilityZones());
        Assert.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assert.assertTrue(virtualMachine.availabilityZones().contains("1"));
        // Checks the zone assigned to the explicitly created public IP address.
        //
        publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assert.assertNotNull(publicIPAddress.sku());
        Assert.assertTrue(publicIPAddress.sku().equals(PublicIPSkuType.BASIC));
        Assert.assertNotNull(publicIPAddress.availabilityZones());
        Assert.assertFalse(publicIPAddress.availabilityZones().isEmpty());
        Assert.assertTrue(publicIPAddress.availabilityZones().contains("1"));
        // Check the zone assigned to the explicitly created data disk
        //
        Map<Integer, VirtualMachineDataDisk> dataDisks = virtualMachine.dataDisks();
        Assert.assertNotNull(dataDisks);
        Assert.assertFalse(dataDisks.isEmpty());
        VirtualMachineDataDisk dataDisk1 = dataDisks.values().iterator().next();
        Assert.assertNotNull(dataDisk1.id());
        dataDisk = computeManager.disks().getById(dataDisk1.id());
        Assert.assertNotNull(dataDisk);
        Assert.assertNotNull(dataDisk.availabilityZones());
        Assert.assertFalse(dataDisk.availabilityZones().isEmpty());
        Assert.assertTrue(dataDisk.availabilityZones().contains("1"));
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        String osDiskId = virtualMachine.osDiskId();    // Only VM based on managed disk can have zone assigned
        Assert.assertNotNull(osDiskId);
        Assert.assertFalse(osDiskId.isEmpty());
        Disk osDisk = computeManager.disks().getById(osDiskId);
        Assert.assertNotNull(osDisk);
        // Checks the zone assigned to the implicitly created managed OS disk.
        //
        Assert.assertNotNull(osDisk.availabilityZones());
        Assert.assertFalse(osDisk.availabilityZones().isEmpty());
        Assert.assertTrue(osDisk.availabilityZones().contains("1"));
    }

    @Test
    public void canCreateZonedVirtualMachineWithZoneResilientPublicIP() throws Exception {
        // Create zone resilient public IP for the virtual machine
        //
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        PublicIPAddress publicIPAddress = networkManager.publicIPAddresses()
                .define(pipDnsLabel)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withStaticIP()
                // Optionals
                .withSku(PublicIPSkuType.STANDARD)  // No zone selected, STANDARD SKU is zone resilient [zone resilient: resources deployed in all zones by the service and it will be served by all AZs all the time]
                // Create PIP
                .create();
        // Create a zoned virtual machine
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIPAddress)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                // Optionals
                .withAvailabilityZone("1")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                // Create VM
                .create();
        // Checks the zone assigned to the virtual machine
        //
        Assert.assertNotNull(virtualMachine.availabilityZones());
        Assert.assertFalse(virtualMachine.availabilityZones().isEmpty());
        Assert.assertTrue(virtualMachine.availabilityZones().contains("1"));
        // Check the zone resilient PIP
        //
        publicIPAddress = virtualMachine.getPrimaryPublicIPAddress();
        Assert.assertNotNull(publicIPAddress.sku());
        Assert.assertTrue(publicIPAddress.sku().equals(PublicIPSkuType.STANDARD));
        Assert.assertNotNull(publicIPAddress.availabilityZones());  // Though zone-resilient, this property won't be populated by the service.
        Assert.assertTrue(publicIPAddress.availabilityZones().isEmpty());
    }
}


