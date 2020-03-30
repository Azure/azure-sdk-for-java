/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management;

import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.network.Network;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineNics extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final NetworkManager networkManager;
    private String secondaryNicName;

    public TestVirtualMachineNics(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        // Prepare the resource group definition
        final String rgName = virtualMachines.manager().getSdkContext().randomResourceName("rg", 10);;
        Creatable<ResourceGroup> resourceGroupCreatable = virtualMachines.manager().getResourceManager().resourceGroups()
                .define(rgName)
                .withRegion(Region.US_EAST);

        // Prepare the virtual network definition [shared by primary and secondary network interfaces]
        final String vnetName = virtualMachines.manager().getSdkContext().randomResourceName("vnet", 10);;
        Creatable<Network> networkCreatable = this.networkManager.networks()
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare the secondary network interface definition
        secondaryNicName = virtualMachines.manager().getSdkContext().randomResourceName("nic", 10);;
        Creatable<NetworkInterface> secondaryNetworkInterfaceCreatable = this.networkManager.networkInterfaces()
                .define(secondaryNicName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.5");
        // .withNewPrimaryPublicIPAddress();
        // [Secondary NIC cannot have PublicIP - Only primary network interface can reference a public IP address]

        // Prepare the secondary network interface definition
        final String secondaryNicName2 = virtualMachines.manager().getSdkContext().randomResourceName("nic2", 10);;
        Creatable<NetworkInterface> secondaryNetworkInterfaceCreatable2 = this.networkManager.networkInterfaces()
                .define(secondaryNicName2)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.6");

        // Create Virtual Machine
        final String vmName = virtualMachines.manager().getSdkContext().randomResourceName("vm", 10);;
        final String primaryPipName = "pip" + vmName;
        VirtualMachine virtualMachine = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.4")
                .withNewPrimaryPublicIPAddress(primaryPipName)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_A9)
                .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable)
                .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable2)
                .create();

        Assertions.assertTrue(virtualMachine.networkInterfaceIds().size() == 3);
        NetworkInterface primaryNetworkInterface = virtualMachine.getPrimaryNetworkInterface();
        Assertions.assertEquals(primaryNetworkInterface.primaryPrivateIP(), "10.0.0.4");

        PublicIPAddress primaryPublicIPAddress = primaryNetworkInterface.primaryIPConfiguration().getPublicIPAddress();
        Assertions.assertTrue(primaryPublicIPAddress.fqdn().startsWith(primaryPipName));
        return virtualMachine;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        virtualMachine.powerOff();
        virtualMachine.deallocate();
        virtualMachine = virtualMachine.update()
                .withoutSecondaryNetworkInterface(secondaryNicName)
                .apply();

        Assertions.assertTrue(virtualMachine.networkInterfaceIds().size() == 2);
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
